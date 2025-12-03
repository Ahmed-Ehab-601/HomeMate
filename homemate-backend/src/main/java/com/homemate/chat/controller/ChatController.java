package com.homemate.chat.controller;

import com.homemate.chat.Service.ChatService;
import com.homemate.chat.dto.MessageDto;
import com.homemate.chat.dto.PaginatedResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
public class ChatController {
    @Autowired
    private ChatService chatService;
    public ChatController(ChatService chatService){
        this.chatService=chatService;
    }
    @GetMapping("/getHistory/{chatId}")
    @PreAuthorize("hasRole('USER')")

    public ResponseEntity<PaginatedResponse> getHistory(@PathVariable Long chatId, @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "20") int size){
        try {
            PaginatedResponse RES=chatService.getChatHistory(chatId,page,size);
            return ResponseEntity.status(HttpStatus.OK).body(RES);
        }
        catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

    }
}
