package com.homemate.chat.rowMapper;

import com.homemate.chat.dto.ChatDto;
import com.homemate.chat.dto.MessageDto;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ChatMapper implements RowMapper<ChatDto> {
    @Override
    public ChatDto mapRow(ResultSet rs, int rowNum) throws SQLException {
        ChatDto chatDto = new ChatDto();
        chatDto.setChatId(rs.getLong("chatID"));
        chatDto.setUserId(rs.getLong("userID"));
        chatDto.setTaskerId(rs.getLong("taskerID"));
        chatDto.setTaskerIsActive(rs.getBoolean("taskerIsActive"));
        chatDto.setUserIsActive(rs.getBoolean("userIsActive"));

return chatDto;
    }
}
// INSERT INTO Chat (userID, taskerID, userIsActive, taskerIsActive) VALUES