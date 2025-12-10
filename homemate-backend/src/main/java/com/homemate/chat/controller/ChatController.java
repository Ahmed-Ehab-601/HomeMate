package com.homemate.chat.controller;

import com.homemate.chat.Service.ChatService;
import com.homemate.chat.Service.MessageService;
import com.homemate.chat.dao.MessageDao;
import com.homemate.chat.dto.ChatDto;
import com.homemate.chat.dto.MessageDto;
import com.homemate.chat.dto.PaginatedResponse;
import com.homemate.security.model.AppUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
public class ChatController {
    @Autowired
    private ChatService chatService;
    private MessageService messageService;
    public ChatController(ChatService chatService,MessageService messageService){
        this.chatService=chatService;
        this.messageService=messageService;
    }
    @GetMapping("/getHistory/user/{chatId}")
    @PreAuthorize("hasRole('USER')")

    public ResponseEntity<PaginatedResponse> getHistory(@PathVariable Long chatId, @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "20") int size,@AuthenticationPrincipal AppUserDetails userDetails) throws Exception {
        try {
            PaginatedResponse RES=chatService.getChatHistory(chatId,page,size,userDetails);
            return ResponseEntity.status(HttpStatus.OK).body(RES);
        }
        catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

    }
    @GetMapping("/getChat/{chatId}")
    public ResponseEntity<ChatDto> getChat(@PathVariable Long chatId)throws Exception{
        try {
           ChatDto RES=chatService.getChat(chatId);
            return ResponseEntity.status(HttpStatus.OK).body(RES);
        }
        catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

    }
    @GetMapping("/getHistory/tasker/{chatId}")
    @PreAuthorize("hasRole('TASKER')")
    public ResponseEntity<PaginatedResponse> getHistorytasker(@PathVariable Long chatId, @RequestParam(defaultValue = "0") int page,
                                                        @RequestParam(defaultValue = "20") int size,@AuthenticationPrincipal AppUserDetails userDetails){
        try {
            PaginatedResponse RES=chatService.getChatHistory(chatId,page,size, userDetails);
            return ResponseEntity.status(HttpStatus.OK).body(RES);
        }
        catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

    }
    @GetMapping("/callUser/{chatId}")
    @PreAuthorize("hasRole('TASKER')")
    public ResponseEntity<String> getUserPhoneNumber(@PathVariable("chatId") Long chatId,@AuthenticationPrincipal AppUserDetails userDetails){
        try {
            String RES=chatService.getUserPhoneNumber(chatId,userDetails);
            return ResponseEntity.status(HttpStatus.OK).body(RES);
        }
        catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

    }
    @GetMapping("/callTasker/{chatId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<String> getTaskerPhoneNumber(@PathVariable("chatId") Long chatId,@AuthenticationPrincipal AppUserDetails userDetails)
    {        try {
            String RES=chatService.getTaskerPhoneNumber(chatId,userDetails);
            return ResponseEntity.status(HttpStatus.OK).body(RES);
        }
        catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

    }
    @PostMapping("/tasker/{taskerId}/online")
    public ResponseEntity<String> setTaskerOnline(@PathVariable Long taskerId) {
        try {
            chatService.setTaskerOnline(taskerId);
            // Also mark pending messages as received
            messageService.markAllAsReceivedForTasker(taskerId);
            return ResponseEntity.ok("Tasker status set to online");
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body("Failed to set tasker online: " + e.getMessage());
        }
    }
    @PostMapping("/user/{userId}/online")
    public ResponseEntity<String> setUserOnline(@PathVariable Long userId) {
        try {
            chatService.setUserOnline(userId);
            // Also mark pending messages as received
            messageService.markAllAsReceivedForUser(userId);
            return ResponseEntity.ok("User status set to online");
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body("Failed to set user online: " + e.getMessage());
        }
    }

    @PostMapping("/tasker/{taskerId}/offline")
    @PreAuthorize("hasRole('TASKER')")
    public ResponseEntity<String> changeStatus(@PathVariable Long taskerId){
        try {
            chatService.changeStatusTasker(taskerId);
            return ResponseEntity.ok("changed");

        }
        catch (Exception e){
            return ResponseEntity.badRequest().build();
        }
    }
    @PostMapping("/user/{userId}/offline")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<String> changeStatus2(@PathVariable Long userId){
        try {
            chatService.changeStatusUser(userId);
            return ResponseEntity.ok("changed");

        }
        catch (Exception e){
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/user/getRecipientName/{chatId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<String> getRecipientTasker(@PathVariable Long chatId,@AuthenticationPrincipal AppUserDetails userDetails){
        try {
            String res = chatService.getRecipentNameTasker(chatId,userDetails);
            return ResponseEntity.status(HttpStatus.OK).body(res);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    @GetMapping("/tasker/getRecipientName/{chatId}")
    @PreAuthorize("hasRole('TASKER')")
    public ResponseEntity<String> getRecipientUser(@PathVariable Long chatId,@AuthenticationPrincipal AppUserDetails userDetails){
        try {
            String res = chatService.getRecipentNameUser(chatId,userDetails);
            return ResponseEntity.status(HttpStatus.OK).body(res);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}
