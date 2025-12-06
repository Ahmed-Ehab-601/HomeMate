package com.homemate.chat.Service;

import com.homemate.chat.dao.MessageDao;
import com.homemate.chat.dto.MessageDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MessageService {
    private MessageDao messageDao;

    public MessageService(MessageDao messageDao) {
        this.messageDao = messageDao;
    }

    public void sendMessage(Long chatID, MessageDto messageDto)throws Exception {
        try {
            messageDao.save(chatID, messageDto);
        } catch (Exception e) {
            throw new Exception(e);
        }
    }

    public int getUnreadMessagesForUser(Long chatID) throws Exception {
       try {
           return messageDao.getUnreadOnesForUser(chatID);
    }catch (Exception e){
           throw new Exception("Couldn't get unread Messages");
       }
       }

    public int getUnreadMessagesForTasker(Long chatID) throws Exception {
        try {
            return messageDao.getUnreadOnesForTasker(chatID);
        }catch (Exception e){
            throw new Exception("Couldn't get unread Messages");
        }
    }
    public void markAllAsRead(Long chatID)throws Exception {
        try {
             messageDao.markasRead(chatID);
        }catch (Exception e){
            throw new Exception("Couldn't change Message Status to read ");
        }
    }

    // User logs in
    public void markAllAsReceivedForUser(Long userID) throws Exception {
        try {
            messageDao.markasReceivedUser(userID);
        } catch (Exception e) {
            throw new Exception("Couldn't change Message Status to received: " + e.getMessage());
        }
    }

    // Tasker logs in
    public void markAllAsReceivedForTasker(Long taskerID) throws Exception {
        try {
            messageDao.markasReceivedTasker(taskerID);
        } catch (Exception e) {
            throw new Exception("Couldn't change Message Status to received: " + e.getMessage());
        }
    }

}
