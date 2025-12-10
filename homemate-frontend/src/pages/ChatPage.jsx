import React, { useState, useEffect, useRef, useCallback } from 'react';
import { Send, Phone, Image, X, ArrowLeft } from 'lucide-react';
import { useAuth } from "../contexts/AuthContext";
import { useNavigate, useParams } from 'react-router-dom';
import { useWebSocket } from '../hooks/useWebSocket';

const ChatInterface = () => {
  const [inputMessage, setInputMessage] = useState('');
  const [selectedImage, setSelectedImage] = useState(null);
  const [imagePreview, setImagePreview] = useState(null);
  const [page, setPage] = useState(0);
  const [hasMore, setHasMore] = useState(true);
  const [loading, setLoading] = useState(false);
  const [chatDetails, setChatDetails] = useState(null);
  const [recipientName, setRecipientName] = useState('');
  const [recipientOnline, setRecipientOnline] = useState(false);
  const [phoneNumber, setPhoneNumber] = useState(null);
  const [showCallModal, setShowCallModal] = useState(false);
  const [error, setError] = useState(null);
  const [showError, setShowError] = useState(false);
  const [isTyping, setIsTyping] = useState(false);

  const messagesEndRef = useRef(null);
  const messagesContainerRef = useRef(null);
  const fileInputRef = useRef(null);
  const typingTimeoutRef = useRef(null);

  const { getToken, getUserRole, isTasker, user } = useAuth();
  const navigate = useNavigate();
  const { chatId } = useParams();

  const MESSAGE_CHAR_LIMIT = 200;
  const API_BASE = 'http://localhost:8080/api';

  // Decode JWT
  function decodeJwtPayload(token) {
    if (!token) return null;
    try {
      const parts = token.split('.');
      if (parts.length < 2) return null;
      const payload = parts[1];
      const b64 = payload.replace(/-/g, '+').replace(/_/g, '/');
      const pad = b64.length % 4;
      const padded = pad ? b64 + '='.repeat(4 - pad) : b64;
      const decoded = atob(padded);
      const json = decodeURIComponent(
        decoded.split('').map(c => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2)).join('')
      );
      return JSON.parse(json);
    } catch (err) {
      return null;
    }
  }

  const _token = getToken();
  const _decoded = decodeJwtPayload(_token);
  const _tokenRole = _decoded
    ? (_decoded.role || (Array.isArray(_decoded.roles) ? _decoded.roles[0] : _decoded.roles) ||
      (Array.isArray(_decoded.authorities) ? _decoded.authorities[0] : _decoded.authorities) ||
      _decoded.authority)
    : null;

  const userRole = _tokenRole || getUserRole() || user?.role || 'ROLE_USER';
  const isUserRole = userRole === 'ROLE_USER';
  const currentUserId = user?.id || _decoded?.sub;

  // Determine recipient ID for presence tracking
  const recipientId = chatDetails
    ? (isUserRole ? chatDetails.taskerId : chatDetails.userId)
    : null;

  useEffect(() => {
    if (recipientId) {
      console.log('👥 ChatPage identified recipientId:', recipientId);
    }
  }, [recipientId]);

  // Initialize WebSocket
  const handleMessageReceived = useCallback((newMessage) => {
    setAllMessages(prev => {
      // Avoid duplicates
      const exists = prev.some(msg => String(msg.messageId) === String(newMessage.messageId));
      if (exists) return prev;

      // If we are currently watching this chat, mark incoming messages as read instantly?
      // Typically we rely on the component mount or visibility.
      // But for now, just add it.
      return [...prev, newMessage];
    });

    // Scroll to bottom if near bottom?
    // We have an effect for allMessages changes, so that handles scroll.
  }, []);

  const handleStatusUpdate = useCallback((update) => {
    console.log('🔄 ChatPage handleStatusUpdate:', update);
    setAllMessages(prev => prev.map(msg => {
      // Handle explicit Read Receipts
      if (update.type === 'READ_RECEIPT') {
        // If the receipt says it was read by the OTHER user
        // Then mark all MY sent messages as SEEN
        // (Assuming simple logic: if they read one, they read all prior? Or typical "Mark/Seen by X")
        // The receipt has { chatId, readBy, role }

        // If update.readBy is NOT me, then it means THEY read my messages.
        if (String(update.readBy) !== String(currentUserId)) {
          if (msg.isUserSender === isUserRole) { // If I sent this message
            if (msg.messageStatus !== 'SEEN' && msg.messageStatus !== 'READ') {
              return { ...msg, messageStatus: 'SEEN' };
            }
          }
        }
        return msg;
      }

      if (update.bulkUpdate) {
        // Bulk update logic (Legacy/Fallback)
        if (update.status === 'READ') {
          const isMyMessage = isUserRole ? msg.isUserSender : !msg.isUserSender;
          if (isMyMessage && msg.messageStatus !== 'READ' && msg.messageStatus !== 'SEEN') {
            return { ...msg, messageStatus: 'SEEN' };
          }
        }

        if (msg.messageStatus === 'SENT' && update.status === 'RECEIVED') {
          return { ...msg, messageStatus: 'RECEIVED' };
        }
        return msg;
      }

      if (msg.messageId == update.messageId) { // Loose equality
        // Simple status update
        console.log(`🔄 Updating message ${msg.messageId} status to ${update.status}`);
        return {
          ...msg,
          messageStatus: update.status
        };
      }
      return msg;
    }));
  }, [currentUserId, isUserRole]);

  // Initialize WebSocket with callbacks
  const {
    connected,
    typingUsers,
    onlineStatus,
    sendTypingIndicator,
    sendMessage,
    markMessageAsReceived,
    markMessageAsRead,
    markAllMessagesAsRead
  } = useWebSocket(chatId, currentUserId, recipientId, userRole.replace('ROLE_', ''), _token, handleMessageReceived, handleStatusUpdate);

  const [allMessages, setAllMessages] = useState([]);

  // Auto-mark incoming messages as read when viewed
  useEffect(() => {
    if (allMessages.length > 0 && connected) {
      const lastMsg = allMessages[allMessages.length - 1];
      const isSender = isUserRole ? lastMsg.isUserSender : !lastMsg.isUserSender;
      const status = lastMsg.messageStatus?.toUpperCase();

      // If messages exist and I am reading them (connected), mark all as read?
      // Or just the last one?
      // Let's mark ALL as read when we have messages.
      if (!isSender && status !== 'READ' && status !== 'SEEN') {
        markAllMessagesAsRead(_token);
      }
    }
  }, [allMessages, connected, isUserRole, markAllMessagesAsRead, _token]);

  // Sync online status
  useEffect(() => {
    if (recipientId && onlineStatus) {
      const isOnline = !!onlineStatus[recipientId];
      // Only log if status changed to avoid spam? No, effect only runs on change.
      console.log(`👤 Recipient ${recipientId} is ${isOnline ? 'ONLINE' : 'OFFLINE'}`);
      setRecipientOnline(isOnline);
    }
  }, [recipientId, onlineStatus]);

  // Display error
  const showErrorMessage = (message) => {
    setError(message);
    setShowError(true);
    setTimeout(() => {
      setShowError(false);
      setTimeout(() => setError(null), 300);
    }, 4000);
  };

  // Load initial data
  useEffect(() => {
    if (!chatId) {
      isUserRole ? navigate('/my-tasks') : navigate('/tasker/my-tasks');
      return;
    }
    fetchChat();
    fetchRecipient();
    loadMessages(0, true);
  }, [chatId]);

  // Mark online / Join chat presence
  useEffect(() => {
    if (connected && chatId) {
      markAsRead();
      // If there was a join method, we would call it here.
      // For now, rely on connection + markAsRead.
    }
  }, [connected, chatId]);



  // Update recipient online status
  useEffect(() => {
    if (chatDetails) {
      const recipientId = isUserRole ? chatDetails.taskerId : chatDetails.userId;
      setRecipientOnline(onlineStatus[recipientId] === true);
    }
  }, [onlineStatus, chatDetails, isUserRole]);

  // Check if someone is typing
  useEffect(() => {
    setIsTyping(typingUsers.size > 0);
    scrollToBottom();
  }, [typingUsers]);

  // Auto-scroll to bottom for new messages
  useEffect(() => {
    if (allMessages.length > 0 && page === 0) {
      scrollToBottom();
    }
  }, [allMessages]);

  // Mark messages as read when chat opens
  const markAsRead = async () => {
    try {
      const endpoint = isUserRole
        ? `${API_BASE}/message/${chatId}/mark-read-user`
        : `${API_BASE}/message/${chatId}/mark-read-tasker`;

      const response = await fetch(endpoint, {
        method: 'PUT',
        headers: {
          'Authorization': `Bearer ${_token}`
        }
      });

      if (response.ok) {
        console.log('Messages marked as read via REST endpoint');
      } else {
        console.error('Failed to mark messages as read:', response.status);
      }
    } catch (error) {
      console.error('Error marking as read:', error);
    }
  };

  // Then your useEffect that calls markAsRead
  useEffect(() => {
    if (connected && chatId) {
      markAsRead();
    }
  }, [connected, chatId]);
  const fetchChat = async () => {
    try {
      const response = await fetch(`${API_BASE}/chat/getChat/${chatId}`, {
        headers: { 'Authorization': `Bearer ${_token}` }
      });
      if (response.ok) {
        const data = await response.json();
        setChatDetails(data);
      }
    } catch (error) {
      console.error('Error fetching chat:', error);
      showErrorMessage('Network error loading chat');
    }
  };

  const fetchRecipient = async () => {
    try {
      const endpoint = isUserRole
        ? `chat/user/getRecipientName/${chatId}`
        : `chat/tasker/getRecipientName/${chatId}`;

      const response = await fetch(`${API_BASE}/${endpoint}`, {
        headers: { 'Authorization': `Bearer ${_token}` }
      });

      if (response.ok) {
        const name = await response.text();
        setRecipientName(name);
      }
    } catch (error) {
      console.error('Error fetching recipient:', error);
    }
  };

  const loadMessages = async (pageNum, isInitial = false) => {
    if (loading) return;
    setLoading(true);

    try {
      const endpoint = isUserRole
        ? `${API_BASE}/chat/getHistory/user/${chatId}`
        : `${API_BASE}/chat/getHistory/tasker/${chatId}`;

      const response = await fetch(`${endpoint}?page=${pageNum}&size=20`, {
        headers: { 'Authorization': `Bearer ${_token}` }
      });

      if (response.ok) {
        const data = await response.json();
        const newMessages = data.messages || [];

        if (isInitial) {
          setAllMessages(newMessages.reverse());
        } else {
          setAllMessages(prev => [...newMessages.reverse(), ...prev]);
        }

        setHasMore(data.hasNext || false);
        setPage(pageNum);
      }
    } catch (error) {
      console.error('Error loading messages:', error);
      showErrorMessage('Network error loading messages');
    } finally {
      setLoading(false);
    }
  };

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  const handleScroll = (e) => {
    const { scrollTop } = e.target;
    if (scrollTop === 0 && hasMore && !loading) {
      const previousScrollHeight = messagesContainerRef.current.scrollHeight;
      loadMessages(page + 1).then(() => {
        const newScrollHeight = messagesContainerRef.current.scrollHeight;
        messagesContainerRef.current.scrollTop = newScrollHeight - previousScrollHeight;
      });
    }
  };

  const handleInputChange = (e) => {
    setInputMessage(e.target.value);

    // Send typing indicator
    sendTypingIndicator(true);

    // Clear previous timeout
    if (typingTimeoutRef.current) {
      clearTimeout(typingTimeoutRef.current);
    }

    // Stop typing indicator after 2 seconds of no typing
    typingTimeoutRef.current = setTimeout(() => {
      sendTypingIndicator(false);
    }, 2000);
  };

  const handleSendMessage = async () => {
    if (!inputMessage.trim() && !selectedImage) return;

    if (inputMessage.trim().length > MESSAGE_CHAR_LIMIT) {
      showErrorMessage(`Message exceeds ${MESSAGE_CHAR_LIMIT} character limit`);
      return;
    }

    const messageDto = {
      chatId: parseInt(chatId),
      content: inputMessage.trim(),
      isUserSender: isUserRole,
      messageStatus: 'sent',
      senderId: isUserRole ? chatDetails.userId : chatDetails.taskerId,
      receiverId: isUserRole ? chatDetails.taskerId : chatDetails.userId,
      timestamp: new Date().toISOString(),
      imageDto: selectedImage ? {
        fileData: selectedImage.data,
        fileName: selectedImage.name,
        fileFormat: selectedImage.format
      } : null
    };

    try {
      const endpoint = isUserRole
        ? `${API_BASE}/message/userSendMessage/${chatId}`
        : `${API_BASE}/message/taskerSendMessage/${chatId}`;

      const response = await fetch(endpoint, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${getToken()}`
        },
        body: JSON.stringify(messageDto)
      });

      if (response.ok) {
        const savedMessage = await response.json();  // Now expecting JSON
        setInputMessage('');
        setAllMessages(prev => {
          const exists = prev.some(msg => String(msg.messageId) === String(savedMessage.messageId));
          if (exists) return prev;
          return [...prev, savedMessage];
        });

        // Broadcast via WebSocket
        sendMessage(savedMessage);

        setSelectedImage(null);
        setImagePreview(null);
        sendTypingIndicator(false);
        scrollToBottom();
      } else {
        showErrorMessage('Failed to send message');
      }
    } catch (error) {
      console.error('Error sending message:', error);
      showErrorMessage('Network error');
    }
  };

  const handleCall = async () => {
    try {
      const endpoint = isUserRole
        ? `${API_BASE}/chat/callTasker/${chatId}`
        : `${API_BASE}/chat/callUser/${chatId}`;

      const response = await fetch(endpoint, {
        headers: { 'Authorization': `Bearer ${_token}` }
      });

      if (response.ok) {
        const phone = await response.text();
        setPhoneNumber(phone);
        setShowCallModal(true);
      }
    } catch (error) {
      console.error('Error getting phone:', error);
      showErrorMessage('Unable to place call');
    }
  };

  const copyPhoneToClipboard = async () => {
    if (!phoneNumber) return;
    try {
      await navigator.clipboard.writeText(phoneNumber);
      showErrorMessage('Phone number copied!');
    } catch (err) {
      console.warn('Copy failed', err);
      showErrorMessage('Failed to copy');
    }
  };

  const handleImageSelect = (e) => {
    const file = e.target.files[0];
    if (file) {
      if (file.size > 5 * 1024 * 1024) {
        showErrorMessage('Image size must be less than 5MB');
        return;
      }

      const reader = new FileReader();
      reader.onloadend = () => {
        const base64 = reader.result.split(',')[1];
        setSelectedImage({
          data: base64,
          name: file.name,
          format: file.type.split('/')[1]
        });
        setImagePreview(reader.result);
      };
      reader.readAsDataURL(file);
    }
  };

  const getStatusIcon = (status) => {
    const statusLower = status?.toLowerCase();
    switch (statusLower) {
      case 'seen':
        return (
          <svg style={{ width: '16px', height: '16px', color: '#3d8ca0ff' }} viewBox="0 0 24 24" fill="none" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 13l4 4L23 7" />
          </svg>
        );
      case 'received':
        return (
          <svg style={{ width: '16px', height: '16px', color: '#a7df2d' }} viewBox="0 0 24 24" fill="none" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 13l4 4L23 7" />
          </svg>
        );
      case 'sent':
        return (
          <svg style={{ width: '16px', height: '16px', color: '#bdc3c7' }} viewBox="0 0 24 24" fill="none" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
          </svg>
        );
      default:
        return null;
    }
  };

  const formatTime = (timestamp) => {
    // Ensure timestamp is treated as UTC if it doesn't have timezone info
    let timeStr = timestamp;
    if (typeof timeStr === 'string' && !timeStr.endsWith('Z') && !timeStr.includes('+')) {
      timeStr += 'Z';
    }
    const date = new Date(timeStr);
    const now = new Date();
    const diffInHours = (now - date) / (1000 * 60 * 60);

    if (diffInHours < 24) {
      return date.toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' });
    } else {
      return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric' }) + ' ' +
        date.toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' });
    }
  };

  // Styles (keeping the same styles from original)
  const styles = {
    container: {
      position: 'fixed',
      top: 0,
      left: 0,
      right: 0,
      bottom: 0,
      display: 'flex',
      flexDirection: 'column',
      height: '100vh',
      width: '100vw',
      background: 'linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%)',
      fontFamily: '-apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif',
      overflow: 'hidden',
      zIndex: 1000
    },
    header: {
      background: 'linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%)',
      boxShadow: '0 4px 12px rgba(0, 0, 0, 0.08)',
      padding: '1.25rem 1.5rem',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'space-between',
      borderBottom: '2px solid #a7df2d',
      zIndex: 100,
      flexShrink: 0
    },
    contentWrapper: {
      display: 'flex',
      flexDirection: 'column',
      flex: 1,
      overflow: 'hidden',
      minHeight: 0
    },
    headerLeft: {
      display: 'flex',
      alignItems: 'center',
      gap: '1rem'
    },
    backButton: {
      padding: '0.625rem',
      background: 'linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%)',
      border: '2px solid #e0e0e0',
      borderRadius: '12px',
      cursor: 'pointer',
      transition: 'all 0.3s ease',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center'
    },
    chatInfo: {
      display: 'flex',
      flexDirection: 'column',
      gap: '0.25rem'
    },
    chatTitle: {
      fontSize: '1.375rem',
      fontWeight: '700',
      color: '#2c3e50',
      margin: 0
    },
    statusIndicator: {
      fontSize: '0.8125rem',
      color: '#95a5a6',
      display: 'flex',
      alignItems: 'center',
      gap: '0.375rem'
    },
    onlineDot: {
      width: '8px',
      height: '8px',
      borderRadius: '50%',
      backgroundColor: '#2ecc71',
      animation: 'pulse 2s infinite'
    },
    offlineDot: {
      width: '8px',
      height: '8px',
      borderRadius: '50%',
      backgroundColor: '#95a5a6'
    },
    callButton: {
      padding: '0.875rem',
      background: 'linear-gradient(135deg, #a7df2d 0%, #95c927 100%)',
      color: '#ffffff',
      border: 'none',
      borderRadius: '16px',
      cursor: 'pointer',
      transition: 'all 0.3s ease',
      boxShadow: '0 4px 12px rgba(167, 223, 45, 0.3)',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center'
    },
    messagesContainer: {
      flex: 1,
      overflowY: 'auto',
      overflowX: 'hidden',
      padding: '1.5rem',
      display: 'flex',
      flexDirection: 'column',
      gap: '1rem',
      minHeight: 0
    },
    typingIndicator: {
      padding: '0.5rem 1rem',
      background: '#ffffff',
      borderRadius: '18px',
      alignSelf: 'flex-start',
      boxShadow: '0 2px 8px rgba(0, 0, 0, 0.05)',
      fontSize: '0.875rem',
      color: '#7f8c8d',
      fontStyle: 'italic'
    },
    messageWrapper: {
      display: 'flex'
    },
    messageContent: {
      maxWidth: '75%',
      display: 'flex',
      flexDirection: 'column'
    },
    messageBubble: {
      borderRadius: '18px',
      padding: '0.875rem 1.125rem',
      wordWrap: 'break-word',
      position: 'relative',
      transition: 'all 0.2s ease',
      maxWidth: '100%'
    },
    messageBubbleSender: {
      background: 'linear-gradient(135deg, #a7df2d 0%, #95c927 100%)',
      color: '#ffffff',
      boxShadow: '0 4px 12px rgba(167, 223, 45, 0.25)',
      borderBottomRightRadius: '4px'
    },
    messageBubbleReceiver: {
      background: '#ffffff',
      color: '#2c3e50',
      boxShadow: '0 4px 12px rgba(0, 0, 0, 0.08)',
      borderBottomLeftRadius: '4px',
      border: '1px solid rgba(167, 223, 45, 0.1)'
    },
    messageText: {
      fontSize: '0.9375rem',
      margin: 0,
      lineHeight: '1.5'
    },
    messageFooter: {
      display: 'flex',
      alignItems: 'center',
      gap: '0.375rem',
      marginTop: '0.375rem'
    },
    messageTime: {
      fontSize: '0.75rem',
      color: '#95a5a6',
      fontWeight: '500'
    },
    connectionIndicator: {
      position: 'fixed',
      bottom: '1rem',
      right: '1rem',
      padding: '0.5rem 1rem',
      borderRadius: '20px',
      fontSize: '0.8125rem',
      fontWeight: '500',
      boxShadow: '0 4px 12px rgba(0, 0, 0, 0.15)',
      zIndex: 1000,
      display: 'flex',
      alignItems: 'center',
      gap: '0.5rem'
    },
    connected: {
      background: '#2ecc71',
      color: '#ffffff'
    },
    disconnected: {
      background: '#e74c3c',
      color: '#ffffff'
    },
    inputContainer: {
      background: 'linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%)',
      borderTop: '2px solid #e9ecef',
      padding: '1.25rem 1.5rem',
      boxShadow: '0 -4px 12px rgba(0, 0, 0, 0.05)',
      flexShrink: 0
    },
    inputWrapper: {
      display: 'flex',
      alignItems: 'flex-end',
      gap: '0.75rem',
      background: '#ffffff',
      borderRadius: '24px',
      padding: '0.5rem',
      boxShadow: '0 4px 12px rgba(0, 0, 0, 0.08)',
      border: '2px solid #e9ecef'
    },
    imageButton: {
      padding: '0.75rem',
      background: 'linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%)',
      color: '#7f8c8d',
      border: 'none',
      borderRadius: '16px',
      cursor: 'pointer',
      transition: 'all 0.3s ease'
    },
    messageInput: {
      flex: 1,
      resize: 'none',
      border: 'none',
      padding: '0.75rem 0.5rem',
      fontSize: '0.9375rem',
      maxHeight: '8rem',
      outline: 'none',
      background: 'transparent',
      color: '#2c3e50'
    },
    sendButton: {
      padding: '0.875rem',
      background: 'linear-gradient(135deg, #a7df2d 0%, #95c927 100%)',
      color: '#ffffff',
      border: 'none',
      borderRadius: '16px',
      cursor: 'pointer',
      transition: 'all 0.3s ease',
      boxShadow: '0 4px 12px rgba(167, 223, 45, 0.3)'
    },
    charCounter: {
      fontSize: '0.75rem',
      color: '#95a5a6',
      padding: '0 0.5rem'
    },
    charCounterWarning: {
      color: '#e67e22'
    },
    charCounterError: {
      color: '#e74c3c',
      fontWeight: '600'
    },
    modalOverlay: {
      position: 'fixed',
      inset: 0,
      background: 'rgba(0,0,0,0.45)',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      zIndex: 1100
    },
    modal: {
      background: '#fff',
      borderRadius: '12px',
      padding: '1.25rem 1.5rem',
      width: 'min(420px, 90%)',
      boxShadow: '0 12px 40px rgba(0,0,0,0.2)',
      textAlign: 'center'
    },
    modalButtons: {
      display: 'flex',
      gap: '0.75rem',
      marginTop: '1.5rem',
      justifyContent: 'center'
    },
    modalButtonPrimary: {
      padding: '0.625rem 1.25rem',
      background: 'linear-gradient(135deg, #a7df2d 0%, #95c927 100%)',
      color: '#ffffff',
      border: 'none',
      borderRadius: '12px',
      cursor: 'pointer',
      fontWeight: '600'
    },
    modalButtonSecondary: {
      padding: '0.625rem 1.25rem',
      background: '#f8f9fa',
      color: '#2c3e50',
      border: '1px solid #e9ecef',
      borderRadius: '12px',
      cursor: 'pointer',
      fontWeight: '500'
    }
  };

  return (
    <div style={styles.container}>
      <style>
        {`
          @keyframes pulse {
            0%, 100% { opacity: 1; }
            50% { opacity: 0.5; }
          }
          body {
            overflow: hidden !important;
            position: fixed !important;
            width: 100% !important;
            height: 100% !important;
          }
        `}
      </style>

      {/* Header */}
      <div style={styles.header}>
        <div style={styles.headerLeft}>
          <button
            onClick={() => isUserRole ? navigate('/my-tasks') : navigate('/tasker/my-tasks')}
            style={styles.backButton}
          >
            <ArrowLeft style={{ width: '20px', height: '20px' }} />
          </button>
          <div style={styles.chatInfo}>
            <h1 style={styles.chatTitle}>{recipientName || 'Loading...'}</h1>
            <div style={styles.statusIndicator}>
              <div style={recipientOnline ? styles.onlineDot : styles.offlineDot}></div>
              <span>{recipientOnline ? 'Online' : 'Offline'}</span>
            </div>
          </div>
        </div>
        <button onClick={handleCall} style={styles.callButton}>
          <Phone style={{ width: '20px', height: '20px' }} />
        </button>
      </div>

      {/* Connection Status */}
      <div style={{
        ...styles.connectionIndicator,
        ...(connected ? styles.connected : styles.disconnected)
      }}>
        <div style={{
          width: '8px',
          height: '8px',
          borderRadius: '50%',
          backgroundColor: '#ffffff'
        }}></div>
        {connected ? 'Connected' : 'Connecting...'}
      </div>

      {/* Messages */}
      <div style={styles.contentWrapper}>
        <div
          ref={messagesContainerRef}
          onScroll={handleScroll}
          style={styles.messagesContainer}
        >
          {allMessages.map((msg, idx) => {
            const isSender = isUserRole ? msg.isUserSender : !msg.isUserSender;

            return (
              <div
                key={msg.messageId || idx}
                style={{
                  ...styles.messageWrapper,
                  justifyContent: isSender ? 'flex-end' : 'flex-start'
                }}
              >
                <div style={{
                  ...styles.messageContent,
                  alignItems: isSender ? 'flex-end' : 'flex-start'
                }}>
                  {msg.content && (
                    <div style={{
                      ...styles.messageBubble,
                      ...(isSender ? styles.messageBubbleSender : styles.messageBubbleReceiver)
                    }}>
                      <p style={styles.messageText}>{msg.content}</p>
                    </div>
                  )}
                  <div style={{
                    ...styles.messageFooter,
                    justifyContent: isSender ? 'flex-end' : 'flex-start'
                  }}>
                    <span style={styles.messageTime}>{formatTime(msg.timestamp)}</span>
                    {isSender && getStatusIcon(msg.messageStatus)
                    }
                  </div>
                </div>
              </div>
            );
          })}

          {/* Typing Indicator */}
          {isTyping && (
            <div style={styles.typingIndicator}>
              {recipientName} is typing...
            </div>
          )}

          <div ref={messagesEndRef} />
        </div>

        {/* Input */}
        <div style={styles.inputContainer}>
          <div style={styles.inputWrapper}>
            <input
              type="file"
              ref={fileInputRef}
              onChange={handleImageSelect}
              accept="image/*"
              style={{ display: 'none' }}
            />
            <button onClick={() => fileInputRef.current?.click()} style={styles.imageButton}>
              <Image style={{ width: '20px', height: '20px' }} />
            </button>
            <div style={{ flex: 1, display: 'flex', flexDirection: 'column' }}>
              <textarea
                value={inputMessage}
                onChange={handleInputChange}
                onKeyPress={(e) => {
                  if (e.key === 'Enter' && !e.shiftKey) {
                    e.preventDefault();
                    handleSendMessage();
                  }
                }}
                placeholder="Type a message..."
                style={styles.messageInput}
                rows="1"
              />
              {inputMessage.trim().length > 0 && (
                <div style={{
                  ...styles.charCounter,
                  ...(inputMessage.trim().length > MESSAGE_CHAR_LIMIT * 0.9
                    ? (inputMessage.trim().length > MESSAGE_CHAR_LIMIT
                      ? styles.charCounterError
                      : styles.charCounterWarning)
                    : {})
                }}>
                  {inputMessage.trim().length}/{MESSAGE_CHAR_LIMIT}
                </div>
              )}
            </div>
            <button
              onClick={handleSendMessage}
              disabled={!inputMessage.trim() && !selectedImage}
              style={{
                ...styles.sendButton,
                opacity: (!inputMessage.trim() && !selectedImage) ? 0.5 : 1
              }}
            >
              <Send style={{ width: '20px', height: '20px' }} />
            </button>
          </div>
        </div>
      </div>

      {/* Call Confirmation Modal */}
      {showCallModal && (
        <div style={styles.modalOverlay} onClick={() => setShowCallModal(false)}>
          <div style={styles.modal} onClick={(e) => e.stopPropagation()}>
            <h3 style={{ margin: 0, fontSize: '1.125rem' }}>Call {recipientName || 'Contact'}</h3>
            <p style={{ marginTop: '0.5rem', color: '#333', wordBreak: 'break-all', fontSize: '1.25rem', fontWeight: 'bold' }}>
              {phoneNumber || 'Loading...'}
            </p>
            <div style={styles.modalButtons}>
              <button style={styles.modalButtonPrimary} onClick={() => window.open(`tel:${phoneNumber}`)}>
                Call Now
              </button>
              <button style={styles.modalButtonSecondary} onClick={copyPhoneToClipboard}>
                Copy
              </button>
              <button style={styles.modalButtonSecondary} onClick={() => setShowCallModal(false)}>
                Close
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default ChatInterface;