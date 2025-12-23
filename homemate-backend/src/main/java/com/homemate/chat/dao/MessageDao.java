package com.homemate.chat.dao;

import com.homemate.chat.Interface.IMessageDao;

import com.homemate.chat.dto.ImageDto;
import com.homemate.chat.dto.MessageDto;
import com.homemate.chat.rowMapper.MessageRowMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;

import java.util.Base64;
import java.util.List;


@Component
public class MessageDao implements IMessageDao<MessageDto> {
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Override
    public List<MessageDto> getAllMessages(Long chatId, int page, int size) {
        int Offset=size*page;
        String sql = "SELECT * " +
                "FROM Message m " +
                "LEFT JOIN MessageImage mi ON m.messageId = mi.messageID " +
                "WHERE m.chatID = ? " +
                "ORDER BY m.timestamp DESC " +
                "LIMIT ? OFFSET ?";

        return jdbcTemplate.query(sql,
                new Object[]{chatId, size, Offset},
                new MessageRowMapper());

    }
    @Override
    public Long getTotalCount(Long chatId) {
        String sql = "SELECT COUNT(*) FROM Message WHERE chatID = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, chatId);
        return count != null ? count.longValue() : 0L;

    }
@Override
public MessageDto save(Long chatID, MessageDto messageDto) {
        // Set timestamp if not provided
        Timestamp timestamp = messageDto.getTimestamp() != null
                ? Timestamp.valueOf(messageDto.getTimestamp())
                : Timestamp.from(Instant.now());

        // Insert message first
        String messageSql = "INSERT INTO Message (chatID, content, timestamp, senderID, receiverID, isUserSender, status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(messageSql, new String[]{"messageId"});
            ps.setLong(1, chatID);
            ps.setString(2, messageDto.getContent());
            ps.setTimestamp(3, timestamp);
            ps.setLong(4, messageDto.getSenderId());
            ps.setLong(5, messageDto.getReceiverId());
            ps.setBoolean(6, messageDto.isIsUserSender());
            ps.setString(7, messageDto.getMessageStatus() != null
                    ? messageDto.getMessageStatus().toString()
                    : "sent");
            return ps;
        }, keyHolder);

        // Get the generated messageId
        Long messageId = keyHolder.getKey().longValue();
        messageDto.setMessageId(messageId);

        // Insert image if provided
        if (messageDto.getImageDto() != null) {
            ImageDto imageDto = messageDto.getImageDto();
            byte[] imageBytes = imageDto.getFileData() != null
                    ? Base64.getDecoder().decode(imageDto.getFileData())
                    : null;

            String imageSql = "INSERT INTO MessageImage (messageID, format, imageFile, imageName) " +
                    "VALUES (?, ?, ?, ?)";
            jdbcTemplate.update(imageSql,
                    messageId,
                    imageDto.getFileFormat(),
                    imageBytes,
                    imageDto.getFileName()
            );
        }
        return jdbcTemplate.queryForObject("SELECT * " +
                "FROM Message m " +
                "LEFT JOIN MessageImage mi ON m.messageId = mi.messageID " +
                "WHERE m.messageID = ? ",new MessageRowMapper(),messageId);
    }
@Override
public int getUnreadOnesForUser(Long chatID) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Message WHERE chatID = ? AND status != 'seen' AND isUserSender = FALSE";
        return jdbcTemplate.queryForObject(sql,Integer.class,chatID);
    }
@Override
public int getUnreadOnesForTasker(Long chatID) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Message WHERE chatID = ? AND status != 'seen' AND isUserSender = TRUE";
        return jdbcTemplate.queryForObject(sql,Integer.class,chatID);
    }
@Override
public void markasReadUser(Long chatID) throws SQLException {
        String sql = "UPDATE Message SET status = 'seen' WHERE chatID = ? AND isUserSender= FALSE";
        jdbcTemplate.update(sql,chatID);
    }
    @Override
    public void markasReadTasker(Long chatID) throws SQLException {
        String sql = "UPDATE Message SET status = 'seen' WHERE chatID = ? AND isUserSender = TRUE";
        jdbcTemplate.update(sql,chatID);
    }
@Override
public void markasReceivedUser(Long userID) {
        String sql = "UPDATE Message m " +
                "JOIN Chat c ON m.chatID = c.chatID " +
                "SET m.status = 'received' " +
                "WHERE c.userID = ? AND m.isUserSender = FALSE AND m.status = 'sent'";
        jdbcTemplate.update(sql, userID);
    }
@Override
public void markasReceivedTasker(Long taskerID) {
        String sql = "UPDATE Message m " +
                "JOIN Chat c ON m.chatID = c.chatID " +
                "SET m.status = 'received' " +
                "WHERE c.taskerID = ? AND m.isUserSender = TRUE AND m.status = 'sent'";
        jdbcTemplate.update(sql, taskerID);
    }
    @Override
    public Boolean getUserStatus(Long chatID){
        String sql="SELECT userIsActive FROM Chat WHERE chatID = ?";
        return jdbcTemplate.queryForObject(sql,Boolean.class,chatID);
    }
    @Override
    public Boolean getTaskerStatus(Long chatID){
        String sql="SELECT taskerIsActive FROM Chat WHERE chatID = ?";
        return jdbcTemplate.queryForObject(sql,Boolean.class,chatID);
    }

    public List<MessageDto> listMessagesRecievedTasker(Long id, Boolean isUser) {
        String sql;
        if(isUser){
            sql = "SELECT * FROM Message m " +
                    "LEFT JOIN MessageImage mi ON m.messageId = mi.messageID " +
                    "WHERE m.status = 'received' " +
                    "AND m.receiverID = ? " +
                    "AND m.isUserSender = FALSE " +
                    "ORDER BY m.timestamp DESC";
        }
        else{
            sql = "SELECT * FROM Message m " +
                    "LEFT JOIN MessageImage mi ON m.messageId = mi.messageID " +
                    "WHERE m.status = 'received' " +
                    "AND m.receiverID = ? " +
                    "AND m.isUserSender = TRUE " +
                    "ORDER BY m.timestamp DESC";
        }

        return jdbcTemplate.query(sql,
                new Object[]{id},
                new MessageRowMapper());
    }
}
