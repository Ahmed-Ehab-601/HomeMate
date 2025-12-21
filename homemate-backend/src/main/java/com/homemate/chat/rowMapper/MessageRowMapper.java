package com.homemate.chat.rowMapper;

import com.homemate.chat.Enum.MessageStatus;
import com.homemate.chat.dto.ImageDto;
import com.homemate.chat.dto.MessageDto;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Base64;

public class MessageRowMapper implements RowMapper<MessageDto> {

    @Override
    public MessageDto mapRow(ResultSet rs, int rowNum) throws SQLException {
        MessageDto message = new MessageDto();

        // Map message fields
        message.setMessageId(rs.getLong("messageId"));
        message.setChatId(rs.getLong("chatID"));
        message.setSenderId(rs.getLong("senderID"));
        message.setContent(rs.getString("content"));
        message.setTimestamp(toLocalDateTime(rs.getTimestamp("timestamp")));
        message.setReceiverId(rs.getLong("receiverID"));
        message.setIsUserSender(rs.getBoolean("isUserSender"));
        message.setMessageStatus(MessageStatus.valueOf(rs.getString("status")));

        // Map image if present (from LEFT JOIN with MessageImage)
        Long imageId = rs.getLong("imageID");
        if (!rs.wasNull()) {
            ImageDto image = new ImageDto();
            image.setId(imageId);
            image.setMessageId(rs.getLong("messageId"));
            image.setFileFormat(rs.getString("format"));

            image.setFileData(rs.getString("imageFile"));

            image.setFileName(rs.getString("imageName"));

            message.setImageDto(image);
        }

        return message;
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp){
        return timestamp != null ? timestamp.toLocalDateTime() : null;
    }
}