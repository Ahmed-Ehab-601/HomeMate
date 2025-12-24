package com.homemate.chat.Service;

import com.homemate.chat.dao.ChatDao;
import com.homemate.chat.dao.MessageDao;
import com.homemate.chat.dto.ChatDto;
import com.homemate.chat.dto.MessageDto;
import com.homemate.chat.dto.PaginatedResponse;
import com.homemate.security.model.AppUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatService {
    @Autowired
    private final MessageDao messageDao;
    private final ChatDao chatDao;
    private final MessageService messageService;
    public ChatService(MessageDao messageDao, ChatDao chatDao,MessageService messageService) {
        this.messageDao = messageDao;
        this.chatDao = chatDao;
        this.messageService=messageService;
    }
    public ChatDto getChat(Long chatId) throws Exception{
           return chatDao.getChat(chatId);

    }
    public PaginatedResponse getChatHistory(Long chatId, int page, int size,AppUserDetails userDetails) throws Exception{
        if(!userDetails.getId().equals(chatDao.getChat(chatId).getUserId()) && !userDetails.getId().equals(chatDao.getChat(chatId).getTaskerId()))
                   throw new Exception("unauthorized user");
        List<MessageDto> messages = messageDao.getAllMessages(chatId, page, size);

        Long totalCount = messageDao.getTotalCount(chatId);

        long totalPages = (long) Math.ceil((double) totalCount / size);

        return PaginatedResponse.builder()
                .messages(messages)
                .page(page)
                .pageSize(size)
                .totalCount(totalCount)
                .totalPages(totalPages)
                .hasNext(page < totalPages - 1)
                .hasPrevious(page > 0)
                .build();
    }

    public String getUserPhoneNumber(Long Id,AppUserDetails userDetails) throws Exception{
        if(!userDetails.getId().equals(chatDao.getChat(Id).getUserId()) && !userDetails.getId().equals(chatDao.getChat(Id).getTaskerId()))
            throw new Exception("unauthorized user");
        String str=chatDao.getUserPhone(Id);
        if(str==null)
            throw new RuntimeException("No Phone Number Available");

        return str;
    }

    public String getTaskerPhoneNumber(Long Id,AppUserDetails userDetails)throws Exception {
        if(!userDetails.getId().equals(chatDao.getChat(Id).getUserId()) && !userDetails.getId().equals(chatDao.getChat(Id).getTaskerId()))
            throw new Exception("unauthorized user");
        String str=chatDao.getTaskerPhone(Id);
        if(str==null)
            throw new RuntimeException("No Phone Number Available");

        return str;
    }


    public String getRecipentNameUser(Long chatId,AppUserDetails userDetails) throws Exception {

        ChatDto chat = chatDao.getChat(chatId);

        if(chat == null) throw new Exception("Unknown chat");
        if(!userDetails.getId().equals(chat.getTaskerId()))
            throw new Exception("Unauthorized user");

        return chatDao.getNameUser(chat.getUserId());
    }
    public String getRecipentNameTasker(Long chatId,AppUserDetails userDetails) throws Exception{
        ChatDto chat = chatDao.getChat(chatId);

        if(chat == null) throw new Exception("Unknown chat");
        if(!userDetails.getId().equals(chat.getUserId()))
            throw new Exception("Unauthorized user");

        return chatDao.getNameTasker(chat.getTaskerId());
    }


    public void changeStatusTasker(Long taskerId) throws Exception {
        chatDao.changeTaskerStatus(taskerId);
    }

    public void changeStatusUser(Long taskerId) throws Exception{
        chatDao.changeUserStatus(taskerId);
    }
    public void setTaskerOnline(Long taskerId) throws Exception {
        try {
            messageService.markAllAsReceivedForTasker(taskerId);
            chatDao.setTaskerOnline(taskerId);
        } catch (Exception e) {
            throw new Exception("Failed to set tasker online: " + e.getMessage());
        }
    }

    public void setUserOnline(Long userId) throws Exception {
        try {
            messageService.markAllAsReceivedForUser(userId);
            chatDao.setUserOnline(userId);
        } catch (Exception e) {
            throw new Exception("Failed to set user online: " + e.getMessage());
        }
    }
         public  void resetUnreadForUser(Long chatID,AppUserDetails userDetails) throws Exception {
          try {
              if(!chatDao.getChat(chatID).getUserId().equals(userDetails.getId())){
                  throw new Exception("UnAuthorizied user");}
                  chatDao.resetUserUnreadCount(chatID);
          } catch (Exception e) {
              throw new Exception("couldn't reset unread count");
          }
         }

    public void resetUnreadForTasker(Long chatID, AppUserDetails userDetails) throws Exception {
        try {
            if(!chatDao.getChat(chatID).getTaskerId().equals(userDetails.getId())){
                throw new Exception("UnAuthorizied tasker");}
            chatDao.resetTaskerUnreadCount(chatID);
        } catch (Exception e) {
            throw new Exception("couldn't reset unread count");
        }
    }
}

