package com.homemate.chat.controller;

import com.homemate.chat.Enum.OnlineStatus;
import com.homemate.chat.Service.MessageService;
import com.homemate.chat.Service.PresenceService;
import com.homemate.chat.dto.MessageDto;
import com.homemate.chat.dto.PresenceUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.HashMap;
import java.util.Map;

/**
 * WebSocket controller for real-time messaging
 * Handles message sending, delivery, and read receipts
 */
@Controller
public class WebSocketMessageController {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketMessageController.class);

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private PresenceService presenceService;

    @Autowired
    private MessageService messageService;

    /**
     * Handle incoming messages from clients
     * Route: /app/chat/{chatId}/send
     * Clients subscribe to: /send/chat/{chatId}
     */
    @MessageMapping("/chat/{chatId}/send")
    public void sendMessage(
            @DestinationVariable Long chatId,
            @Payload MessageDto messageDto) {
        try {
            logger.debug("📨 Sending message to chat {}", chatId);

            messagingTemplate.convertAndSend(
                    "/send/chat/" + chatId,
                    messageDto
            );

            Long recipientId = messageDto.getReceiverId();
            String recipientRole = messageDto.isIsUserSender() ? "TASKER" : "USER";

            Map<String, Object> notification = new HashMap<>();
            notification.put("type", "NEW_MESSAGE");
            notification.put("chatId", chatId);
            notification.put("messageId", messageDto.getMessageId());
            notification.put("senderId", messageDto.getSenderId());
            notification.put("preview", messageDto.getContent());
            notification.put("timestamp", messageDto.getTimestamp());

            messagingTemplate.convertAndSend(
                    "/send/notifications/" + recipientRole.toLowerCase() + "/" + recipientId,
                    notification
            );

            logger.info("✅ Message sent successfully to chat {}", chatId);

        } catch (Exception e) {
            logger.error("❌ Failed to send message to chat {}: {}", chatId, e.getMessage(), e);

            Map<String, Object> error = new HashMap<>();
            error.put("type", "ERROR");
            error.put("message", "Failed to send message");
            error.put("chatId", chatId);

            messagingTemplate.convertAndSend(
                    "/send/errors/" + messageDto.getSenderId(),
                    error
            );
        }
    }

    @MessageMapping("/chat/{chatId}/status")
    public void updateMessageStatus(
            @DestinationVariable Long chatId,
            @Payload Map<String, Object> statusUpdate) {

        try {
            String status = (String) statusUpdate.get("status");
            Long messageId = Long.valueOf(statusUpdate.get("messageId").toString());

            Map<String, Object> statusBroadcast = new HashMap<>();
            statusBroadcast.put("messageId", messageId);
            statusBroadcast.put("status", status);
            statusBroadcast.put("timestamp", System.currentTimeMillis());

            messagingTemplate.convertAndSend(
                    "/send/chat/" + chatId + "/status",
                    statusBroadcast
            );

            logger.debug("📊 Message {} status updated to {} in chat {}", messageId, status, chatId);

        } catch (Exception e) {
            logger.error("❌ Failed to update message status in chat {}: {}", chatId, e.getMessage(), e);
        }
    }

    @MessageMapping("/chat/{chatId}/typing")
    public void handleTypingIndicator(
            @DestinationVariable Long chatId,
            @Payload Map<String, Object> typingData) {

        try {
            Long userId = Long.valueOf(typingData.get("userId").toString());
            Boolean isTyping = (Boolean) typingData.get("isTyping");
            String role = (String) typingData.get("role");

            Map<String, Object> typingIndicator = new HashMap<>();
            typingIndicator.put("userId", userId);
            typingIndicator.put("role", role);
            typingIndicator.put("isTyping", isTyping);
            typingIndicator.put("timestamp", System.currentTimeMillis());

            messagingTemplate.convertAndSend(
                    "/send/chat/" + chatId + "/typing",
                    typingIndicator
            );

        } catch (Exception e) {
            logger.error("❌ Failed to handle typing indicator in chat {}: {}", chatId, e.getMessage(), e);
        }
    }

    @MessageMapping("/chat/{chatId}/mark-all-read")
    public void markAllAsRead(
            @DestinationVariable Long chatId,
            @Payload Map<String, Object> data) {

        try {
            String role = (String) data.get("role");

            Map<String, Object> readReceipt = new HashMap<>();
            readReceipt.put("chatId", chatId);
            readReceipt.put("readBy", data.get("userId"));
            readReceipt.put("role", role);
            readReceipt.put("timestamp", System.currentTimeMillis());

            messagingTemplate.convertAndSend(
                    "/send/chat/" + chatId + "/read-receipt",
                    readReceipt
            );

        } catch (Exception e) {
            logger.error("❌ Failed to mark messages as read in chat {}: {}", chatId, e.getMessage(), e);
        }
    }

    /**
     * Handle presence updates
     * Route: /app/presence/update
     * Clients subscribe to: /send/presence
     */
    // WebSocketMessageController.java - Update these two methods

    /**
     * Handle presence updates (EXPLICIT login/logout)
     * Route: /app/presence/update
     * Clients subscribe to: /send/presence
     * This ALWAYS broadcasts to all clients
     */
    @MessageMapping("/presence/update")
    public void updatePresence(@Payload PresenceUpdate presenceUpdate) {
        try {
            logger.info("🔥 Received EXPLICIT presence update: {} {} -> {}",
                    presenceUpdate.getUserType(),
                    presenceUpdate.getId(),
                    presenceUpdate.getOnlineStatus());

            // This will ALWAYS broadcast (unless throttled)
            presenceService.updatePresence(presenceUpdate);

        } catch (Exception e) {
            logger.error("❌ Failed to update presence for {} {}: {}",
                    presenceUpdate.getUserType(),
                    presenceUpdate.getId(),
                    e.getMessage(), e);
            throw new RuntimeException("Failed to update presence", e);
        }
    }

    /**
     * Heartbeat endpoint to keep user online
     * Route: /app/presence/heartbeat
     * Clients should send this every 20-30 seconds
     * This only updates timestamp, doesn't broadcast
     */
    @MessageMapping("/presence/heartbeat")
    public void heartbeat(@Payload Map<String, Object> heartbeatData) {
        try {
            Long userId = Long.valueOf(heartbeatData.get("userId").toString());
            String userType = (String) heartbeatData.get("userType");

            logger.debug("💓 Heartbeat received for {} {}", userType, userId);

            PresenceUpdate presenceUpdate = PresenceUpdate.builder()
                    .Id(userId)
                    .UserType(userType)
                    .onlineStatus(OnlineStatus.ONLINE)
                    .time(java.time.LocalDateTime.now())
                    .build();

            // Use heartbeat method (doesn't broadcast, just updates timestamp)
            presenceService.updatePresenceFromHeartbeat(presenceUpdate);

        } catch (Exception e) {
            logger.error("❌ Failed to process heartbeat: {}", e.getMessage(), e);
        }
    }
    

}