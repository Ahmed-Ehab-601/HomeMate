import React, { useState, useEffect, useRef, useCallback } from 'react';
import { Send, Phone, Image, X } from 'lucide-react';
import { useAuth } from "../contexts/AuthContext";
import { useNavigate, useParams } from 'react-router-dom';
import { useWebSocket } from '../hooks/useWebSocket';
import { uploadToCloudinary } from '../utils/cloudinary';
import { baseUrl } from '../utils/apiClient';
import { useUnread } from '../contexts/UnreadContext'; // ADD THIS IMPORT

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
  const [expandedImage, setExpandedImage] = useState(null);

  const messagesEndRef = useRef(null);
  const messagesContainerRef = useRef(null);
  const fileInputRef = useRef(null);
  const typingTimeoutRef = useRef(null);

  const { getToken, getUserRole, user } = useAuth();
  const { refreshUnread } = useUnread(); // ADD THIS
  const navigate = useNavigate();
  const { chatId } = useParams();

  const MESSAGE_CHAR_LIMIT = 1000;
  const API_BASE = baseUrl + '/api';

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

  const [allMessages, setAllMessages] = useState([]);

  // Initialize WebSocket
  const handleMessageReceived = useCallback((newMessage) => {
    setAllMessages(prev => {
      const exists = prev.some(msg => String(msg.messageId) === String(newMessage.messageId));
      if (exists) return prev;

      if (newMessage.imageDto && !newMessage.imageDto.fileData) {
        console.log('🖼️ Received lightweight image message, fetching details...');
        loadMessages(0, true);
        return prev;
      }

      return [...prev, newMessage];
    });
    
    // Refresh unread count when new message arrives
    refreshUnread();
  }, [refreshUnread]);

  const recipientId = chatDetails
    ? (isUserRole ? chatDetails.taskerId : chatDetails.userId)
    : null;

  const handleStatusUpdate = useCallback((update) => {
    console.log('📊 Status update received:', update);
    
    setAllMessages(prev => prev.map(msg => {
      // Handle READ receipts
      if (update.type === 'READ_RECEIPT') {
        if (String(update.readBy) !== String(currentUserId)) {
          if (msg.isUserSender === isUserRole) {
            if (msg.messageStatus !== 'SEEN' && msg.messageStatus !== 'READ') {
              return { ...msg, messageStatus: 'SEEN' };
            }
          }
        }
        return msg;
      }

      // Handle BULK updates OR individual RECEIVED updates
      if (update.status === 'RECEIVED') {
        const isMyMessage = isUserRole ? msg.isUserSender : !msg.isUserSender;
        
        if (isMyMessage && msg.messageStatus === 'SENT') {
          console.log(`  ✓ Updating message ${msg.messageId}: SENT -> RECEIVED`);
          return { ...msg, messageStatus: 'RECEIVED' };
        }
      }

      // Handle bulk READ updates
      if (update.bulkUpdate && update.status === 'READ') {
        const isMyMessage = isUserRole ? msg.isUserSender : !msg.isUserSender;
        if (isMyMessage && msg.messageStatus !== 'READ' && msg.messageStatus !== 'SEEN') {
          return { ...msg, messageStatus: 'SEEN' };
        }
      }

      // Handle INDIVIDUAL message status updates
      if (msg.messageId == update.messageId) {
        console.log(`🔍 Updating message ${msg.messageId} status to ${update.status}`);
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
    markAllMessagesAsRead
  } = useWebSocket(
    chatId,
    currentUserId,
    recipientId,
    userRole.replace('ROLE_', ''),
    _token,
    handleMessageReceived,
    handleStatusUpdate
  );

  const removeSelectedImage = () => {
    setSelectedImage(null);
    setImagePreview(null);
    if (fileInputRef.current) {
      fileInputRef.current.value = '';
    }
  };

  // Auto-mark incoming messages as read when viewed
  useEffect(() => {
    if (allMessages.length > 0 && connected) {
      const lastMsg = allMessages[allMessages.length - 1];
      const isSender = isUserRole ? lastMsg.isUserSender : !lastMsg.isUserSender;
      const status = lastMsg.messageStatus?.toUpperCase();

      if (!isSender && status !== 'READ' && status !== 'SEEN') {
        markAllMessagesAsRead(_token);
        // Refresh unread count after marking as read
        refreshUnread();
      }
    }
  }, [allMessages, connected, isUserRole, markAllMessagesAsRead, _token, refreshUnread]);

  // Sync online status from WebSocket
  useEffect(() => {
    if (recipientId && onlineStatus) {
      const recipientIdStr = String(recipientId);
      const isOnline = !!onlineStatus[recipientIdStr];
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

  // Mark as read when connected
  useEffect(() => {
    if (connected && chatId) {
      markAsRead();
    }
  }, [connected, chatId]);

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

  // Refresh unread count when chat opens
  useEffect(() => {
    if (chatId) {
      // Small delay to ensure backend has processed the mark-as-read
      const timer = setTimeout(() => {
        refreshUnread();
      }, 500);
      return () => clearTimeout(timer);
    }
  }, [chatId, refreshUnread]);

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
        console.log('✅ Messages marked as read via REST endpoint');
        // Refresh unread count after successfully marking as read
        setTimeout(() => refreshUnread(), 300);
      } else {
        console.error('❌ Failed to mark messages as read:', response.status);
      }
    } catch (error) {
      console.error('❌ Error marking as read:', error);
    }
  };

  const fetchChat = async () => {
    try {
      const response = await fetch(`${API_BASE}/chat/getChat/${chatId}`, {
        headers: { 'Authorization': `Bearer ${_token}` }
      });
      if (response.ok) {
        const data = await response.json();
        console.log('🛠 Debug: chatDetails:', data);
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

    sendTypingIndicator(true);

    if (typingTimeoutRef.current) {
      clearTimeout(typingTimeoutRef.current);
    }

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

    let imageUrl = null;
    if (selectedImage) {
      try {
        const blob = await fetch(imagePreview).then(r => r.blob());
        const file = new File([blob], selectedImage.name, { type: `image/${selectedImage.format}` });
        imageUrl = await uploadToCloudinary(file, currentUserId);
      } catch (error) {
        showErrorMessage('Failed to upload image');
        return;
      }
    }

    const messageDto = {
      chatId: parseInt(chatId),
      content: inputMessage.trim(),
      isUserSender: isUserRole,
      messageStatus: 'sent',
      senderId: isUserRole ? chatDetails.userId : chatDetails.taskerId,
      receiverId: isUserRole ? chatDetails.taskerId : chatDetails.userId,
      timestamp: new Date().toISOString(),
      imageDto: imageUrl ? {
        fileData: imageUrl,
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
        const savedMessage = await response.json();
        setInputMessage('');
        setAllMessages(prev => {
          const exists = prev.some(msg => String(msg.messageId) === String(savedMessage.messageId));
          if (exists) return prev;
          return [...prev, savedMessage];
        });

        sendMessage(savedMessage);
        const wsMessage = {
          ...savedMessage,
          imageDto: savedMessage.imageDto ? {
            ...savedMessage.imageDto,
            fileData: null
          } : null
        };
        sendMessage(wsMessage);

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
      } else {
        const errorText = await response.text();
        try {
          const errorJson = JSON.parse(errorText);
          showErrorMessage(errorJson.message || errorText || 'Unable to place call');
        } catch (e) {
          showErrorMessage(errorText || 'Unable to place call');
        }
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
      if (file.size > 16 * 1024 * 1024) {
        showErrorMessage('Image size must be less than 16MB');
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
    messageImage: {
      maxWidth: '100%',
      maxHeight: '300px',
      borderRadius: '12px',
      marginTop: '0.5rem',
      cursor: 'pointer',
      objectFit: 'cover',
      display: 'block'
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
    inputContainer: {
      background: 'linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%)',
      borderTop: '2px solid #e9ecef',
      padding: '1.25rem 1.5rem',
      boxShadow: '0 -4px 12px rgba(0, 0, 0, 0.05)',
      flexShrink: 0
    },
    imagePreviewContainer: {
      position: 'relative',
      marginBottom: '0.75rem',
      display: 'inline-block'
    },
    imagePreview: {
      maxWidth: '150px',
      maxHeight: '150px',
      borderRadius: '12px',
      boxShadow: '0 2px 8px rgba(0, 0, 0, 0.1)'
    },
    removeImageButton: {
      position: 'absolute',
      top: '-8px',
      right: '-8px',
      background: '#e74c3c',
      color: '#ffffff',
      border: 'none',
      borderRadius: '50%',
      width: '24px',
      height: '24px',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      cursor: 'pointer',
      boxShadow: '0 2px 4px rgba(0, 0, 0, 0.2)'
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
    },
    imageModal: {
      background: 'transparent',
      padding: '2rem',
      maxWidth: '90vw',
      maxHeight: '90vh',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center'
    },
    expandedImage: {
      maxWidth: '100%',
      maxHeight: '80vh',
      borderRadius: '8px',
      boxShadow: '0 8px 32px rgba(0, 0, 0, 0.3)'
    },
    errorToast: {
      position: 'fixed',
      top: '5rem',
      left: '50%',
      transform: 'translateX(-50%)',
      backgroundColor: '#e74c3c',
      color: 'white',
      padding: '0.75rem 1.5rem',
      borderRadius: '24px',
      zIndex: 2000,
      boxShadow: '0 4px 12px rgba(231, 76, 60, 0.3)',
      fontWeight: '500',
      fontSize: '0.9375rem',
      animation: 'slideDown 0.3s ease'
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
          @keyframes slideDown {
            from { transform: translate(-50%, -100%); opacity: 0; }
            to { transform: translate(-50%, 0); opacity: 1; }
          }
        `}
      </style>

      {/* Header */}
      <div style={styles.header}>
        <div style={styles.headerLeft}>
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

      {/* Error Toast */}
      {showError && error && (
        <div style={styles.errorToast}>
          {error}
        </div>
      )}

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
                  {msg.imageDto && msg.imageDto.fileData && (
                    <img
                      src={msg.imageDto.fileData}
                      alt={msg.imageDto.fileName || 'Image'}
                      style={styles.messageImage}
                      onClick={() => setExpandedImage(msg.imageDto.fileData)}
                    />
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
          {/* ADD THIS IMAGE PREVIEW */}
          {imagePreview && (
            <div style={styles.imagePreviewContainer}>
              <img src={imagePreview} alt="Preview" style={styles.imagePreview} />
              <button onClick={removeSelectedImage} style={styles.removeImageButton}>
                <X style={{ width: '16px', height: '16px' }} />
              </button>
            </div>
          )}

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
      {/* ADD THIS IMAGE MODAL */}
      {expandedImage && (
        <div style={styles.modalOverlay} onClick={() => setExpandedImage(null)}>
          <div style={styles.imageModal} onClick={(e) => e.stopPropagation()}>
            <img src={expandedImage} alt="Full size" style={styles.expandedImage} />
          </div>
        </div>
      )}
    </div>
  );
}
  ;

export default ChatInterface;