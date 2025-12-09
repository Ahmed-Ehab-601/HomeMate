package com.homemate.chat.Interface;

import com.homemate.chat.dto.ChatDto;

import java.sql.SQLException;

public interface IChatDao <C>{
    String getUserPhone(Long id)throws SQLException;

    String getTaskerPhone(Long id) throws SQLException;

    ChatDto getChat(Long chatId) throws SQLException;

    String getNameTasker(Long taskerId);

    String getNameUser(Long userId);


}
