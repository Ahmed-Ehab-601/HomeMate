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

    public Long getTotalCount(Long chatId) {
        String sql = "SELECT COUNT(*) FROM Message WHERE chatID = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, chatId);
        return count != null ? count.longValue() : 0L;

    }

    public void save(Long chatID, MessageDto messageDto) {
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
    }

    public int getUnreadOnesForUser(Long chatID) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Message WHERE chatID = ? AND status != 'seen' AND isUserSender = FALSE";
        return jdbcTemplate.queryForObject(sql,Integer.class,chatID);
    }

    public int getUnreadOnesForTasker(Long chatID) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Message WHERE chatID = ? AND status != 'seen' AND isUserSender = TRUE";
        return jdbcTemplate.queryForObject(sql,Integer.class,chatID);
    }

    public void markasRead(Long chatID) throws SQLException {
        String sql = "UPDATE Message SET status = 'seen' WHERE chatID = ?";
        jdbcTemplate.update(sql,chatID);
    }
    // When USER logs in - mark all tasker messages (across all their chats) as received
    public void markasReceivedUser(Long userID) {
        String sql = "UPDATE Message m " +
                "JOIN Chat c ON m.chatID = c.chatID " +
                "SET m.status = 'received' " +
                "WHERE c.userID = ? AND m.isUserSender = FALSE AND m.status = 'sent'";
        jdbcTemplate.update(sql, userID);
    }

    // When TASKER logs in - mark all user messages (across all their chats) as received
    public void markasReceivedTasker(Long taskerID) {
        String sql = "UPDATE Message m " +
                "JOIN Chat c ON m.chatID = c.chatID " +
                "SET m.status = 'received' " +
                "WHERE c.taskerID = ? AND m.isUserSender = TRUE AND m.status = 'sent'";
        jdbcTemplate.update(sql, taskerID);
    }
}
//CREATE TABLE Message (
//    messageId INT AUTO_INCREMENT PRIMARY KEY,
//    chatID INT NOT NULL,
//    content VARCHAR(200),
//    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
//    senderID INT NOT NULL,
//    receiverID INT NOT NULL,
//    isUserSender BOOLEAN NOT NULL, -- true=user, false=tasker
//    status ENUM('sent','received','seen') DEFAULT 'sent' NOT NULL,
//    FOREIGN KEY (chatID) REFERENCES Chat(chatID) ON DELETE CASCADE ON UPDATE CASCADE,
//    INDEX idx_message_chat (chatID),
//    INDEX idx_message_timestamp (timestamp),
//    INDEX idx_message_sender (senderID),
//    INDEX idx_message_receiver (receiverID),
//    INDEX idx_message_status (status)
//);
//CREATE TABLE MessageImage (
//    imageID INT AUTO_INCREMENT PRIMARY KEY,
//    messageID INT NOT NULL,
//    format VARCHAR(50) NOT NULL,
//    imageFile LONGBLOB NOT NULL,
//    imageName VARCHAR(100) NOT NULL,
//    FOREIGN KEY (messageID) REFERENCES Message(messageID) ON DELETE CASCADE ON UPDATE CASCADE,
//    INDEX idx_msg_img_message (messageID)
//);