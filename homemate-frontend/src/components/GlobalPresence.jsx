import React from 'react';
import { useAuth } from '../contexts/AuthContext';
import { useWebSocket } from '../hooks/useWebSocket';

/**
 * GlobalPresence Component
 * Maintains a WebSocket connection while the user is logged in
 * to ensure they appear "Online" to other users.
 */
const GlobalPresence = () => {
    const { user, getToken, getUserRole } = useAuth();
    const token = getToken();
    const role = getUserRole() ? getUserRole().replace('ROLE_', '') : 'USER';
    const userId = user?.id;

    // Use the WebSocket hook without a chatId.
    // This will trigger the global subscriptions (My Status, Notifications)
    // and keep the STOMP connection alive.
    useWebSocket(
        null,      // chatId (null for global)
        userId,    // userId
        null,      // recipientId (null for global)
        role,      // role
        token,     // token
        null,      // onMessageReceived (not needed globally)
        null       // onStatusUpdate (not needed globally)
    );

    return null; // Render nothing
};

export default GlobalPresence;
