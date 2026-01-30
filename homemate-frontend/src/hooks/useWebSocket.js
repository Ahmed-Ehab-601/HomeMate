import { useEffect, useRef, useState, useCallback } from 'react';
import { useGlobalWebSocket } from '../contexts/WebSocketContext';

/**
 * Custom React hook for WebSocket interaction (Chat specific)
 * Now uses the shared WebSocketContext connection
 */
export const useWebSocket = (chatId, userId, recipientId, role, token, onMessageReceived, onStatusUpdate) => {
  const {
    connected,
    error,
    onlineStatus,
    client,
    sendTypingIndicator: sendGlobalTyping,
    sendPresenceUpdate,
    sendHeartbeat
  } = useGlobalWebSocket();

  const [typingUsers, setTypingUsers] = useState(new Set());
  const subscriptionsRef = useRef([]);
  const typingTimeoutRef = useRef(null);
  const isActiveRef = useRef(true); // Track if this chat is currently active

  // Keep callback refs
  const onMessageReceivedRef = useRef(onMessageReceived);
  const onStatusUpdateRef = useRef(onStatusUpdate);

  useEffect(() => {
    onMessageReceivedRef.current = onMessageReceived;
    onStatusUpdateRef.current = onStatusUpdate;
  }, [onMessageReceived, onStatusUpdate]);

  // Mark this chat as active when mounted, inactive when unmounted
  useEffect(() => {
    isActiveRef.current = true;
    console.log(`✅ [Hook] Chat ${chatId} is now ACTIVE`);
    
    return () => {
      isActiveRef.current = false;
      console.log(`❌ [Hook] Chat ${chatId} is now INACTIVE`);
    };
  }, [chatId]);

  // Constants
  const WEBSOCKET_URL = baseUrl + '/HomeMate';
  const API_BASE = baseUrl + '/api';
  const MAX_RECONNECT_ATTEMPTS = 5;
  const RECONNECT_DELAY_BASE = 1000;
  const MAX_RECONNECT_DELAY = 30000;
  // Subscribe to chat-specific topics when connected and chatId is present
  useEffect(() => {
    if (!connected || !client || !chatId) return;

    console.log(`🔌 [Hook] Subscribing to chat ${chatId}`);

    const subs = [];

    try {
      // 1. Chat Messages
      const chatSub = client.subscribe(`/send/chat/${chatId}`, (message) => {
        try {
          const newMessage = JSON.parse(message.body);
          
          console.log(`📨 [Hook] Message received in chat ${chatId}:`, {
            messageId: newMessage.messageId,
            isActive: isActiveRef.current,
            sender: newMessage.isUserSender ? 'USER' : 'TASKER',
            myRole: role
          });
          
          if (onMessageReceivedRef.current) {
            onMessageReceivedRef.current(newMessage);
          }
          
          // CRITICAL FIX: Only auto-mark if this chat is CURRENTLY ACTIVE
          if (isActiveRef.current) {
            const isSender = role === 'USER' ? newMessage.isUserSender : !newMessage.isUserSender;
            if (!isSender && newMessage.messageStatus === 'SENT') {
              console.log(`✅ [Hook] Auto-marking message ${newMessage.messageId} as RECEIVED (chat is active)`);
              markMessageAsReceived(newMessage.messageId, token);
            }
          } else {
            console.log(`⏸️ [Hook] Skipping auto-mark for message ${newMessage.messageId} (chat is inactive)`);
          }
        } catch (err) {
          console.error('Error processing msg:', err);
        }
      });
      subs.push(chatSub);

      // 2. Status Updates
      const statusSub = client.subscribe(`/send/chat/${chatId}/status`, (message) => {
        try {
          const update = JSON.parse(message.body);
          if (onStatusUpdateRef.current) {
            onStatusUpdateRef.current(update);
          }
        } catch (err) {
          console.error('Error processing status:', err);
        }
      });
      subs.push(statusSub);

      // 3. Typing Indicators
      const typingSub = client.subscribe(`/send/chat/${chatId}/typing`, (message) => {
        try {
          const data = JSON.parse(message.body);
          if (String(data.userId) !== String(userId)) {
            setTypingUsers(prev => {
              const newSet = new Set(prev);
              if (data.isTyping) newSet.add(data.userId);
              else newSet.delete(data.userId);
              return newSet;
            });
          }
        } catch (err) {
          console.error('Error processing typing:', err);
        }
      });
      subs.push(typingSub);

      // 4. Read Receipts
      const readSub = client.subscribe(`/send/chat/${chatId}/read-receipt`, (message) => {
        try {
          const receipt = JSON.parse(message.body);
          if (onStatusUpdateRef.current) {
            onStatusUpdateRef.current({
              type: 'READ_RECEIPT',
              ...receipt,
              status: 'READ'
            });
          }
        } catch (err) {
          console.error('Error processing read receipt:', err);
        }
      });
      subs.push(readSub);

    } catch (err) {
      console.error('❌ [Hook] Subscription error:', err);
    }

    subscriptionsRef.current = subs;

    return () => {
      console.log(`🔌 [Hook] Unsubscribing from chat ${chatId}`);
      subs.forEach(s => s.unsubscribe());
      subscriptionsRef.current = [];
    };
  }, [connected, client, chatId, userId, role, token]);

  // Send Typing
  const sendTypingIndicator = useCallback((isTyping) => {
    if (!client?.connected || !chatId) return;

    try {
      client.send(
        `/app/chat/${chatId}/typing`,
        {},
        JSON.stringify({
          userId: String(userId),
          role: role,
          isTyping: isTyping
        })
      );

      if (isTyping) {
        if (typingTimeoutRef.current) clearTimeout(typingTimeoutRef.current);
        typingTimeoutRef.current = setTimeout(() => sendTypingIndicator(false), 2000);
      }
    } catch (err) {
      console.error('Error sending typing:', err);
    }
  }, [client, chatId, userId, role]);

  // Send Message
  const sendMessage = useCallback((messageDto) => {
    if (!client?.connected || !chatId) return;
    try {
      client.send(`/app/chat/${chatId}/send`, {}, JSON.stringify(messageDto));
    } catch (err) {
      console.error('Error sending message:', err);
    }
  }, [client, chatId]);

  // Update Status
  const updateMessageStatus = useCallback((messageId, status) => {
    if (!client?.connected || !chatId) return;
    try {
      client.send(
        `/app/chat/${chatId}/status`,
        {},
        JSON.stringify({ messageId: String(messageId), status })
      );
    } catch (err) {
      console.error('Error updating status:', err);
    }
  }, [client, chatId]);



  const markMessageAsReceived = useCallback(async (messageId, authToken) => {
    if (!messageId || !authToken) return;
    try {
      const endpoint = role === 'USER'
        ? `${API_BASE}/message/status/user/${messageId}/received`
        : `${API_BASE}/message/status/tasker/${messageId}/received`;

      console.log(`🔄 [Hook] Marking message ${messageId} as RECEIVED via ${endpoint}`);
      
      const response = await fetch(endpoint, {
        method: 'PUT',
        headers: { 'Authorization': `Bearer ${authToken}` }
      });
      
      if (response.ok) {
        console.log(`✅ [Hook] Message ${messageId} marked as RECEIVED`);
        updateMessageStatus(messageId, 'RECEIVED');
      } else {
        console.error(`❌ [Hook] Failed to mark message ${messageId}:`, response.status);
      }
    } catch (e) { 
      console.error('Error marking message as received:', e); 
    }
  }, [role, updateMessageStatus]);

  const markMessageAsRead = useCallback(async (messageId, authToken) => {
    if (!messageId || !authToken) return;
    try {
      const endpoint = role === 'USER'
        ? `${API_BASE}/message/status/user/${messageId}/read`
        : `${API_BASE}/message/status/tasker/${messageId}/read`;

      const response = await fetch(endpoint, {
        method: 'PUT',
        headers: { 'Authorization': `Bearer ${authToken}` }
      });
      if (response.ok) updateMessageStatus(messageId, 'READ');
    } catch (e) { console.error(e); }
  }, [role, updateMessageStatus]);

  const markAllMessagesAsRead = useCallback(async (authToken) => {
    if (!chatId || !authToken) return;
    try {
      const endpoint = role === 'USER'
        ? `${API_BASE}/message/${chatId}/mark-read-user`
        : `${API_BASE}/message/${chatId}/mark-read-tasker`;

      console.log(`📖 [Hook] Marking all messages as read in chat ${chatId}`);

      const response = await fetch(endpoint, {
        method: 'PUT',
        headers: { 'Authorization': `Bearer ${authToken}` }
      });

      if (response.ok && client?.connected) {
        client.send(
          `/app/chat/${chatId}/mark-all-read`,
          {},
          JSON.stringify({ userId: userId, role: role })
        );
        console.log(`✅ [Hook] All messages marked as read in chat ${chatId}`);
      }
    } catch (e) { console.error(e); }
  }, [chatId, role, userId, client]);

  // Is User Online Helper
  const isUserOnline = useCallback((targetId) => {
    const online = onlineStatus[targetId] === true;
    console.log(`👤 [Hook] isUserOnline(${targetId}) = ${online}`, onlineStatus);
    return online;
  }, [onlineStatus]);

  return {
    connected,
    error,
    typingUsers,
    onlineStatus,
    sendTypingIndicator,
    sendMessage,
    updateMessageStatus,
    markMessageAsReceived,
    markMessageAsRead,
    markAllMessagesAsRead,
    isUserOnline,
    disconnect: () => console.warn('Disconnect handled globally now'),
    reconnect: () => console.warn('Reconnect handled globally now'),
    sendPresenceUpdate,
    sendHeartbeat
  };
};