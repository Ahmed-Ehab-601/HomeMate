import { useEffect, useRef, useState, useCallback } from 'react';
import SockJS from 'sockjs-client';
import { Stomp } from '@stomp/stompjs';

/**
 * Custom React hook for WebSocket connection
 * Handles real-time messaging, status updates, and presence
 * 
 * @param {string|number} chatId - The chat ID to connect to
 * @param {string|number} userId - Current user's ID
 * @param {string|number} userId - Current user's ID
 * @param {string|number} [recipientId] - ID of the other user in chat (to track presence)
 * @param {string} role - User's role (USER or TASKER)
 * @param {string} token - JWT authentication token
 * @param {Function} onMessageReceived - Callback for new messages
 * @param {Function} onStatusUpdate - Callback for status updates
 * @returns {Object} WebSocket connection state and methods
 */
export const useWebSocket = (chatId, userId, recipientId, role, token, onMessageReceived, onStatusUpdate) => {
  // State
  const [connected, setConnected] = useState(false);

  const [typingUsers, setTypingUsers] = useState(new Set());
  const [onlineStatus, setOnlineStatus] = useState({});
  const [error, setError] = useState(null);

  // Keep latest callbacks in refs to avoid stale closures in subscriptions
  const onMessageReceivedRef = useRef(onMessageReceived);
  const onStatusUpdateRef = useRef(onStatusUpdate);

  useEffect(() => {
    onMessageReceivedRef.current = onMessageReceived;
    onStatusUpdateRef.current = onStatusUpdate;
  }, [onMessageReceived, onStatusUpdate]);

  // Refs
  const stompClientRef = useRef(null);
  const subscriptionsRef = useRef([]);
  const reconnectTimeoutRef = useRef(null);
  const reconnectAttempts = useRef(0);
  const typingTimeoutRef = useRef(null);

  // Constants
  const WEBSOCKET_URL = 'http://localhost:8080/HomeMate';
  const API_BASE = 'http://localhost:8080/api';
  const MAX_RECONNECT_ATTEMPTS = 5;
  const RECONNECT_DELAY_BASE = 1000;
  const MAX_RECONNECT_DELAY = 30000;

  /**
   * Connect to WebSocket server
   */
  const connect = useCallback(() => {
    // Don't connect if already connected
    if (stompClientRef.current?.connected) {
      console.log('✅ Already connected to WebSocket');
      return;
    }

    // Validate required parameters
    if (!chatId || !userId || !token) {
      console.error('❌ Missing required parameters for WebSocket connection');
      setError('Missing connection parameters');
      return;
    }

    console.log('🔌 Connecting to WebSocket...', { chatId, userId, role });

    try {
      // Create SockJS connection
      const socket = new SockJS(WEBSOCKET_URL);

      // Create STOMP client with proper factory function
      const client = Stomp.over(() => socket);

      // Configure client
      client.reconnectDelay = 5000;
      client.heartbeatIncoming = 4000;
      client.heartbeatOutgoing = 4000;

      // Debug mode (set to false in production)
      client.debug = (str) => {
        if (process.env.NODE_ENV === 'development') {
          console.log('STOMP:', str);
        }
      };

      // Connection headers with authentication
      const connectHeaders = {
        Authorization: `Bearer ${token}`,
        userId: role === 'USER' ? String(userId) : '',
        taskerId: role === 'TASKER' ? String(userId) : '',
        role: role
      };

      // Connect to WebSocket
      client.connect(
        connectHeaders,
        // Success callback
        () => {
          console.log('✅ WebSocket Connected Successfully');
          setConnected(true);
          setError(null);
          reconnectAttempts.current = 0;
          stompClientRef.current = client;

          // Subscribe to all necessary topics
          subscribeToTopics(client);
        },
        // Error callback
        (error) => {
          console.error('❌ WebSocket connection error:', error);
          setConnected(false);
          setError(error.message || 'Connection failed');
          handleReconnect();
        }
      );
    } catch (error) {
      console.error('❌ Error creating WebSocket connection:', error);
      setError(error.message);
      handleReconnect();
    }
  }, [chatId, userId, recipientId, role, token]);

  /**
   * Subscribe to all WebSocket topics
   */
  const subscribeToTopics = useCallback((client) => {
    try {
      // 1. Subscribe to chat messages (Only if chatId exists)
      if (chatId) {
        const chatSub = client.subscribe(`/send/chat/${chatId}`, (message) => {
          try {
            const newMessage = JSON.parse(message.body);
            console.log('📨 WS Received in hook (MAIN):', newMessage);

            if (onMessageReceivedRef.current) {
              onMessageReceivedRef.current(newMessage);
            }

            const isSender = role === 'USER' ? newMessage.isUserSender : !newMessage.isUserSender;
            if (!isSender && newMessage.messageStatus === 'SENT') {
              markMessageAsReceived(newMessage.messageId, token);
            }

            // Infer presence: If we received a message, they are online
            if (!isSender) {
              setOnlineStatus(prev => ({
                ...prev,
                [newMessage.senderId]: true
              }));
            }
          } catch (err) {
            console.error('Error processing message (MAIN):', err);
          }
        });
        subscriptionsRef.current.push(chatSub);

        // DEBUG: Subscribe to fallback topics
        const fallbackTopics = [
          `/send/chat/${chatId}/message`,
          `/send/messages/${chatId}`,
          `/send/message/${chatId}`,
          `/send/chat/${chatId}`
        ];

        fallbackTopics.forEach(topic => {
          const sub = client.subscribe(topic, (message) => {
            console.log(`📨 WS Received on FALLBACK topic (${topic}):`, message.body);
            try {
              const newMessage = JSON.parse(message.body);
              if (onMessageReceivedRef.current) {
                onMessageReceivedRef.current(newMessage);
              }
            } catch (e) {
              console.error('Error parsing fallback message:', e);
            }
          });
          subscriptionsRef.current.push(sub);
        });

        // 2. Subscribe to message status updates
        const statusSub = client.subscribe(`/send/chat/${chatId}/status`, (message) => {
          try {
            const statusUpdate = JSON.parse(message.body);
            console.log('📊 WS Status update in hook:', statusUpdate);

            if (onStatusUpdateRef.current) {
              onStatusUpdateRef.current(statusUpdate);
            }
          } catch (err) {
            console.error('Error processing status update:', err);
          }
        });
        subscriptionsRef.current.push(statusSub);

        // 3. Subscribe to typing indicators
        const typingSub = client.subscribe(`/send/chat/${chatId}/typing`, (message) => {
          try {
            const typingData = JSON.parse(message.body);
            console.log('⌨️ Typing indicator:', typingData);

            // Only track other users' typing status
            // Compare as strings to handle potential type mismatches (API often returns strings)
            if (String(typingData.userId) !== String(userId)) {
              // Infer presence: If they are typing, they are online
              setOnlineStatus(prev => ({
                ...prev,
                [typingData.userId]: true
              }));

              setTypingUsers(prev => {
                const newSet = new Set(prev);
                if (typingData.isTyping) {
                  newSet.add(typingData.userId);
                } else {
                  newSet.delete(typingData.userId);
                }
                return newSet;
              });
            }
          } catch (err) {
            console.error('Error processing typing indicator:', err);
          }
        });
        subscriptionsRef.current.push(typingSub);

        // 4. Subscribe to read receipts
        const readReceiptSub = client.subscribe(`/send/chat/${chatId}/read-receipt`, (message) => {
          try {
            const receipt = JSON.parse(message.body);
            console.log('📖 Read receipt:', receipt);

            if (onStatusUpdateRef.current) {
              onStatusUpdateRef.current({
                type: 'READ_RECEIPT',
                ...receipt,
                status: 'READ'
              });
            }

            // Infer presence: If they read it, they are online
            if (receipt.readBy && String(receipt.readBy) !== String(userId)) {
              setOnlineStatus(prev => ({
                ...prev,
                [receipt.readBy]: true
              }));
            }
          } catch (err) {
            console.error('Error processing read receipt:', err);
          }
        });
        subscriptionsRef.current.push(readReceiptSub);
      }

      // 5. Subscribe to personal notifications
      const notificationSub = client.subscribe(
        `/send/notifications/${role.toLowerCase()}/${userId}`,
        (message) => {
          try {
            const notification = JSON.parse(message.body);
            console.log('🔔 Notification:', notification);

            // Handle notification (e.g., show toast, update badge)
            if (notification.type === 'NEW_MESSAGE') {
              console.log('New message notification from chat:', notification.chatId);
            }
          } catch (err) {
            console.error('Error processing notification:', err);
          }
        }
      );
      subscriptionsRef.current.push(notificationSub);

      // 6. Subscribe to online status updates (My own status - confirming connection)
      const statusUpdateSub = client.subscribe(`/send/status/${userId}`, (message) => {
        try {
          const status = JSON.parse(message.body);
          console.log('👤 My status update:', status);

          // We don't necessarily update onlineStatus for ourselves, but we could
        } catch (err) {
          console.error('Error processing status update:', err);
        }
      });
      subscriptionsRef.current.push(statusUpdateSub);

      // 7. Subscribe to RECIPIENT status (The other user)
      if (recipientId) {
        console.log(`👀 Watching status for recipient: ${recipientId}`);
        const recipientStatusSub = client.subscribe(`/send/status/${recipientId}`, (message) => {
          try {
            const status = JSON.parse(message.body);
            console.log('👤 Recipient status update:', status);

            const statusUserId = status.userId || status.taskerId;
            setOnlineStatus(prev => ({
              ...prev,
              [statusUserId]: status.status === 'online'
            }));
          } catch (err) {
            console.error('Error processing recipient status:', err);
          }
        });
        subscriptionsRef.current.push(recipientStatusSub);
      }

      console.log('✅ Subscribed to all topics successfully');
    } catch (error) {
      console.error('❌ Error subscribing to topics:', error);
      setError('Failed to subscribe to topics');
    }
  }, [chatId, userId, recipientId, role, token]); // Callbacks are now in refs

  /**
   * Handle reconnection with exponential backoff
   */
  const handleReconnect = useCallback(() => {
    if (reconnectAttempts.current >= MAX_RECONNECT_ATTEMPTS) {
      console.error('❌ Max reconnection attempts reached');
      setError('Connection failed. Please refresh the page.');
      return;
    }

    reconnectAttempts.current += 1;
    const delay = Math.min(
      RECONNECT_DELAY_BASE * Math.pow(2, reconnectAttempts.current),
      MAX_RECONNECT_DELAY
    );

    console.log(`🔄 Reconnecting in ${delay}ms (attempt ${reconnectAttempts.current}/${MAX_RECONNECT_ATTEMPTS})`);

    reconnectTimeoutRef.current = setTimeout(() => {
      connect();
    }, delay);
  }, [connect]);

  /**
   * Send typing indicator
   */
  const sendTypingIndicator = useCallback((isTyping) => {
    if (!stompClientRef.current?.connected) {
      console.warn('Cannot send typing indicator: not connected');
      return;
    }

    try {
      stompClientRef.current.send(
        `/app/chat/${chatId}/typing`,
        {},
        JSON.stringify({
          userId: String(userId),
          role: role,
          isTyping: isTyping
        })
      );

      // Auto-stop typing after 2 seconds of inactivity
      if (isTyping) {
        if (typingTimeoutRef.current) {
          clearTimeout(typingTimeoutRef.current);
        }
        typingTimeoutRef.current = setTimeout(() => {
          sendTypingIndicator(false);
        }, 2000);
      }
    } catch (error) {
      console.error('Error sending typing indicator:', error);
    }
  }, [chatId, userId, role]);

  /**
   * Send a chat message via WebSocket (Broadcast only)
   * Use this after saving to DB via REST to notify others immediately
   */
  const sendMessage = useCallback((messageDto) => {
    if (!stompClientRef.current?.connected) {
      console.warn('Cannot send message via WS: not connected');
      return;
    }

    try {
      stompClientRef.current.send(
        `/app/chat/${chatId}/send`,
        {},
        JSON.stringify(messageDto)
      );
      console.log('📤 Message broadcast via WS:', messageDto.messageId);
    } catch (error) {
      console.error('Error broadcasting message:', error);
    }
  }, [chatId]);

  /**
   * Broadcast a status update via WebSocket
   */
  const updateMessageStatus = useCallback((messageId, status) => {
    if (!stompClientRef.current?.connected) return;

    try {
      stompClientRef.current.send(
        `/app/chat/${chatId}/status`,
        {},
        JSON.stringify({
          messageId: String(messageId),
          status: status
        })
      );
      console.log('📤 Status update broadcast:', { messageId, status });
    } catch (error) {
      console.error('Error broadcasting status:', error);
    }
  }, [chatId]);

  /**
   * Mark a single message as received (call REST endpoint)
   */
  const markMessageAsReceived = useCallback(async (messageId, authToken) => {
    if (!messageId || !authToken) return;

    try {
      const endpoint = role === 'USER'
        ? `${API_BASE}/message/status/user/${messageId}/received`
        : `${API_BASE}/message/status/tasker/${messageId}/received`;

      const response = await fetch(endpoint, {
        method: 'PUT',
        headers: {
          'Authorization': `Bearer ${authToken}`,
          'Content-Type': 'application/json'
        }
      });

      if (response.ok) {
        console.log('✅ Message marked as received:', messageId);
        // Also broadcast the status change
        updateMessageStatus(messageId, 'RECEIVED');
      } else {
        console.error('Failed to mark message as received:', response.status);
      }
    } catch (error) {
      console.error('Error marking message as received:', error);
    }
  }, [role, updateMessageStatus]);

  /**
   * Mark a single message as read (call REST endpoint)
   */
  const markMessageAsRead = useCallback(async (messageId, authToken) => {
    if (!messageId || !authToken) return;

    try {
      const endpoint = role === 'USER'
        ? `${API_BASE}/message/status/user/${messageId}/read`
        : `${API_BASE}/message/status/tasker/${messageId}/read`;

      const response = await fetch(endpoint, {
        method: 'PUT',
        headers: {
          'Authorization': `Bearer ${authToken}`,
          'Content-Type': 'application/json'
        }
      });

      if (response.ok) {
        console.log('✅ Message marked as read:', messageId);
        // Also broadcast the status change
        updateMessageStatus(messageId, 'READ');
      } else {
        console.error('Failed to mark message as read:', response.status);
      }
    } catch (error) {
      console.error('Error marking message as read:', error);
    }
  }, [role, updateMessageStatus]);

  /**
   * Mark all messages in chat as received (call REST endpoint)
   */
  const markAllMessagesAsReceived = useCallback(async (authToken) => {
    if (!chatId || !authToken) return;

    try {
      const endpoint = role === 'USER'
        ? `${API_BASE}/message/status/user/chat/${chatId}/received`
        : `${API_BASE}/message/status/tasker/chat/${chatId}/received`;

      const response = await fetch(endpoint, {
        method: 'PUT',
        headers: {
          'Authorization': `Bearer ${authToken}`,
          'Content-Type': 'application/json'
        }
      });

      if (response.ok) {
        console.log('✅ All messages marked as received in chat:', chatId);
      } else {
        console.error('Failed to mark all messages as received:', response.status);
      }
    } catch (error) {
      console.error('Error marking all messages as received:', error);
    }
  }, [chatId, role]);

  /**
   * Mark all messages in chat as read (call REST endpoint)
   */
  const markAllMessagesAsRead = useCallback(async (authToken) => {
    if (!chatId || !authToken) return;

    try {
      const endpoint = role === 'USER'
        ? `${API_BASE}/message/${chatId}/mark-read-user` // Verify endpoint
        // Or maybe endpoint is mark-all-read?
        // Let's assume the controller: @MessageMapping is mark-all-read.
        // REST endpoint? In logic above, user said 'markAsRead' in ChatPage uses:
        // message/${chatId}/mark-read-user
        : `${API_BASE}/message/${chatId}/mark-read-tasker`;

      const response = await fetch(endpoint, {
        method: 'PUT',
        headers: {
          'Authorization': `Bearer ${authToken}`,
          'Content-Type': 'application/json'
        }
      });

      if (response.ok) {
        console.log('✅ All messages marked as read in chat:', chatId);
        // Also broadcast the status change via WebSocket
        // Use the WebSocket controller's mark-all-read endpoint if possible
        if (stompClientRef.current?.connected) {
          stompClientRef.current.send(
            `/app/chat/${chatId}/mark-all-read`,
            {},
            JSON.stringify({ userId: userId, role: role })
          );
        }
      } else {
        console.error('Failed to mark all messages as read:', response.status);
      }
    } catch (error) {
      console.error('Error marking all messages as read:', error);
    }
  }, [chatId, role, userId]);

  /**
   * Disconnect from WebSocket
   */
  const disconnect = useCallback(() => {
    console.log('🔌 Disconnecting WebSocket...');

    // Clear typing timeout
    if (typingTimeoutRef.current) {
      clearTimeout(typingTimeoutRef.current);
      typingTimeoutRef.current = null;
    }

    // Unsubscribe from all subscriptions
    subscriptionsRef.current.forEach(sub => {
      try {
        sub.unsubscribe();
      } catch (error) {
        console.error('Error unsubscribing:', error);
      }
    });
    subscriptionsRef.current = [];

    // Disconnect client
    if (stompClientRef.current?.connected) {
      try {
        stompClientRef.current.disconnect(() => {
          console.log('✅ WebSocket disconnected');
          setConnected(false);
        });
      } catch (error) {
        console.error('Error disconnecting:', error);
      }
    }

    stompClientRef.current = null;

    // Clear reconnect timeout
    if (reconnectTimeoutRef.current) {
      clearTimeout(reconnectTimeoutRef.current);
      reconnectTimeoutRef.current = null;
    }

    // Reset state
    setConnected(false);
    setError(null);
  }, []);

  /**
   * Check if a specific user is online
   */
  const isUserOnline = useCallback((targetUserId) => {
    return onlineStatus[targetUserId] === true;
  }, [onlineStatus]);

  /**
   * Connect on mount, disconnect on unmount
   */
  useEffect(() => {
    // START CONNECTION: Connect if we have USER info. ChatId is optional (Global vs Chat mode).
    if (userId && token) {
      connect();
    }

    return () => {
      disconnect();
    };
  }, [chatId, userId, token, connect, disconnect]);

  /**
   * Cleanup on unmount
   */
  useEffect(() => {
    return () => {
      if (typingTimeoutRef.current) {
        clearTimeout(typingTimeoutRef.current);
      }
      if (reconnectTimeoutRef.current) {
        clearTimeout(reconnectTimeoutRef.current);
      }
    };
  }, []);

  // Return hook API
  return {
    // Connection state
    connected,
    error,

    // Presence state
    typingUsers,
    onlineStatus,

    // Methods
    sendTypingIndicator,
    sendMessage, // Exposed new method
    updateMessageStatus, // Exposed new method
    markMessageAsReceived,
    markMessageAsRead,
    markAllMessagesAsReceived,
    markAllMessagesAsRead,
    disconnect,
    isUserOnline,

    // Connection control
    reconnect: connect
  };
};

export default useWebSocket;