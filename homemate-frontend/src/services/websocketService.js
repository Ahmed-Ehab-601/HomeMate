import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

class WebSocketService {
    constructor() {
        this.client = null;
        this.subscriptions = new Map();
    }

    connect(token) {
        return new Promise((resolve, reject) => {
            if (this.client && this.client.connected) {
                resolve(this.client);
                return;
            }

            // Create SockJS instance - token will be sent in STOMP headers
            const socket = new SockJS('http://localhost:8080/HomeMate');

            this.client = new Client({
                webSocketFactory: () => socket,
                connectHeaders: {
                    Authorization: `Bearer ${token}`,
                },
                debug: (str) => {
                    console.log('[WebSocket Debug]', str);
                },
                reconnectDelay: 5000,
                heartbeatIncoming: 4000,
                heartbeatOutgoing: 4000,
                onConnect: () => {
                    console.log('✅ WebSocket connected successfully');
                    resolve(this.client);
                },
                onStompError: (frame) => {
                    console.error('❌ WebSocket STOMP error:', frame.headers['message']);
                    console.error('Details:', frame.body);
                    reject(new Error(frame.headers['message']));
                },
                onWebSocketError: (error) => {
                    console.error('❌ WebSocket error:', error);
                    reject(error);
                },
                onDisconnect: () => {
                    console.log('🔌 WebSocket disconnected');
                },
            });

            this.client.activate();
        });
    }

    subscribeToTask(taskId, callback) {
        if (!this.client || !this.client.connected) {
            console.error('WebSocket not connected. Cannot subscribe to task:', taskId);
            return null;
        }

        const destination = `/send/task/${taskId}`;

        // Unsubscribe if already subscribed
        if (this.subscriptions.has(taskId)) {
            this.subscriptions.get(taskId).unsubscribe();
        }

        const subscription = this.client.subscribe(destination, (message) => {
            try {
                const taskDto = JSON.parse(message.body);
                console.log('📨 Received task update:', taskDto);
                callback(taskDto);
            } catch (error) {
                console.error('Error parsing task update:', error);
            }
        });

        this.subscriptions.set(taskId, subscription);
        console.log(`👂 Subscribed to task updates: ${destination}`);

        return subscription;
    }

    unsubscribeFromTask(taskId) {
        if (this.subscriptions.has(taskId)) {
            this.subscriptions.get(taskId).unsubscribe();
            this.subscriptions.delete(taskId);
            console.log(`🔕 Unsubscribed from task: ${taskId}`);
        }
    }

    disconnect() {
        if (this.client) {
            // Unsubscribe from all subscriptions
            this.subscriptions.forEach((subscription) => subscription.unsubscribe());
            this.subscriptions.clear();

            this.client.deactivate();
            this.client = null;
            console.log('🔌 WebSocket disconnected and cleaned up');
        }
    }

    isConnected() {
        return this.client && this.client.connected;
    }
}

// Export singleton instance
export const websocketService = new WebSocketService();
