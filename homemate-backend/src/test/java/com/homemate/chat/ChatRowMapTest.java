package com.homemate.chat;

import com.homemate.chat.Enum.MessageStatus;
import com.homemate.chat.dto.ChatDto;
import com.homemate.chat.dto.ImageDto;
import com.homemate.chat.dto.MessageDto;
import com.homemate.chat.rowMapper.ChatMapper;
import com.homemate.chat.rowMapper.MessageRowMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ChatRowMapTest {

    @Mock
    private ResultSet resultSet;

    private MessageRowMapper messageRowMapper;
    private ChatMapper chatMapper;
    private static final Long MESSAGE_ID = 1L;
    private static final Long CHAT_ID = 10L;
    private static final Long SENDER_ID = 100L;
    private static final Long RECEIVER_ID = 200L;
    private static final Long IMAGE_ID = 500L;
    private static final String CONTENT = "Test message content";
    private static final LocalDateTime TEST_TIMESTAMP = LocalDateTime.of(2024, 1, 15, 10, 30, 0);
    private static final Long USER_ID = 100L;
    private static final Long TASKER_ID = 200L;

    @BeforeEach
    public void setUp() {
        messageRowMapper = new MessageRowMapper();
        chatMapper = new ChatMapper();
    }

    // ==================== MESSAGE WITHOUT IMAGE TESTS ====================

    @Test
    public void testMapRow_MessageWithoutImage() throws SQLException {
        // Arrange
        setupBasicMessageFields();
        when(resultSet.getLong("imageID")).thenReturn(0L);
        when(resultSet.wasNull()).thenReturn(true);

        // Act
        MessageDto result = messageRowMapper.mapRow(resultSet, 0);

        // Assert
        assertNotNull(result);
        assertEquals(MESSAGE_ID, result.getMessageId());
        assertEquals(CHAT_ID, result.getChatId());
        assertEquals(SENDER_ID, result.getSenderId());
        assertEquals(RECEIVER_ID, result.getReceiverId());
        assertEquals(CONTENT, result.getContent());
        assertEquals(TEST_TIMESTAMP, result.getTimestamp());
        assertTrue(result.isIsUserSender());
        assertEquals(MessageStatus.sent, result.getMessageStatus());
        assertNull(result.getImageDto());
    }

    @Test
    public void testMapRow_MessageWithReceivedStatus() throws SQLException {
        // Arrange
        setupBasicMessageFields();
        when(resultSet.getString("status")).thenReturn("received");
        when(resultSet.getLong("imageID")).thenReturn(0L);
        when(resultSet.wasNull()).thenReturn(true);

        // Act
        MessageDto result = messageRowMapper.mapRow(resultSet, 0);

        // Assert
        assertNotNull(result);
        assertEquals(MessageStatus.received, result.getMessageStatus());
    }

    @Test
    public void testMapRow_MessageWithSeenStatus() throws SQLException {
        // Arrange
        setupBasicMessageFields();
        when(resultSet.getString("status")).thenReturn("seen");
        when(resultSet.getLong("imageID")).thenReturn(0L);
        when(resultSet.wasNull()).thenReturn(true);

        // Act
        MessageDto result = messageRowMapper.mapRow(resultSet, 0);

        // Assert
        assertEquals(MessageStatus.seen, result.getMessageStatus());
    }

    @Test
    public void testMapRow_MessageIsUserSenderFalse() throws SQLException {
        // Arrange
        setupBasicMessageFields();
        when(resultSet.getBoolean("isUserSender")).thenReturn(false);
        when(resultSet.getLong("imageID")).thenReturn(0L);
        when(resultSet.wasNull()).thenReturn(true);

        // Act
        MessageDto result = messageRowMapper.mapRow(resultSet, 0);

        // Assert
        assertFalse(result.isIsUserSender());
    }

    // ==================== MESSAGE WITH IMAGE TESTS ====================

    @Test
    public void testMapRow_MessageWithImage() throws SQLException {
        // Arrange
        setupBasicMessageFields();
        setupImageFields();

        // Act
        MessageDto result = messageRowMapper.mapRow(resultSet, 0);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getImageDto());

        ImageDto image = result.getImageDto();
        assertEquals(IMAGE_ID, image.getId());
        assertEquals(MESSAGE_ID, image.getMessageId());
        assertEquals("jpeg", image.getFileFormat());
        assertEquals("test_image.jpg", image.getFileName());
        assertNotNull(image.getFileData());
    }

    @Test
    public void testMapRow_MessageWithImageNullBytes() throws SQLException {
        // Arrange
        setupBasicMessageFields();
        when(resultSet.getLong("imageID")).thenReturn(IMAGE_ID);
        when(resultSet.wasNull()).thenReturn(false);
        when(resultSet.getLong("messageId")).thenReturn(MESSAGE_ID);
        when(resultSet.getString("format")).thenReturn("png");
        when(resultSet.getString("imageFile")).thenReturn(null);
        when(resultSet.getString("imageName")).thenReturn("null_image.png");

        // Act
        MessageDto result = messageRowMapper.mapRow(resultSet, 0);

        // Assert
        assertNotNull(result.getImageDto());
        assertNull(result.getImageDto().getFileData());
    }

    @Test
    public void testMapRow_MessageWithDifferentImageFormat() throws SQLException {
        // Arrange
        setupBasicMessageFields();
        when(resultSet.getLong("imageID")).thenReturn(IMAGE_ID);
        when(resultSet.wasNull()).thenReturn(false);
        when(resultSet.getLong("messageId")).thenReturn(MESSAGE_ID);
        when(resultSet.getString("format")).thenReturn("png");

        String imageBytes = "PNG image data";
        when(resultSet.getString("imageFile")).thenReturn(imageBytes);
        when(resultSet.getString("imageName")).thenReturn("image.png");

        // Act
        MessageDto result = messageRowMapper.mapRow(resultSet, 0);

        // Assert
        assertNotNull(result.getImageDto());
        assertEquals("png", result.getImageDto().getFileFormat());
        assertEquals("image.png", result.getImageDto().getFileName());
    }

    // ==================== TIMESTAMP TESTS ====================

    @Test
    public void testMapRow_NullTimestamp() throws SQLException {
        // Arrange
        setupBasicMessageFields();
        when(resultSet.getTimestamp("timestamp")).thenReturn(null);
        when(resultSet.getLong("imageID")).thenReturn(0L);
        when(resultSet.wasNull()).thenReturn(true);

        // Act
        MessageDto result = messageRowMapper.mapRow(resultSet, 0);

        // Assert
        assertNull(result.getTimestamp());
    }

    @Test
    public void testMapRow_DifferentTimestamp() throws SQLException {
        // Arrange
        LocalDateTime customTime = LocalDateTime.of(2023, 12, 25, 15, 45, 30);
        setupBasicMessageFields();
        when(resultSet.getTimestamp("timestamp")).thenReturn(Timestamp.valueOf(customTime));
        when(resultSet.getLong("imageID")).thenReturn(0L);
        when(resultSet.wasNull()).thenReturn(true);

        // Act
        MessageDto result = messageRowMapper.mapRow(resultSet, 0);

        // Assert
        assertEquals(customTime, result.getTimestamp());
    }

    // ==================== EDGE CASES AND EXCEPTIONS ====================

    @Test
    public void testMapRow_EmptyContent() throws SQLException {
        // Arrange
        setupBasicMessageFields();
        when(resultSet.getString("content")).thenReturn("");
        when(resultSet.getLong("imageID")).thenReturn(0L);
        when(resultSet.wasNull()).thenReturn(true);

        // Act
        MessageDto result = messageRowMapper.mapRow(resultSet, 0);

        // Assert
        assertEquals("", result.getContent());
    }

    @Test
    public void testMapRow_NullContent() throws SQLException {
        // Arrange
        setupBasicMessageFields();
        when(resultSet.getString("content")).thenReturn(null);
        when(resultSet.getLong("imageID")).thenReturn(0L);
        when(resultSet.wasNull()).thenReturn(true);

        // Act
        MessageDto result = messageRowMapper.mapRow(resultSet, 0);

        // Assert
        assertNull(result.getContent());
    }

    @Test
    public void testMapRow_LongContent() throws SQLException {
        // Arrange
        String longContent = "A".repeat(5000);
        setupBasicMessageFields();
        when(resultSet.getString("content")).thenReturn(longContent);
        when(resultSet.getLong("imageID")).thenReturn(0L);
        when(resultSet.wasNull()).thenReturn(true);

        // Act
        MessageDto result = messageRowMapper.mapRow(resultSet, 0);

        // Assert
        assertEquals(longContent, result.getContent());
        assertEquals(5000, result.getContent().length());
    }

    @Test
    public void testMapRow_DifferentRowNumbers() throws SQLException {
        // Arrange
        setupBasicMessageFields();
        when(resultSet.getLong("imageID")).thenReturn(0L);
        when(resultSet.wasNull()).thenReturn(true);

        // Act - test with different row numbers
        MessageDto result1 = messageRowMapper.mapRow(resultSet, 0);
        MessageDto result2 = messageRowMapper.mapRow(resultSet, 5);
        MessageDto result3 = messageRowMapper.mapRow(resultSet, 100);

        // Assert
        assertNotNull(result1);
        assertNotNull(result2);
        assertNotNull(result3);
        assertEquals(MESSAGE_ID, result1.getMessageId());
        assertEquals(MESSAGE_ID, result2.getMessageId());
        assertEquals(MESSAGE_ID, result3.getMessageId());
    }

    @Test
    public void testMapRow_ThrowsSQLException_OnMessageId() throws SQLException {
        // Arrange
        doThrow(new SQLException("Database error")).when(resultSet).getLong("messageId");

        // Act & Assert
        assertThrows(SQLException.class, () -> messageRowMapper.mapRow(resultSet, 0));
    }

    @Test
    public void testMapRow_ThrowsSQLException_OnContent() throws SQLException {
        // Arrange
        doReturn(MESSAGE_ID).when(resultSet).getLong("messageId");
        doReturn(CHAT_ID).when(resultSet).getLong("chatID");
        doReturn(SENDER_ID).when(resultSet).getLong("senderID");
        doThrow(new SQLException("Database error")).when(resultSet).getString("content");

        // Act & Assert
        assertThrows(SQLException.class, () -> messageRowMapper.mapRow(resultSet, 0));
    }

    @Test
    public void testMapRow_ThrowsSQLException_OnStatus() throws SQLException {
        // Arrange
        setupBasicMessageFieldsWithDoReturn();
        doThrow(new SQLException("Database error")).when(resultSet).getString("status");

        // Act & Assert
        assertThrows(SQLException.class, () -> messageRowMapper.mapRow(resultSet, 0));
    }


    @Test
    public void testMapRow_ZeroIds() throws SQLException {
        // Arrange
        when(resultSet.getLong("messageId")).thenReturn(0L);
        when(resultSet.getLong("chatID")).thenReturn(0L);
        when(resultSet.getLong("senderID")).thenReturn(0L);
        when(resultSet.getLong("receiverID")).thenReturn(0L);
        when(resultSet.getString("content")).thenReturn(CONTENT);
        when(resultSet.getTimestamp("timestamp")).thenReturn(Timestamp.valueOf(TEST_TIMESTAMP));
        when(resultSet.getBoolean("isUserSender")).thenReturn(true);
        when(resultSet.getString("status")).thenReturn("sent");
        when(resultSet.getLong("imageID")).thenReturn(0L);
        when(resultSet.wasNull()).thenReturn(true);

        // Act
        MessageDto result = messageRowMapper.mapRow(resultSet, 0);

        // Assert
        assertEquals(0L, result.getMessageId());
        assertEquals(0L, result.getChatId());
        assertEquals(0L, result.getSenderId());
        assertEquals(0L, result.getReceiverId());
    }

    @Test
    public void testMapRow_LargeImageData() throws SQLException {
        // Arrange
        setupBasicMessageFields();
        when(resultSet.getLong("imageID")).thenReturn(IMAGE_ID);
        when(resultSet.wasNull()).thenReturn(false);
        when(resultSet.getLong("messageId")).thenReturn(MESSAGE_ID);
        when(resultSet.getString("format")).thenReturn("jpeg");

        // Create large image data (1MB)
        String largeImage = Arrays.toString(new byte[505]);
        when(resultSet.getString("imageFile")).thenReturn(largeImage);
        when(resultSet.getString("imageName")).thenReturn("large_image.jpg");

        // Act
        MessageDto result = messageRowMapper.mapRow(resultSet, 0);

        // Assert
        assertNotNull(result.getImageDto());
        assertNotNull(result.getImageDto().getFileData());
    }

    // ==================== HELPER METHODS ====================

    private void setupBasicMessageFields() throws SQLException {
        when(resultSet.getLong("messageId")).thenReturn(MESSAGE_ID);
        when(resultSet.getLong("chatID")).thenReturn(CHAT_ID);
        when(resultSet.getLong("senderID")).thenReturn(SENDER_ID);
        when(resultSet.getLong("receiverID")).thenReturn(RECEIVER_ID);
        when(resultSet.getString("content")).thenReturn(CONTENT);
        when(resultSet.getTimestamp("timestamp")).thenReturn(Timestamp.valueOf(TEST_TIMESTAMP));
        when(resultSet.getBoolean("isUserSender")).thenReturn(true);
        when(resultSet.getString("status")).thenReturn("sent");
    }

    private void setupBasicMessageFieldsWithDoReturn() throws SQLException {
        doReturn(MESSAGE_ID).when(resultSet).getLong("messageId");
        doReturn(CHAT_ID).when(resultSet).getLong("chatID");
        doReturn(SENDER_ID).when(resultSet).getLong("senderID");
        doReturn(RECEIVER_ID).when(resultSet).getLong("receiverID");
        doReturn(CONTENT).when(resultSet).getString("content");
        doReturn(Timestamp.valueOf(TEST_TIMESTAMP)).when(resultSet).getTimestamp("timestamp");
        doReturn(true).when(resultSet).getBoolean("isUserSender");
    }

    private void setupImageFields() throws SQLException {
        when(resultSet.getLong("imageID")).thenReturn(IMAGE_ID);
        when(resultSet.wasNull()).thenReturn(false);
        when(resultSet.getLong("messageId")).thenReturn(MESSAGE_ID);
        when(resultSet.getString("format")).thenReturn("jpeg");

        String imageBytes = "test image data";
        when(resultSet.getString("imageFile")).thenReturn(imageBytes);
        when(resultSet.getString("imageName")).thenReturn("test_image.jpg");
    }
    @Test
    public void testMapRow_AllFieldsPresent() throws SQLException {
        // Arrange
        when(resultSet.getLong("chatID")).thenReturn(CHAT_ID);
        when(resultSet.getLong("userID")).thenReturn(USER_ID);
        when(resultSet.getLong("taskerID")).thenReturn(TASKER_ID);
        when(resultSet.getBoolean("taskerIsActive")).thenReturn(false);
        when(resultSet.getBoolean("userIsActive")).thenReturn(true);

        // Act
        ChatDto result = chatMapper.mapRow(resultSet, 0);

        // Assert
        assertNotNull(result);
        assertEquals(CHAT_ID, result.getChatId());
        assertEquals(USER_ID, result.getUserId());
        assertEquals(TASKER_ID, result.getTaskerId());
        assertTrue(result.isUserIsActive());
        assertFalse(result.isTaskerIsActive());

        // Verify all fields were accessed
        verify(resultSet).getLong("chatID");
        verify(resultSet).getLong("userID");
        verify(resultSet).getLong("taskerID");
        verify(resultSet).getBoolean("taskerIsActive");
        verify(resultSet).getBoolean("userIsActive");
    }

    @Test
    public void testMapRow_BothUsersActive() throws SQLException {
        // Arrange
        when(resultSet.getLong("chatID")).thenReturn(CHAT_ID);
        when(resultSet.getLong("userID")).thenReturn(USER_ID);
        when(resultSet.getLong("taskerID")).thenReturn(TASKER_ID);
        when(resultSet.getBoolean("taskerIsActive")).thenReturn(true);
        when(resultSet.getBoolean("userIsActive")).thenReturn(true);

        // Act
        ChatDto result = chatMapper.mapRow(resultSet, 0);

        // Assert
        assertTrue(result.isUserIsActive());
        assertTrue(result.isTaskerIsActive());
    }

    @Test
    public void testMapRow_BothUsersInactive() throws SQLException {
        // Arrange
        when(resultSet.getLong("chatID")).thenReturn(CHAT_ID);
        when(resultSet.getLong("userID")).thenReturn(USER_ID);
        when(resultSet.getLong("taskerID")).thenReturn(TASKER_ID);
        when(resultSet.getBoolean("taskerIsActive")).thenReturn(false);
        when(resultSet.getBoolean("userIsActive")).thenReturn(false);

        // Act
        ChatDto result = chatMapper.mapRow(resultSet, 0);

        // Assert
        assertFalse(result.isUserIsActive());
        assertFalse(result.isTaskerIsActive());
    }

    @Test
    public void testMapRow_DifferentRowNumber() throws SQLException {
        // Arrange
        when(resultSet.getLong("chatID")).thenReturn(CHAT_ID);
        when(resultSet.getLong("userID")).thenReturn(USER_ID);
        when(resultSet.getLong("taskerID")).thenReturn(TASKER_ID);
        when(resultSet.getBoolean("taskerIsActive")).thenReturn(true);
        when(resultSet.getBoolean("userIsActive")).thenReturn(true);

        // Act - test with different row numbers
        ChatDto result1 = chatMapper.mapRow(resultSet, 0);
        ChatDto result2 = chatMapper.mapRow(resultSet, 5);
        ChatDto result3 = chatMapper.mapRow(resultSet, 100);

        // Assert - all should produce valid results regardless of row number
        assertNotNull(result1);
        assertNotNull(result2);
        assertNotNull(result3);
        assertEquals(CHAT_ID, result1.getChatId());
        assertEquals(CHAT_ID, result2.getChatId());
        assertEquals(CHAT_ID, result3.getChatId());
    }


    @Test
    public void testMapRow_LargeIds() throws SQLException {
        // Arrange
        Long largeId = Long.MAX_VALUE;
        when(resultSet.getLong("chatID")).thenReturn(largeId);
        when(resultSet.getLong("userID")).thenReturn(largeId - 1);
        when(resultSet.getLong("taskerID")).thenReturn(largeId - 2);
        when(resultSet.getBoolean("taskerIsActive")).thenReturn(true);
        when(resultSet.getBoolean("userIsActive")).thenReturn(true);

        // Act
        ChatDto result = chatMapper.mapRow(resultSet, 0);

        // Assert
        assertEquals(largeId, result.getChatId());
        assertEquals(largeId - 1, result.getUserId());
        assertEquals(largeId - 2, result.getTaskerId());
    }

    @Test
    public void testMapRow_ThrowsSQLException_OnChatId() throws SQLException {
        // Arrange
        doThrow(new SQLException("Database error")).when(resultSet).getLong("chatID");

        // Act & Assert
        assertThrows(SQLException.class, () -> chatMapper.mapRow(resultSet, 0));
    }

    @Test
    public void testMapRow_ThrowsSQLException_OnUserId() throws SQLException {
        // Arrange
        doReturn(CHAT_ID).when(resultSet).getLong("chatID");
        doThrow(new SQLException("Database error")).when(resultSet).getLong("userID");

        // Act & Assert
        assertThrows(SQLException.class, () -> chatMapper.mapRow(resultSet, 0));
    }

    @Test
    public void testMapRow_ThrowsSQLException_OnTaskerId() throws SQLException {
        // Arrange
        doReturn(CHAT_ID).when(resultSet).getLong("chatID");
        doReturn(USER_ID).when(resultSet).getLong("userID");
        doThrow(new SQLException("Database error")).when(resultSet).getLong("taskerID");

        // Act & Assert
        assertThrows(SQLException.class, () -> chatMapper.mapRow(resultSet, 0));
    }

    @Test
    public void testMapRow_ThrowsSQLException_OnTaskerIsActive() throws SQLException {
        // Arrange
        doReturn(CHAT_ID).when(resultSet).getLong("chatID");
        doReturn(USER_ID).when(resultSet).getLong("userID");
        doReturn(TASKER_ID).when(resultSet).getLong("taskerID");
        doThrow(new SQLException("Database error")).when(resultSet).getBoolean("taskerIsActive");

        // Act & Assert
        assertThrows(SQLException.class, () -> chatMapper.mapRow(resultSet, 0));
    }

    @Test
    public void testMapRow_ThrowsSQLException_OnUserIsActive() throws SQLException {
        // Arrange
        doReturn(CHAT_ID).when(resultSet).getLong("chatID");
        doReturn(USER_ID).when(resultSet).getLong("userID");
        doReturn(TASKER_ID).when(resultSet).getLong("taskerID");
        doReturn(true).when(resultSet).getBoolean("taskerIsActive");
        doThrow(new SQLException("Database error")).when(resultSet).getBoolean("userIsActive");

        // Act & Assert
        assertThrows(SQLException.class, () -> chatMapper.mapRow(resultSet, 0));
    }
}