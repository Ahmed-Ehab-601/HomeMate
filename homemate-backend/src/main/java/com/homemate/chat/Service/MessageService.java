package com.homemate.chat.Service;

import com.homemate.admin.domain.entities.Tasker;
import com.homemate.chat.Enum.MessageStatus;
import com.homemate.chat.dao.ChatDao;
import com.homemate.chat.dao.MessageDao;
import com.homemate.chat.dto.MessageDto;
import com.homemate.security.model.AppUserDetails;
import org.springframework.messaging.core.AbstractDestinationResolvingMessagingTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class MessageService {
    private final MessageDao messageDao;
    private final ChatDao chatDao;
    private final SimpMessagingTemplate messagingTemplate;

    public MessageService(MessageDao messageDao,ChatDao chatDao,SimpMessagingTemplate messagingTemplate) {
        this.messageDao = messageDao;
        this.chatDao=chatDao;
        this.messagingTemplate=messagingTemplate;
    }

    public MessageDto sendMessage(Long chatID, MessageDto messageDto, AppUserDetails userDetails)throws Exception {
        try {
            if(!userDetails.getId().equals(chatDao.getChat(chatID).getUserId())
                    && !userDetails.getId().equals(chatDao.getChat(chatID).getTaskerId()))
                throw new Exception("unauthorized user");
            boolean isUserSender = messageDto.isIsUserSender();

            if (isUserSender&& messageDao.getTaskerStatus(chatID)) {
                messageDto.setMessageStatus(MessageStatus.received);
            } else if (!isUserSender && messageDao.getUserStatus(chatID)) {
                messageDto.setMessageStatus(MessageStatus.received);
            }
            return messageDao.save(chatID, messageDto);
        } catch (Exception e) {
            throw new Exception(e);
        }
    }

    private void broadcastReceivedMessages(Long id,Boolean isUser) {
        List<MessageDto> list = messageDao.listMessagesRecievedTasker(id, isUser);
        Map<Long, List<MessageDto>> messagesByChat = list.stream()
                .collect(Collectors.groupingBy(MessageDto::getChatId));
        messagesByChat.forEach((chatId, messages)->{
            messages.forEach(msg-> {
                    Map<String,Object> statusUpdate=new HashMap<>();
            statusUpdate.put("messageId", msg.getMessageId());
            statusUpdate.put("status", "RECEIVED");
            statusUpdate.put("timestamp", System.currentTimeMillis());
            statusUpdate.put("bulkUpdate", true);
                messagingTemplate.convertAndSend(
                        "/send/chat/" + chatId + "/status",
                        statusUpdate
                );});

    });
}

    public void markAllAsReadTasker(Long chatID,AppUserDetails userDetails)throws Exception {
        try {
            if(!Objects.equals(userDetails.getId(), chatDao.getChat(chatID).getUserId())
                    && !userDetails.getId().equals(chatDao.getChat(chatID).getTaskerId()))
                throw new Exception("unauthorized user");
            messageDao.markasReadTasker(chatID);
        }catch (Exception e){
            throw new Exception("Couldn't change Message Status to read ");
        }
    }

    public void markAllAsReceivedForUser(Long userID) throws Exception {
        try {
            messageDao.markasReceivedUser(userID);
            broadcastReceivedMessages(userID,true);
        } catch (Exception e) {
            throw new Exception("Couldn't change Message Status to received: " + e.getMessage());
        }
    }
    // Tasker logs in

    public void markAllAsReceivedForTasker(Long taskerID) throws Exception {
        try {
            messageDao.markasReceivedTasker(taskerID);
            broadcastReceivedMessages(taskerID,false);
        } catch (Exception e) {
            throw new Exception("Couldn't change Message Status to received: " + e.getMessage());
        }
    }
    public Boolean calculateAndCheckUnread(Long userID, AppUserDetails userDetails) throws Exception {
      try{
          if(!userID.equals(userDetails.getId()))
              throw new Exception("Unauthorized User");
          return messageDao.getUnreadOnesForUser(userID);

      } catch (Exception e) {
          throw new Exception("Couldn't calculate unread messages ");
      }
    }
    public Boolean calculateAndCheckUnreadT(Long taskerID, AppUserDetails userDetails) throws Exception {
        try{
            if(!taskerID.equals(userDetails.getId()))
                throw new Exception("Unauthorized Tasker");
            return messageDao.getUnreadOnesForTasker(taskerID);

        } catch (Exception e) {
            throw new Exception("Couldn't calculate unread messages ");
        }
    }

    public void markAllAsReadUser(Long chatID,AppUserDetails userDetails)throws Exception {
        try {
            if(!userDetails.getId().equals(chatDao.getChat(chatID).getUserId())
                    && !userDetails.getId().equals(chatDao.getChat(chatID).getTaskerId()))
                throw new Exception("unauthorized user");

            messageDao.markasReadUser(chatID);
        }catch (Exception e){
            throw new Exception("Couldn't change Message Status to read ");
        }
    }
}
