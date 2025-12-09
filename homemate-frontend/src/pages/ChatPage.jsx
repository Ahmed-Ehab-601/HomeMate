/* Chat Interface Component - Fixed Header Version */
import React, { useState, useEffect, useRef } from 'react';
import { Send, Phone, Image, X, ArrowLeft } from 'lucide-react';
import { useAuth } from "../contexts/AuthContext";
import { useNavigate, useParams, useLocation } from 'react-router-dom';
const ChatInterface = () => {
  const [messages, setMessages] = useState([]);
  const [inputMessage, setInputMessage] = useState('');
  const [selectedImage, setSelectedImage] = useState(null);
  const [imagePreview, setImagePreview] = useState(null);
  const [page, setPage] = useState(0);
  const [hasMore, setHasMore] = useState(true);
  const [loading, setLoading] = useState(false);
  const [unreadCount, setUnreadCount] = useState(0);
  const [chatDetails, setChatDetails] = useState(null);
  const [recipientName, setRecipientName] = useState('');
  const [phoneNumber, setPhoneNumber] = useState(null);
  const [showCallModal, setShowCallModal] = useState(false);

  const messagesEndRef = useRef(null);
  const messagesContainerRef = useRef(null);
  const fileInputRef = useRef(null);
  const { getToken, getUserRole, isTasker, user } = useAuth();
const navigate = useNavigate();
const { chatId } = useParams();
const location = useLocation();
  // Safe JWT payload decoder. Returns null on failure.
  function decodeJwtPayload(token) {
    if (!token) return null;
    try {
      const parts = token.split('.');
      if (parts.length < 2) return null;
      const payload = parts[1];
      const b64 = payload.replace(/-/g, '+').replace(/_/g, '/');
      // Add padding if necessary
      const pad = b64.length % 4;
      const padded = pad ? b64 + '='.repeat(4 - pad) : b64;
      const decoded = atob(padded);
      const json = decodeURIComponent(
        decoded
          .split('')
          .map(c => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
          .join('')
      );
      return JSON.parse(json);
    } catch (err) {
      return null;
    }
  }

  // Determine the effective role: prefer token role (if present), otherwise fall back.
  const _token = getToken();
  const _decoded = decodeJwtPayload(_token);
  const _tokenRole = _decoded
    ? (_decoded.role ||
       (Array.isArray(_decoded.roles) ? _decoded.roles[0] : _decoded.roles) ||
       (Array.isArray(_decoded.authorities) ? _decoded.authorities[0] : _decoded.authorities) ||
       _decoded.authority)
    : null;

  const userRole = _tokenRole || getUserRole() || user?.role || 'ROLE_USER';
  const isUserRole = userRole === 'ROLE_USER';
  const isTaskerRole = userRole === 'ROLE_TASKER' || isTasker();
  const API_BASE = 'http://localhost:8080/api';

  useEffect(() => {
    if (!chatId) {
      isUserRole ? navigate('/my-tasks') : navigate('/tasker/my-tasks');
      return;
    }
    fetchChat();
    fetchRecipient();
    loadMessages(0, true);
    
    markAsRead();
  }, [chatId]);

  const fetchChat = async () => {
    try {
      const response = await fetch(`${API_BASE}/chat/getChat/${chatId}`, {
        headers: {
          'Authorization': `Bearer ${getToken()}`
        }
      });

      if (response.ok) {
        const chatDetails = await response.json();
        setChatDetails(chatDetails);
        console.log('Chat details loaded:', chatDetails);
      }
    } catch (error) {
      console.error('Error fetching chat:', error);
    } finally {
      setLoading(false);
    }
  };

  const fetchRecipient = async () => {
    try { 
      const endpoint = isUserRole
        ? `chat/user/getRecipientName/${chatId}`
        : `chat/tasker/getRecipientName/${chatId}`;
      
      console.log('Fetching recipient - Role:', userRole, 'isUserRole:', isUserRole, 'Endpoint:', endpoint);
      
      const response = await fetch(`${API_BASE}/${endpoint}`, {
        headers: {
          'Authorization': `Bearer ${getToken()}`
        }
      });

      if (response.ok) {
        const name = await response.text();
        setRecipientName(name);
        console.log('Recipient name loaded:', name);
      } else {
        console.error('Failed to fetch recipient name:', response.status);
      }
    } catch (error) {
      console.error('Error fetching recipient name:', error);
    }
  };

  useEffect(() => {
    if (messages.length > 0 && page === 0) {
      scrollToBottom();
    }
  }, [messages]);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  const loadMessages = async (pageNum, isInitial = false) => {
    if (loading) return;
    
    setLoading(true);
    try {
      console.log('Loading messages - Role:', userRole, 'isUserRole:', isUserRole);
      const endpoint = isUserRole
        ? `${API_BASE}/chat/getHistory/user/${chatId}`
        : `${API_BASE}/chat/getHistory/tasker/${chatId}`;
      
      console.log(`Loading messages - Page: ${pageNum}, Initial: ${isInitial}`);
      console.log('Endpoint:', endpoint);
      const response = await fetch(`${endpoint}?page=${pageNum}&size=20`, {
        headers: {
          'Authorization': `Bearer ${getToken()}`
        }
      });

      if (response.ok) {
        const data = await response.json();
        console.log('Messages loaded:', data);
        
        const newMessages = data.messages || [];
        
        if (isInitial) {
          setMessages(newMessages.reverse());
        } else {
          setMessages(prev => [...newMessages.reverse(), ...prev]);
        }
        
        setHasMore(data.hasNext || false);
        setPage(pageNum);
      } else {
        console.error('Failed to load messages:', response.status);
      }
    } catch (error) {
      console.error('Error loading messages:', error);
    } finally {
      setLoading(false);
    }
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

  const handleSendMessage = async () => {
    if (!inputMessage.trim() && !selectedImage) return;

    const messageDto = {
      chatId: chatId,
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
        const newMessage = {
          ...messageDto,
          messageId: Date.now(),
        };
        setMessages(prev => [...prev, newMessage]);
        setInputMessage('');
        setSelectedImage(null);
        setImagePreview(null);
        scrollToBottom();
      } else {
        alert('Failed to send message');
      }
    } catch (error) {
      console.error('Error sending message:', error);
      alert('Error sending message');
    }
  };

  // Updated: fetch the phone number then show modal rather than directly calling
  const handleCall = async () => {
    try {
      const endpoint = isUserRole
        ? `${API_BASE}/chat/callTasker/${chatId}`
        : `${API_BASE}/chat/callUser/${chatId}`;

      const response = await fetch(endpoint, {
        headers: {
          'Authorization': `Bearer ${getToken()}`
        }
      });

      if (response.ok) {
        const phone = await response.text();
        setPhoneNumber(phone);
        setShowCallModal(true);
      } else {
        alert('Unable to get phone number');
      }
    } catch (error) {
      console.error('Error getting phone number:', error);
      alert('Unable to place call at this time');
    }
  };

  const confirmCall = () => {
    if (!phoneNumber) return;
    window.open(`tel:${phoneNumber}`);
    setShowCallModal(false);
  };

  const copyPhoneToClipboard = async () => {
    if (!phoneNumber) return;
    try {
      await navigator.clipboard.writeText(phoneNumber);
      // small visual feedback could be added (toast/snack)
      console.log('Phone number copied to clipboard');
    } catch (err) {
      console.warn('Copy failed', err);
    }
  };

  const handleImageSelect = (e) => {
    const file = e.target.files[0];
    if (file) {
      if (file.size > 5 * 1024 * 1024) {
        alert('Image size should be less than 5MB');
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
  const markAsRead = async () => {
    try {
      const endpoint=isUserRole?`${API_BASE}/message/${chatId}/mark-read-user`:
      `${API_BASE}/message/${chatId}/mark-read-tasker`;
        console.log(endpoint);
      const response = await fetch(endpoint, {
        method: 'PUT',
        headers: {
          'Authorization': `Bearer ${getToken()}`
        }
      });
      
      if (response.ok) {
        setUnreadCount(0);
        console.log('Messages marked as read');
      } else {
        console.error('Failed to mark messages as read:', response.status);
      }
    } catch (error) {
      console.error('Error marking as read:', error);
    }
  };

  const getStatusIcon = (status) => {
    const statusLower = status?.toLowerCase();
    
    switch (statusLower) {
      case 'read':
      case 'seen':
        return (
          <svg style={{ width: '16px', height: '16px', color: '#3d8ca0ff' }} viewBox="0 0 24 24" fill="none" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 13l4 4L23 7" />
          </svg>
        );
      case 'received':
      case 'delivered':
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
    const date = new Date(timestamp);
    const now = new Date();
    const diffInHours = (now - date) / (1000 * 60 * 60);
    
    if (diffInHours < 24) {
      return date.toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' });
    } else {
      return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric' }) + ' ' +
             date.toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' });
    }
  };

  // Styles - UPDATED
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
      fontFamily: '-apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif',
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
      backgroundColor: '#ffffff',
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
      margin: 0,
      letterSpacing: '-0.5px'
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
    loadingSpinner: {
      textAlign: 'center',
      padding: '1rem'
    },
    spinner: {
      display: 'inline-block',
      width: '2rem',
      height: '2rem',
      border: '3px solid #f3f3f3',
      borderTop: '3px solid #a7df2d',
      borderRadius: '50%',
      animation: 'spin 0.8s linear infinite'
    },
    noMessages: {
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      height: '100%',
      color: '#95a5a6',
      fontSize: '1rem',
      fontWeight: '500'
    },
    messageWrapper: {
      display: 'flex'
    },
    messageContent: {
      maxWidth: '75%',
      display: 'flex',
      flexDirection: 'column'
    },
   messageImage: {
  borderRadius: '16px',
  marginBottom: '0.5rem',
  maxWidth: '100%',
  cursor: 'default', 
  transition: 'all 0.3s ease',
  boxShadow: '0 4px 12px rgba(0, 0, 0, 0.1)'
},
    messageBubble: {
      borderRadius: '18px',
      padding: '0.875rem 1.125rem',
      wordWrap: 'break-word',
      whiteSpace: 'pre-wrap',
      position: 'relative',
      transition: 'all 0.2s ease'
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
      marginTop: '0.375rem',
      padding: '0 0.25rem'
    },
    messageTime: {
      fontSize: '0.75rem',
      color: '#95a5a6',
      fontWeight: '500'
    },
    imagePreviewContainer: {
      background: 'linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%)',
      borderTop: '2px solid #e9ecef',
      padding: '1rem 1.5rem'
    },
    imagePreviewWrapper: {
      position: 'relative',
      display: 'inline-block'
    },
    imagePreview: {
      height: '6rem',
      borderRadius: '12px',
      boxShadow: '0 4px 12px rgba(0, 0, 0, 0.1)',
      border: '2px solid #e9ecef'
    },
    removeImageButton: {
      position: 'absolute',
      top: '-0.5rem',
      right: '-0.5rem',
      background: 'linear-gradient(135deg, #e74c3c 0%, #c0392b 100%)',
      color: '#ffffff',
      border: '2px solid #ffffff',
      borderRadius: '50%',
      padding: '0.375rem',
      cursor: 'pointer',
      transition: 'all 0.3s ease',
      boxShadow: '0 2px 8px rgba(231, 76, 60, 0.3)',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center'
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
      transition: 'all 0.3s ease',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center'
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
      color: '#2c3e50',
      lineHeight: '1.5'
    },
    sendButton: {
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

    /* Modal styles */
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
      gap: '0.5rem',
      marginTop: '1rem',
      justifyContent: 'center'
    },
    modalButtonPrimary: {
      padding: '0.6rem 1rem',
      background: '#2e7d32',
      color: '#fff',
      border: 'none',
      borderRadius: '8px',
      cursor: 'pointer'
    },
    modalButtonSecondary: {
      padding: '0.6rem 1rem',
      background: '#f1f1f1',
      color: '#222',
      border: 'none',
      borderRadius: '8px',
      cursor: 'pointer'
    }
  };

  return (
    <div style={styles.container}>
      <style>
        {`
          @keyframes spin {
            0% { transform: rotate(0deg); }
            100% { transform: rotate(360deg); }
          }
          
          /* Prevent body scroll when chat is open */
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
            onClick={() => {      isUserRole ? navigate('/my-tasks') : navigate('/tasker/my-tasks');}}
            style={styles.backButton}
            onMouseEnter={(e) => {
              e.currentTarget.style.background = 'linear-gradient(135deg, #a7df2d 0%, #95c927 100%)';
              e.currentTarget.style.borderColor = '#a7df2d';
              e.currentTarget.style.transform = 'translateX(-3px)';
            }}
            onMouseLeave={(e) => {
              e.currentTarget.style.background = 'linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%)';
              e.currentTarget.style.borderColor = '#e0e0e0';
              e.currentTarget.style.transform = 'translateX(0)';
            }}
          >
            <ArrowLeft style={{ width: '20px', height: '20px', color: '#666' }} />
          </button>
          <div style={styles.chatInfo}>
            <h1 style={styles.chatTitle}>
              {recipientName || 'Loading...'}
            </h1>
          </div>
        </div>
        <button
          onClick={handleCall}
          style={styles.callButton}
          onMouseEnter={(e) => {
            e.currentTarget.style.transform = 'translateY(-2px) scale(1.05)';
            e.currentTarget.style.boxShadow = '0 6px 20px rgba(167, 223, 45, 0.4)';
          }}
          onMouseLeave={(e) => {
            e.currentTarget.style.transform = 'translateY(0) scale(1)';
            e.currentTarget.style.boxShadow = '0 4px 12px rgba(167, 223, 45, 0.3)';
          }}
        >
          <Phone style={{ width: '20px', height: '20px' }} />
        </button>
      </div>

      {/* Content Wrapper */}
      <div style={styles.contentWrapper}>
        {/* Messages Container */}
        <div
          ref={messagesContainerRef}
          onScroll={handleScroll}
          style={styles.messagesContainer}
        >
          {loading && page > 0 && (
            <div style={styles.loadingSpinner}>
              <div style={styles.spinner}></div>
            </div>
          )}

          {messages.length === 0 && !loading && (
            <div style={styles.noMessages}>
              <p>No messages yet. Start the conversation!</p>
            </div>
          )}

          {messages.map((msg, idx) => {
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
                  {msg.imageDto && (
                    <img
                      src={`data:image/${msg.imageDto.fileFormat};base64,${msg.imageDto.fileData}`}
                      alt={msg.imageDto.fileName}
                      style={styles.messageImage}
                    />
                  )}
                  {msg.content && (
                    <div
                      style={{
                        ...styles.messageBubble,
                        ...(isSender ? styles.messageBubbleSender : styles.messageBubbleReceiver)
                      }}
                    >
                      <p style={styles.messageText}>{msg.content}</p>
                    </div>
                  )}
                  <div style={{
                    ...styles.messageFooter,
                    justifyContent: isSender ? 'flex-end' : 'flex-start'
                  }}>
                    <span style={styles.messageTime}>
                      {formatTime(msg.timestamp)}
                    </span>
                    {isSender && getStatusIcon(msg.messageStatus)}
                  </div>
                </div>
              </div>
            );
          })}
          <div ref={messagesEndRef} />
        </div>

        {/* Image Preview */}
        {imagePreview && (
          <div style={styles.imagePreviewContainer}>
            <div style={styles.imagePreviewWrapper}>
              <img src={imagePreview} alt="Preview" style={styles.imagePreview} />
              <button
                onClick={() => {
                  setSelectedImage(null);
                  setImagePreview(null);
                }}
                style={styles.removeImageButton}
                onMouseEnter={(e) => {
                  e.currentTarget.style.transform = 'scale(1.1) rotate(90deg)';
                  e.currentTarget.style.boxShadow = '0 4px 12px rgba(231, 76, 60, 0.4)';
                }}
                onMouseLeave={(e) => {
                  e.currentTarget.style.transform = 'scale(1) rotate(0deg)';
                  e.currentTarget.style.boxShadow = '0 2px 8px rgba(231, 76, 60, 0.3)';
                }}
              >
                <X style={{ width: '16px', height: '16px' }} />
              </button>
            </div>
          </div>
        )}

        {/* Input Area */}
        <div style={styles.inputContainer}>
          <div style={styles.inputWrapper}>
            <input
              type="file"
              ref={fileInputRef}
              onChange={handleImageSelect}
              accept="image/*"
              style={{ display: 'none' }}
            />
            <button
              onClick={() => fileInputRef.current?.click()}
              style={styles.imageButton}
              onMouseEnter={(e) => {
                e.currentTarget.style.background = 'linear-gradient(135deg, #a7df2d 0%, #95c927 100%)';
                e.currentTarget.style.color = '#ffffff';
                e.currentTarget.style.transform = 'scale(1.05)';
              }}
              onMouseLeave={(e) => {
                e.currentTarget.style.background = 'linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%)';
                e.currentTarget.style.color = '#7f8c8d';
                e.currentTarget.style.transform = 'scale(1)';
              }}
            >
              <Image style={{ width: '20px', height: '20px' }} />
            </button>
            <textarea
              value={inputMessage}
              onChange={(e) => setInputMessage(e.target.value)}
              onKeyPress={(e) => {
                if (e.key === 'Enter' && !e.shiftKey) {
                  e.preventDefault();
                  handleSendMessage();
                }
              }}
              placeholder="Type a message..."
              style={styles.messageInput}
              rows="1"
              onInput={(e) => {
                e.target.style.height = 'auto';
                e.target.style.height = e.target.scrollHeight + 'px';
              }}
            />
            <button
              onClick={handleSendMessage}
              disabled={!inputMessage.trim() && !selectedImage}
              style={{
                ...styles.sendButton,
                opacity: (!inputMessage.trim() && !selectedImage) ? 0.5 : 1,
                cursor: (!inputMessage.trim() && !selectedImage) ? 'not-allowed' : 'pointer'
              }}
              onMouseEnter={(e) => {
                if (inputMessage.trim() || selectedImage) {
                  e.currentTarget.style.transform = 'translateY(-2px) scale(1.05)';
                  e.currentTarget.style.boxShadow = '0 6px 20px rgba(167, 223, 45, 0.4)';
                }
              }}
              onMouseLeave={(e) => {
                e.currentTarget.style.transform = 'translateY(0) scale(1)';
                e.currentTarget.style.boxShadow = '0 4px 12px rgba(167, 223, 45, 0.3)';
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
            <p style={{ marginTop: '0.5rem', color: '#333', wordBreak: 'break-all' }}>
              {phoneNumber || 'Loading number...'}
            </p>

            <div style={styles.modalButtons}>
              <button style={styles.modalButtonPrimary} onClick={confirmCall}>
                Call
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