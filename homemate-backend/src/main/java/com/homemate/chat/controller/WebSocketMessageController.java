package com.homemate.chat.controller;

import com.homemate.chat.Service.MessageService;
import com.homemate.chat.dto.MessageDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.HashMap;
import java.util.Map;

/**
 * WebSocket controller for real-time messaging
 * Handles message sending, delivery, and read receipts
 */
@Controller
public class WebSocketMessageController {
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private MessageService messageService;

    /**
     * Handle incoming messages from clients
     * Route: /app/chat/{chatId}/send
     * Clients subscribe to: /topic/chat/{chatId}
     */
    @MessageMapping("/chat/{chatId}/send")
    public void sendMessage(
            @DestinationVariable Long chatId,
            @Payload MessageDto messageDto) {
     try {
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

        } catch (Exception e) {
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

        } catch (Exception e) {
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
        }
    }
}