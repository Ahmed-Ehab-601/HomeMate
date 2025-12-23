// WebSocketContext.jsx - Fixed version

import { createContext, useContext, useEffect, useRef, useState, useCallback } from 'react';
import SockJS from 'sockjs-client';
import { Stomp } from '@stomp/stompjs';
import { useAuth } from './AuthContext';

const WebSocketContext = createContext(null);

export const WebSocketProvider = ({ children }) => {
    const { user, getToken, isAuthenticated } = useAuth();
    const [connected, setConnected] = useState(false);
    const [error, setError] = useState(null);
    const [onlineStatus, setOnlineStatus] = useState({});
    const clientRef = useRef(null);
    const subscriptionsRef = useRef([]);
    const reconnectTimeoutRef = useRef(null);
    const reconnectAttempts = useRef(0);
    const heartbeatIntervalRef = useRef(null);

    const WEBSOCKET_URL = 'http://localhost:8080/HomeMate';
    const MAX_RECONNECT_ATTEMPTS = 5;
    const RECONNECT_DELAY_BASE = 1000;
    const MAX_RECONNECT_DELAY = 30000;
    const HEARTBEAT_INTERVAL = 15000;

    const token = getToken();
    const userId = user?.id;
    const rawRole = user?.role;
    const role = rawRole ? rawRole.replace('ROLE_', '') : null;

    console.log('🔍 [Global] WebSocketContext initialized with:', {
        userId,
        rawRole,
        role,
        isAuthenticated,
        hasToken: !!token
    });

    const sendPresenceUpdate = useCallback((status) => {
        if (!clientRef.current?.connected || !userId) {
            console.warn('⚠️ [Global] Cannot send presence:', {
                connected: clientRef.current?.connected,
                userId,
                role
            });
            return;
        }

        try {
            const presenceData = {
                Id: Number(userId),
                UserType: role.toLowerCase(),
                onlineStatus: status,
                time: null
            };

            console.log(`📡 [Global] Sending presence update:`, presenceData);

            clientRef.current.send(
                '/app/presence/update',
                {},
                JSON.stringify(presenceData)
            );
            console.log(`✅ [Global] Presence update sent: ${status}`);
        } catch (err) {
            console.error('❌ [Global] Error sending presence:', err);
        }
    }, [userId, role]);

    const sendHeartbeat = useCallback(() => {
        if (!clientRef.current?.connected || !userId) return;

        try {
            clientRef.current.send(
                '/app/presence/heartbeat',
                {},
                JSON.stringify({
                    Id: Number(userId),
                    UserType: role.toLowerCase(),
                    userId: Number(userId),
                    userType: role.toLowerCase()
                })
            );
            console.debug('💓 [Global] Heartbeat sent for', role, userId);
        } catch (err) {
            console.error('❌ [Global] Error sending heartbeat:', err);
        }
    }, [userId, role]);

    const startHeartbeat = useCallback(() => {
        if (heartbeatIntervalRef.current) clearInterval(heartbeatIntervalRef.current);
        sendHeartbeat();
        heartbeatIntervalRef.current = setInterval(sendHeartbeat, HEARTBEAT_INTERVAL);
        console.log('💓 [Global] Heartbeat started for', role, userId);
    }, [sendHeartbeat, role, userId]);

    const stopHeartbeat = useCallback(() => {
        if (heartbeatIntervalRef.current) {
            clearInterval(heartbeatIntervalRef.current);
            heartbeatIntervalRef.current = null;
            console.log('💓 [Global] Heartbeat stopped');
        }
    }, []);

    // FIXED: Fetch both users AND taskers online status
    const fetchOnlineUsers = useCallback(async () => {
        try {
            console.log('🌐 [Global] Fetching initial online users list from /api/chat/online...');
            const response = await fetch('http://localhost:8080/api/chat/online', {
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });

            if (response.ok) {
                const data = await response.json();
                console.log('👥 [Global] Fetched online status:', data);

                // Combine both users and taskers into one map
                const combinedStatus = {};
                
                // Add all users
                if (data.users) {
                    Object.keys(data.users).forEach(key => {
                        combinedStatus[String(key)] = data.users[key];
                    });
                }
                
                // Add all taskers
                if (data.taskers) {
                    Object.keys(data.taskers).forEach(key => {
                        combinedStatus[String(key)] = data.taskers[key];
                    });
                }

                console.log('📊 [Global] Combined online status:', combinedStatus);

                setOnlineStatus(prev => ({
                    ...prev,
                    ...combinedStatus
                }));
            } else {
                console.warn('⚠️ [Global] Failed to fetch online users:', response.status);
            }
        } catch (error) {
            console.error('❌ [Global] Error fetching online users:', error);
        }
    }, [token]);

    const connect = useCallback(() => {
        if (clientRef.current?.connected) {
            console.log('⏸️ [Global] Already connected');
            return;
        }

        if (!isAuthenticated || !userId) {
            console.log('⏸️ [Global] Skipping connection:', {
                isAuthenticated,
                userId,
                role
            });
            return;
        }

        console.log('🔌 [Global] Connecting WebSocket for', role, userId);

        const socket = new SockJS(WEBSOCKET_URL);
        const client = Stomp.over(() => socket);

        client.reconnectDelay = 5000;
        client.heartbeatIncoming = 4000;
        client.heartbeatOutgoing = 4000;
        client.debug = (msg) => {
            // console.log('[STOMP]', msg);
        };

        const headers = {
            Authorization: `Bearer ${token}`,
            userId: role === 'USER' ? String(userId) : '',
            taskerId: role === 'TASKER' ? String(userId) : '',
            role: role
        };

        console.log('🔑 [Global] Connecting with headers:', headers);

        client.connect(
            headers,
            () => {
                console.log('✅ [Global] WebSocket Connected for', role, userId);
                setConnected(true);
                setError(null);
                reconnectAttempts.current = 0;
                clientRef.current = client;

                subscriptionsRef.current.forEach(sub => {
                    try { sub.unsubscribe(); } catch (e) { }
                });
                subscriptionsRef.current = [];

                console.log('📡 [Global] Subscribing to global channels...');

                // Subscribe to presence
                try {
                    const presenceSub = client.subscribe('/send/presence', (message) => {
                        try {
                            const body = message.body;
                            console.log('📨 [Global] Raw presence message received:', body);

                            const update = JSON.parse(body);
                            const targetId = String(update.Id);
                            const isOnline = update.onlineStatus === 'ONLINE';

                            console.log(`👤 [Global] Presence update received:`, {
                                userId: targetId,
                                userType: update.UserType,
                                status: isOnline ? 'ONLINE' : 'OFFLINE',
                                timestamp: update.time
                            });

                            setOnlineStatus(prev => {
                                const newStatus = { ...prev, [targetId]: isOnline };
                                return newStatus;
                            });
                        } catch (e) {
                            console.error('❌ [Global] Error parsing presence:', e);
                        }
                    });
                    subscriptionsRef.current.push(presenceSub);
                    console.log('✅ [Global] Subscribed to /send/presence');
                } catch (err) {
                    console.error('❌ [Global] Failed to subscribe to presence:', err);
                }

                // Subscribe to notifications
                try {
                    const notificationPath = `/send/notifications/${role.toLowerCase()}/${userId}`;
                    console.log('📡 [Global] Subscribing to notifications:', notificationPath);

                    const notifSub = client.subscribe(
                        notificationPath,
                        (message) => {
                            console.log('🔔 [Global] Notification:', JSON.parse(message.body));
                        }
                    );
                    subscriptionsRef.current.push(notifSub);
                    console.log('✅ [Global] Subscribed to notifications');
                } catch (err) {
                    console.error('❌ [Global] Failed to subscribe to notifications:', err);
                }

                // IMPORTANT: Fetch online users BEFORE sending presence
                setTimeout(() => {
                    console.log('🚀 [Global] Fetching online users then sending ONLINE status');
                    fetchOnlineUsers().then(() => {
                        sendPresenceUpdate('ONLINE');
                        startHeartbeat();
                    });
                }, 500);
            },
            (err) => {
                console.error('❌ [Global] WS Connection Error:', err);
                setConnected(false);
                setError(err.message);
                stopHeartbeat();
                handleReconnect();
            }
        );
    }, [isAuthenticated, userId, role, token, sendPresenceUpdate, startHeartbeat, stopHeartbeat, fetchOnlineUsers]);

    const handleReconnect = useCallback(() => {
        if (reconnectAttempts.current >= MAX_RECONNECT_ATTEMPTS) {
            console.error('❌ [Global] Max reconnect attempts reached');
            return;
        }
        reconnectAttempts.current += 1;
        const delay = Math.min(
            RECONNECT_DELAY_BASE * Math.pow(2, reconnectAttempts.current),
            MAX_RECONNECT_DELAY
        );
        console.log(`🔄 [Global] Reconnecting in ${delay}ms... (attempt ${reconnectAttempts.current})`);
        reconnectTimeoutRef.current = setTimeout(connect, delay);
    }, [connect]);

    const disconnect = useCallback(() => {
        console.log('🔌 [Global] Disconnecting...');
        stopHeartbeat();

        if (clientRef.current?.connected) {
            sendPresenceUpdate('OFFLINE');

            subscriptionsRef.current.forEach(sub => {
                try { sub.unsubscribe(); } catch (e) { }
            });
            subscriptionsRef.current = [];

            clientRef.current.disconnect(() => {
                console.log('✅ [Global] Disconnected');
                setConnected(false);
            });
        }

        clientRef.current = null;
        setOnlineStatus({});

        if (reconnectTimeoutRef.current) {
            clearTimeout(reconnectTimeoutRef.current);
        }
    }, [sendPresenceUpdate, stopHeartbeat]);

    useEffect(() => {
        if (isAuthenticated && userId) {
            connect();
        } else {
            disconnect();
        }

        return () => {
            if (clientRef.current?.connected) {
                disconnect();
            }
        };
    }, [isAuthenticated, userId]);

    const value = {
        connected,
        error,
        onlineStatus,
        client: clientRef.current,
        sendPresenceUpdate,
        sendHeartbeat,
        userId,
        role
    };

    return (
        <WebSocketContext.Provider value={value}>
            {children}
        </WebSocketContext.Provider>
    );
};

export const useGlobalWebSocket = () => {
    const context = useContext(WebSocketContext);
    if (!context) {
        throw new Error('useGlobalWebSocket must be used within a WebSocketProvider');
    }
    return context;
};