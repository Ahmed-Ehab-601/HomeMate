package com.homemate.chat.dao;

import com.homemate.chat.Interface.IMessageDao;

import com.homemate.chat.dto.MessageDto;
import com.homemate.chat.rowMapper.MessageRowMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
@Component
public class MessageDao implements IMessageDao<MessageDto> {
   @Autowired
   private JdbcTemplate jdbcTemplate;
    @Override
    public List<MessageDto> getAllMessages(Long chatId, int page, int size) {
        int Offset=size*page;
        String sql = "SELECT *" +
                "FROM Message m " +
                "LEFT JOIN MessageImage mi ON m.messageId = mi.messageID " +
                "WHERE m.chatID = ? " +
                "ORDER BY m.timestamp DESC " +
                "LIMIT ? OFFSET ?";
        return jdbcTemplate.query(sql,
                new Object[]{chatId, size, Offset},
                new MessageRowMapper());

    }

    public Long getTotalCount(Long chatId) {
        String sql = "SELECT COUNT(*) FROM Message WHERE chatID = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, chatId);
        return count != null ? count.longValue() : 0L;

    }
}
