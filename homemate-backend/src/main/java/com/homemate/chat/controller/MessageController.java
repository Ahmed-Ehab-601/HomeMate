package com.homemate.chat.controller;

import com.homemate.chat.Service.MessageService;
import com.homemate.chat.dto.MessageDto;
import com.homemate.security.model.AppUserDetails;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/message")
@CrossOrigin(origins = "*")
public class MessageController {
    private MessageService messageService;
    public MessageController(MessageService messageService){
        this.messageService=messageService;
    }
    @PostMapping("/userSendMessage/{chatID}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<MessageDto> userMessage(@PathVariable Long chatID, @RequestBody MessageDto messageDto,@AuthenticationPrincipal AppUserDetails userDetails)throws Exception{
        try {
         MessageDto messageDto1=  messageService.sendMessage(chatID,messageDto,userDetails);
           return   ResponseEntity.status(HttpStatus.OK).body(messageDto1);
        }
        catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    @PostMapping("/taskerSendMessage/{chatID}")
    @PreAuthorize("hasRole('TASKER')")
    public  ResponseEntity<MessageDto> taskerMessage(@PathVariable Long chatID, @RequestBody MessageDto messageDto,@AuthenticationPrincipal AppUserDetails userDetails)throws Exception{
        try {
           MessageDto messageDto1= messageService.sendMessage(chatID,messageDto,userDetails);
            //call websocket (broadcast)
            return   ResponseEntity.status(HttpStatus.OK).body(messageDto1);
        }
        catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    @GetMapping("/{chatID}/user-unread-count")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Integer> getUnreadCountU(@PathVariable Long chatID,@AuthenticationPrincipal AppUserDetails userDetails)throws Exception {
        int unreadMessages = messageService.getUnreadMessagesForUser(chatID,userDetails);
        return ResponseEntity.ok(unreadMessages);
    }

    @GetMapping("/{chatID}/tasker-unread-count")
    @PreAuthorize("hasRole('TASKER')")
    public ResponseEntity<Integer> getUnreadCountT(@PathVariable Long chatID,@AuthenticationPrincipal AppUserDetails userDetails)throws Exception {
        int unreadMessages = messageService.getUnreadMessagesForTasker(chatID,userDetails);
        return ResponseEntity.ok(unreadMessages);
    }
    @PutMapping("/{chatID}/mark-read-user")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> markMessageAsRead(@PathVariable Long chatID,@AuthenticationPrincipal AppUserDetails userDetails) throws Exception{
       try {
           messageService.markAllAsReadUser(chatID,userDetails);
        //websocket
        return ResponseEntity.ok().build();
    }catch (Exception e){
           return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();

       }
    }

    @PutMapping("/{chatID}/mark-read-tasker")
    @PreAuthorize("hasRole('TASKER')")

    public ResponseEntity<Void> markMessagesAsRead(@PathVariable Long chatID,@AuthenticationPrincipal AppUserDetails userDetails) throws Exception{
        try {
            messageService.markAllAsReadTasker(chatID,userDetails);
            //websocket
            return ResponseEntity.ok().build();
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();

        }
    }
//    @PutMapping("/{UserID}/mark-receivedU")
//    @PreAuthorize("hasRole('USER')")
//    public ResponseEntity<Void> markMessagesAsReceivedU(@PathVariable Long UserID) throws Exception{
//       // try {
//            messageService.markAllAsReceivedForUser(UserID);
//            //websocket
//            return ResponseEntity.ok().build();
////        }catch (Exception e){
////            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
////
////        }
//    }
//    @PutMapping("/{TaskerID}/mark-receivedT")
//    @PreAuthorize("hasRole('TASKER')")
//    public ResponseEntity<Void> markMessagesAsReceivedT(@PathVariable Long TaskerID) throws Exception{
//        try {
//            messageService.markAllAsReceivedForTasker(TaskerID);
//            //websocket
//            return ResponseEntity.ok().build();
//        }catch (Exception e){
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//
//        }
//    }
}
