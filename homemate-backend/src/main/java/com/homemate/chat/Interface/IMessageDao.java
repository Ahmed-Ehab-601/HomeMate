package com.homemate.chat.Interface;

import com.homemate.chat.dto.MessageDto;

import java.sql.SQLException;
import java.util.List;

public interface IMessageDao <M>{


    List<MessageDto> getAllMessages(Long chatId, int page, int size);

    Long getTotalCount(Long chatId);

    void save(Long chatID, MessageDto messageDto);

    int getUnreadOnesForUser(Long chatID) throws SQLException;

    int getUnreadOnesForTasker(Long chatID) throws SQLException;


    void markasReadUser(Long chatID) throws SQLException;

    void markasReadTasker(Long chatID) throws SQLException;

    void markasReceivedUser(Long userID);

    // When TASKER logs in - mark all user messages (across all their chats) as received
    void markasReceivedTasker(Long taskerID);

    Boolean getUserStatus(Long chatID);

    Boolean getTaskerStatus(Long chatID);
}
