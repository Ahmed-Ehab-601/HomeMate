package com.homemate.chat.config;

import com.homemate.chat.Service.ChatService;
import com.homemate.chat.Service.MessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WebSocketEventListener {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketEventListener.class);

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private ChatService chatService;

    @Autowired
    private MessageService messageService;

    // Store active sessions: sessionId -> userId/taskerId
    private final Map<String, Long> activeSessions = new ConcurrentHashMap<>();
    private final Map<String, String> sessionRoles = new ConcurrentHashMap<>(); // sessionId -> role (USER/TASKER)

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();

        // Extract user information from headers
        String userId = headerAccessor.getFirstNativeHeader("userId");
        String taskerId = headerAccessor.getFirstNativeHeader("taskerId");
        String role = headerAccessor.getFirstNativeHeader("role");

        logger.info("🔌 New WebSocket Connection - SessionId: {}, UserId: {}, TaskerId: {}, Role: {}",
                sessionId, userId, taskerId, role);

        try {
            if (userId != null && !userId.isEmpty()) {
                Long userIdLong = Long.parseLong(userId);
                activeSessions.put(sessionId, userIdLong);
                sessionRoles.put(sessionId, "USER");

                // Set user online
                chatService.setUserOnline(userIdLong);
                messageService.markAllAsReceivedForUser(userIdLong);

                // Broadcast online status
                broadcastOnlineStatus(userIdLong, "USER", true);

                logger.info("✅ User {} is now online", userIdLong);
            }
            else if (taskerId != null && !taskerId.isEmpty()) {
                Long taskerIdLong = Long.parseLong(taskerId);
                activeSessions.put(sessionId, taskerIdLong);
                sessionRoles.put(sessionId, "TASKER");

                // Set tasker online
                chatService.setTaskerOnline(taskerIdLong);
                messageService.markAllAsReceivedForTasker(taskerIdLong);

                // Broadcast online status
                broadcastOnlineStatus(taskerIdLong, "TASKER", true);

                logger.info("✅ Tasker {} is now online", taskerIdLong);
            }
        } catch (Exception e) {
            logger.error("❌ Error handling WebSocket connection: {}", e.getMessage(), e);
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();

        logger.info("🔌 WebSocket Disconnection - SessionId: {}", sessionId);

        try {
            Long userId = activeSessions.remove(sessionId);
            String role = sessionRoles.remove(sessionId);

            if (userId != null && role != null) {
                if ("USER".equals(role)) {
                    chatService.changeStatusUser(userId);
                    broadcastOnlineStatus(userId, "USER", false);
                    logger.info("👋 User {} disconnected and set offline", userId);
                }
                else if ("TASKER".equals(role)) {
                    chatService.changeStatusTasker(userId);
                    broadcastOnlineStatus(userId, "TASKER", false);
                    logger.info("👋 Tasker {} disconnected and set offline", userId);
                }
            }
        } catch (Exception e) {
            logger.error("❌ Error handling WebSocket disconnection: {}", e.getMessage(), e);
        }
    }

    private void broadcastOnlineStatus(Long id, String role, boolean isOnline) {
        try {
            Map<String, Object> statusUpdate = new HashMap<>();

            if ("USER".equals(role)) {
                statusUpdate.put("userId", id);
            } else {
                statusUpdate.put("taskerId", id);
            }

            statusUpdate.put("role", role);
            statusUpdate.put("status", isOnline ? "online" : "offline");
            statusUpdate.put("timestamp", System.currentTimeMillis());

            // Broadcast to all subscribers of this user's status
            messagingTemplate.convertAndSend("/topic/status/" + id, statusUpdate);

            logger.debug("📡 Broadcasted status update: {}", statusUpdate);
        } catch (Exception e) {
            logger.error("❌ Error broadcasting online status: {}", e.getMessage(), e);
        }
    }

    // Utility method to check if user is online
    public boolean isUserOnline(Long userId, String role) {
        return activeSessions.values().stream()
                .anyMatch(id -> id.equals(userId)) &&
                sessionRoles.values().stream()
                        .anyMatch(r -> r.equals(role));
    }

    // Get all active sessions
    public Map<String, Long> getActiveSessions() {
        return new HashMap<>(activeSessions);
    }

    // Get session count
    public int getActiveSessionCount() {
        return activeSessions.size();
    }
}