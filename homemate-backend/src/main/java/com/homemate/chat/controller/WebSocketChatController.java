//package com.homemate.chat.controller;
//
//import com.homemate.chat.Service.ChatService;
//import com.homemate.chat.Service.MessageService;
//import com.homemate.chat.dto.MessageDto;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.messaging.handler.annotation.MessageMapping;
//import org.springframework.messaging.handler.annotation.Payload;
//import org.springframework.messaging.simp.SimpMessagingTemplate;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.stereotype.Controller;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.HashMap;
//import java.util.Map;
//
//@Controller
//public class WebSocketChatController {
//
//    @Autowired
//    private SimpMessagingTemplate messagingTemplate;
//
//    @Autowired
//    private MessageService messageService;
//
//    @Autowired
//    private ChatService chatService;
//    @PostMapping("/userSendMessage/{chatID}")
//    @PreAuthorize("hasRole('USER')")
//    public ResponseEntity<MessageDto> userMessage(@PathVariable Long chatID, @RequestBody MessageDto messageDto)throws Exception{
//        try {
//            MessageDto savedMessage = messageService.sendMessage(chatID, messageDto);
//            messagingTemplate.convertAndSend(
//                    "/topic/chat/" + chatID,
//                    savedMessage
//            );
//
//            messagingTemplate.convertAndSend(
//                    "/queue/user/" + savedMessage.getReceiverId() + "/messages",
//                    savedMessage
//            );
//            return ResponseEntity.status(HttpStatus.OK).body(savedMessage);
//
//
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//        }
//    }
//    @PostMapping("/taskerSendMessage/{chatID}")
//    @PreAuthorize("hasRole('TASKER')")
//    public  ResponseEntity<MessageDto> taskerMessage(@PathVariable Long chatID, @RequestBody MessageDto messageDto)throws Exception{
//        try {
//           MessageDto savedMessage= messageService.sendMessage(chatID,messageDto);
//            messagingTemplate.convertAndSend(
//                    "/topic/chat/" + chatID,
//                    savedMessage
//            );
//
//            messagingTemplate.convertAndSend(
//                    "/queue/user/" + savedMessage.getReceiverId() + "/messages",
//                    savedMessage
//            );
//            return ResponseEntity.status(HttpStatus.OK).body(savedMessage);
//        }
//        catch (Exception e){
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//        }
//    }
//    @GetMapping("/{chatID}/user-unread-count")
//    @PreAuthorize("hasRole('USER')")
//    public ResponseEntity<Integer> getUnreadCountU(@PathVariable Long chatID)throws Exception {
//        int unreadMessages = messageService.getUnreadMessagesForUser(chatID);
//        return ResponseEntity.ok(unreadMessages);
//    }
//
//    @GetMapping("/{chatID}/tasker-unread-count")
//    @PreAuthorize("hasRole('TASKER')")
//    public ResponseEntity<Integer> getUnreadCountT(@PathVariable Long chatID)throws Exception {
//        int unreadMessages = messageService.getUnreadMessagesForTasker(chatID);
//        return ResponseEntity.ok(unreadMessages);
//    }
//    @PutMapping("/{chatID}/mark-read-user")
//    @PreAuthorize("hasRole('USER')")
//    public ResponseEntity<Void> markMessageAsRead(@PathVariable Long chatID) throws Exception{
//        try {
//            messageService.markAllAsReadUser(chatID);
//            Map<String, Object> statusUpdate = new HashMap<>();
//            statusUpdate.put("chatId", chatID);
//            statusUpdate.put("status", "read");
//            statusUpdate.put("isUserSender", true);
//            statusUpdate.put("timestamp", System.currentTimeMillis());
//
//            messagingTemplate.convertAndSend(
//                    "/topic/chat/" + chatID + "/status",
//                    statusUpdate
//            );
//            return ResponseEntity.ok().build();
//        }catch (Exception e){
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//
//        }
//    }
//
//    @PutMapping("/{chatID}/mark-read-tasker")
//    @PreAuthorize("hasRole('TASKER')")
//
//    public ResponseEntity<Void> markMessagesAsRead(@PathVariable Long chatID) throws Exception{
//        try {
//            messageService.markAllAsReadTasker(chatID);
//            Map<String, Object> statusUpdate = new HashMap<>();
//            statusUpdate.put("chatId", chatID);
//            statusUpdate.put("status", "read");
//            statusUpdate.put("isUserSender", true);
//            statusUpdate.put("timestamp", System.currentTimeMillis());
//
//            messagingTemplate.convertAndSend(
//                    "/topic/chat/" + chatID + "/status",
//                    statusUpdate
//            );
//            return ResponseEntity.ok().build();
//        }catch (Exception e){
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//
//        }
//    }@PutMapping("/{userID}/mark-received-user")
//    @PreAuthorize("hasRole('USER')")
//    public ResponseEntity<Void> markMessagesAsReceivedU(@PathVariable Long userID) throws Exception {
//        try {
//            // Mark all pending messages as received
//            messageService.markAllAsReceivedForUser(userID);
//
//            // Broadcast received status via WebSocket for all affected chats
//            Map<String, Object> statusUpdate = new HashMap<>();
//            statusUpdate.put("userId", userID);
//            statusUpdate.put("status", "received");
//            statusUpdate.put("timestamp", System.currentTimeMillis());
//
//            messagingTemplate.convertAndSend(
//                    "/topic/status/" + userID,
//                    statusUpdate
//            );
//
//            System.out.println("✅ User messages marked as received for user: " + userID);
//
//            return ResponseEntity.ok().build();
//
//        } catch (Exception e) {
//            System.err.println("❌ Error marking user messages as received: " + e.getMessage());
//            e.printStackTrace();
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//        }
//    }
//
//    /**
//     * Mark all messages as received for a tasker
//     * Called when tasker comes online
//     */
//    @PutMapping("/{taskerID}/mark-received-tasker")
//    @PreAuthorize("hasRole('TASKER')")
//    public ResponseEntity<Void> markMessagesAsReceivedT(@PathVariable Long taskerID) throws Exception {
//        try {
//            // Mark all pending messages as received
//            messageService.markAllAsReceivedForTasker(taskerID);
//
//            // Broadcast received status via WebSocket for all affected chats
//            Map<String, Object> statusUpdate = new HashMap<>();
//            statusUpdate.put("taskerId", taskerID);
//            statusUpdate.put("status", "received");
//            statusUpdate.put("timestamp", System.currentTimeMillis());
//
//            messagingTemplate.convertAndSend(
//                    "/topic/status/" + taskerID,
//                    statusUpdate
//            );
//
//            System.out.println("✅ Tasker messages marked as received for tasker: " + taskerID);
//
//            return ResponseEntity.ok().build();
//
//        } catch (Exception e) {
//            System.err.println("❌ Error marking tasker messages as received: " + e.getMessage());
//            e.printStackTrace();
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//        }
//    }
//}