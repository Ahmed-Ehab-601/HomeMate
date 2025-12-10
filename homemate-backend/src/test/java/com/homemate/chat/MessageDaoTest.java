package com.homemate.chat;

import com.homemate.chat.Enum.MessageStatus;
import com.homemate.chat.dao.MessageDao;
import com.homemate.chat.dto.ImageDto;
import com.homemate.chat.dto.MessageDto;
import com.homemate.chat.rowMapper.MessageRowMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MessageDaoTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private MessageDao messageDao;

    private static final Long CHAT_ID = 1L;
    private static final Long MESSAGE_ID = 10L;
    private static final Long USER_ID = 100L;
    private static final Long TASKER_ID = 200L;

    private MessageDto testMessage;

    @BeforeEach
    public void setUp() {
        testMessage = MessageDto.builder()
                .messageId(MESSAGE_ID)
                .chatId(CHAT_ID)
                .senderId(USER_ID)
                .receiverId(TASKER_ID)
                .content("Test message")
                .IsUserSender(true)
                .messageStatus(MessageStatus.sent)
                .timestamp(LocalDateTime.now())
                .build();
    }

    // ==================== GET ALL MESSAGES TESTS ====================

    @Test
    public void testGetAllMessages_Success() {
        int page = 0;
        int size = 10;
        int offset = 0;

        List<MessageDto> expectedMessages = Arrays.asList(
                testMessage,
                MessageDto.builder()
                        .messageId(11L)
                        .content("Another message")
                        .build()
        );

        String sql = "SELECT * " +
                "FROM Message m " +
                "LEFT JOIN MessageImage mi ON m.messageId = mi.messageID " +
                "WHERE m.chatID = ? " +
                "ORDER BY m.timestamp DESC " +
                "LIMIT ? OFFSET ?";

        when(jdbcTemplate.query(eq(sql), any(Object[].class), any(MessageRowMapper.class)))
                .thenReturn(expectedMessages);

        List<MessageDto> result = messageDao.getAllMessages(CHAT_ID, page, size);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(jdbcTemplate).query(eq(sql), any(Object[].class), any(MessageRowMapper.class));
    }

    @Test
    public void testGetAllMessages_WithPagination() {
        int page = 2;
        int size = 5;
        int offset = 10;

        List<MessageDto> expectedMessages = Arrays.asList(testMessage);

        when(jdbcTemplate.query(anyString(), any(Object[].class), any(MessageRowMapper.class)))
                .thenReturn(expectedMessages);

        List<MessageDto> result = messageDao.getAllMessages(CHAT_ID, page, size);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    public void testGetAllMessages_EmptyResult() {
        when(jdbcTemplate.query(anyString(), any(Object[].class), any(MessageRowMapper.class)))
                .thenReturn(Arrays.asList());

        List<MessageDto> result = messageDao.getAllMessages(CHAT_ID, 0, 10);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ==================== GET TOTAL COUNT TESTS ====================

    @Test
    public void testGetTotalCount_Success() {
        String sql = "SELECT COUNT(*) FROM Message WHERE chatID = ?";
        when(jdbcTemplate.queryForObject(sql, Integer.class, CHAT_ID))
                .thenReturn(25);

        Long result = messageDao.getTotalCount(CHAT_ID);

        assertEquals(25L, result);
        verify(jdbcTemplate).queryForObject(sql, Integer.class, CHAT_ID);
    }

    @Test
    public void testGetTotalCount_Zero() {
        String sql = "SELECT COUNT(*) FROM Message WHERE chatID = ?";
        when(jdbcTemplate.queryForObject(sql, Integer.class, CHAT_ID))
                .thenReturn(0);

        Long result = messageDao.getTotalCount(CHAT_ID);

        assertEquals(0L, result);
    }

    @Test
    public void testGetTotalCount_NullReturnsZero() {
        String sql = "SELECT COUNT(*) FROM Message WHERE chatID = ?";
        when(jdbcTemplate.queryForObject(sql, Integer.class, CHAT_ID))
                .thenReturn(null);

        Long result = messageDao.getTotalCount(CHAT_ID);

        assertEquals(0L, result);
    }

    // ==================== SAVE MESSAGE TESTS ====================

    @Test
    public void testSave_MessageWithoutImage() {
        MessageDto messageToSave = MessageDto.builder()
                .chatId(CHAT_ID)
                .senderId(USER_ID)
                .receiverId(TASKER_ID)
                .content("New message")
                .IsUserSender(true)
                .messageStatus(MessageStatus.sent)
                .build();

        when(jdbcTemplate.update(any(PreparedStatementCreator.class), any(KeyHolder.class)))
                .thenAnswer(invocation -> {
                    KeyHolder keyHolder = invocation.getArgument(1);
                    ((GeneratedKeyHolder) keyHolder).getKeyList().add(
                            java.util.Collections.singletonMap("GENERATED_KEY", MESSAGE_ID)
                    );
                    return 1;
                });

        when(jdbcTemplate.queryForObject(anyString(), any(MessageRowMapper.class), eq(MESSAGE_ID)))
                .thenReturn(testMessage);

        MessageDto result = messageDao.save(CHAT_ID, messageToSave);

        assertNotNull(result);
        assertEquals(MESSAGE_ID, result.getMessageId());
        verify(jdbcTemplate).update(any(PreparedStatementCreator.class), any(KeyHolder.class));
        verify(jdbcTemplate).queryForObject(anyString(), any(MessageRowMapper.class), eq(MESSAGE_ID));
    }

    @Test
    public void testSave_MessageWithImage() {
        ImageDto imageDto = new ImageDto();
        imageDto.setFileName("test.jpg");
        imageDto.setFileFormat("jpeg");
        imageDto.setFileData(Base64.getEncoder().encodeToString("test image data".getBytes()));

        MessageDto messageWithImage = MessageDto.builder()
                .chatId(CHAT_ID)
                .senderId(USER_ID)
                .receiverId(TASKER_ID)
                .content("Message with image")
                .IsUserSender(true)
                .messageStatus(MessageStatus.sent)
                .imageDto(imageDto)
                .build();

        when(jdbcTemplate.update(any(PreparedStatementCreator.class), any(KeyHolder.class)))
                .thenAnswer(invocation -> {
                    KeyHolder keyHolder = invocation.getArgument(1);
                    ((GeneratedKeyHolder) keyHolder).getKeyList().add(
                            java.util.Collections.singletonMap("GENERATED_KEY", MESSAGE_ID)
                    );
                    return 1;
                });

        when(jdbcTemplate.update(anyString(), any(), any(), any(), any()))
                .thenReturn(1);

        when(jdbcTemplate.queryForObject(anyString(), any(MessageRowMapper.class), eq(MESSAGE_ID)))
                .thenReturn(testMessage);

        MessageDto result = messageDao.save(CHAT_ID, messageWithImage);

        assertNotNull(result);
        verify(jdbcTemplate).update(anyString(), eq(MESSAGE_ID), anyString(), any(), anyString());
    }

    @Test
    public void testSave_MessageWithTimestamp() {
        LocalDateTime customTimestamp = LocalDateTime.of(2024, 1, 1, 12, 0);
        MessageDto messageWithTimestamp = MessageDto.builder()
                .chatId(CHAT_ID)
                .senderId(USER_ID)
                .receiverId(TASKER_ID)
                .content("Message with timestamp")
                .IsUserSender(true)
                .timestamp(customTimestamp)
                .build();

        when(jdbcTemplate.update(any(PreparedStatementCreator.class), any(KeyHolder.class)))
                .thenAnswer(invocation -> {
                    KeyHolder keyHolder = invocation.getArgument(1);
                    ((GeneratedKeyHolder) keyHolder).getKeyList().add(
                            java.util.Collections.singletonMap("GENERATED_KEY", MESSAGE_ID)
                    );
                    return 1;
                });

        when(jdbcTemplate.queryForObject(anyString(), any(MessageRowMapper.class), eq(MESSAGE_ID)))
                .thenReturn(testMessage);

        MessageDto result = messageDao.save(CHAT_ID, messageWithTimestamp);

        assertNotNull(result);
        assertEquals(MESSAGE_ID, result.getMessageId());
    }

    // ==================== UNREAD MESSAGES TESTS ====================

    @Test
    public void testGetUnreadOnesForUser_Success() throws SQLException {
        String sql = "SELECT COUNT(*) FROM Message WHERE chatID = ? AND status != 'seen' AND isUserSender = FALSE";
        when(jdbcTemplate.queryForObject(sql, Integer.class, CHAT_ID))
                .thenReturn(5);

        int result = messageDao.getUnreadOnesForUser(CHAT_ID);

        assertEquals(5, result);
        verify(jdbcTemplate).queryForObject(sql, Integer.class, CHAT_ID);
    }

    @Test
    public void testGetUnreadOnesForUser_Zero() throws SQLException {
        String sql = "SELECT COUNT(*) FROM Message WHERE chatID = ? AND status != 'seen' AND isUserSender = FALSE";
        when(jdbcTemplate.queryForObject(sql, Integer.class, CHAT_ID))
                .thenReturn(0);

        int result = messageDao.getUnreadOnesForUser(CHAT_ID);

        assertEquals(0, result);
    }

    @Test
    public void testGetUnreadOnesForTasker_Success() throws SQLException {
        String sql = "SELECT COUNT(*) FROM Message WHERE chatID = ? AND status != 'seen' AND isUserSender = TRUE";
        when(jdbcTemplate.queryForObject(sql, Integer.class, CHAT_ID))
                .thenReturn(3);

        int result = messageDao.getUnreadOnesForTasker(CHAT_ID);

        assertEquals(3, result);
        verify(jdbcTemplate).queryForObject(sql, Integer.class, CHAT_ID);
    }

    @Test
    public void testGetUnreadOnesForTasker_Zero() throws SQLException {
        String sql = "SELECT COUNT(*) FROM Message WHERE chatID = ? AND status != 'seen' AND isUserSender = TRUE";
        when(jdbcTemplate.queryForObject(sql, Integer.class, CHAT_ID))
                .thenReturn(0);

        int result = messageDao.getUnreadOnesForTasker(CHAT_ID);

        assertEquals(0, result);
    }

    // ==================== MARK AS READ TESTS ====================

    @Test
    public void testMarkAsReadUser_Success() throws SQLException {
        String sql = "UPDATE Message SET status = 'seen' WHERE chatID = ? AND isUserSender= FALSE";
        when(jdbcTemplate.update(sql, CHAT_ID)).thenReturn(5);

        messageDao.markasReadUser(CHAT_ID);

        verify(jdbcTemplate).update(sql, CHAT_ID);
    }

    @Test
    public void testMarkAsReadTasker_Success() throws SQLException {
        String sql = "UPDATE Message SET status = 'seen' WHERE chatID = ? AND isUserSender = TRUE";
        when(jdbcTemplate.update(sql, CHAT_ID)).thenReturn(3);

        messageDao.markasReadTasker(CHAT_ID);

        verify(jdbcTemplate).update(sql, CHAT_ID);
    }

    // ==================== MARK AS RECEIVED TESTS ====================

    @Test
    public void testMarkAsReceivedUser_Success() {
        String sql = "UPDATE Message m " +
                "JOIN Chat c ON m.chatID = c.chatID " +
                "SET m.status = 'received' " +
                "WHERE c.userID = ? AND m.isUserSender = FALSE AND m.status = 'sent'";

        when(jdbcTemplate.update(sql, USER_ID)).thenReturn(2);

        messageDao.markasReceivedUser(USER_ID);

        verify(jdbcTemplate).update(sql, USER_ID);
    }

    @Test
    public void testMarkAsReceivedUser_NoMessagesToUpdate() {
        String sql = "UPDATE Message m " +
                "JOIN Chat c ON m.chatID = c.chatID " +
                "SET m.status = 'received' " +
                "WHERE c.userID = ? AND m.isUserSender = FALSE AND m.status = 'sent'";

        when(jdbcTemplate.update(sql, USER_ID)).thenReturn(0);

        messageDao.markasReceivedUser(USER_ID);

        verify(jdbcTemplate).update(sql, USER_ID);
    }

    @Test
    public void testMarkAsReceivedTasker_Success() {
        String sql = "UPDATE Message m " +
                "JOIN Chat c ON m.chatID = c.chatID " +
                "SET m.status = 'received' " +
                "WHERE c.taskerID = ? AND m.isUserSender = TRUE AND m.status = 'sent'";

        when(jdbcTemplate.update(sql, TASKER_ID)).thenReturn(4);

        messageDao.markasReceivedTasker(TASKER_ID);

        verify(jdbcTemplate).update(sql, TASKER_ID);
    }

    @Test
    public void testMarkAsReceivedTasker_NoMessagesToUpdate() {
        String sql = "UPDATE Message m " +
                "JOIN Chat c ON m.chatID = c.chatID " +
                "SET m.status = 'received' " +
                "WHERE c.taskerID = ? AND m.isUserSender = TRUE AND m.status = 'sent'";

        when(jdbcTemplate.update(sql, TASKER_ID)).thenReturn(0);

        messageDao.markasReceivedTasker(TASKER_ID);

        verify(jdbcTemplate).update(sql, TASKER_ID);
    }

    // ==================== GET STATUS TESTS ====================

    @Test
    public void testGetUserStatus_Active() {
        String sql = "SELECT userIsActive FROM Chat WHERE chatID = ?";
        when(jdbcTemplate.queryForObject(sql, Boolean.class, CHAT_ID))
                .thenReturn(true);

        Boolean result = messageDao.getUserStatus(CHAT_ID);

        assertTrue(result);
        verify(jdbcTemplate).queryForObject(sql, Boolean.class, CHAT_ID);
    }

    @Test
    public void testGetUserStatus_Inactive() {
        String sql = "SELECT userIsActive FROM Chat WHERE chatID = ?";
        when(jdbcTemplate.queryForObject(sql, Boolean.class, CHAT_ID))
                .thenReturn(false);

        Boolean result = messageDao.getUserStatus(CHAT_ID);

        assertFalse(result);
        verify(jdbcTemplate).queryForObject(sql, Boolean.class, CHAT_ID);
    }

    @Test
    public void testGetTaskerStatus_Active() {
        String sql = "SELECT taskerIsActive FROM Chat WHERE chatID = ?";
        when(jdbcTemplate.queryForObject(sql, Boolean.class, CHAT_ID))
                .thenReturn(true);

        Boolean result = messageDao.getTaskerStatus(CHAT_ID);

        assertTrue(result);
        verify(jdbcTemplate).queryForObject(sql, Boolean.class, CHAT_ID);
    }

    @Test
    public void testGetTaskerStatus_Inactive() {
        String sql = "SELECT taskerIsActive FROM Chat WHERE chatID = ?";
        when(jdbcTemplate.queryForObject(sql, Boolean.class, CHAT_ID))
                .thenReturn(false);

        Boolean result = messageDao.getTaskerStatus(CHAT_ID);

        assertFalse(result);
        verify(jdbcTemplate).queryForObject(sql, Boolean.class, CHAT_ID);
    }

    // ==================== EXCEPTION HANDLING TESTS ====================

    @Test
    public void testGetAllMessages_ThrowsException() {
        when(jdbcTemplate.query(anyString(), any(Object[].class), any(MessageRowMapper.class)))
                .thenThrow(new RuntimeException("Database error"));

        assertThrows(RuntimeException.class,
                () -> messageDao.getAllMessages(CHAT_ID, 0, 10));
    }

    @Test
    public void testMarkAsReadUser_ThrowsException() {
        String sql = "UPDATE Message SET status = 'seen' WHERE chatID = ? AND isUserSender= FALSE";
        when(jdbcTemplate.update(sql, CHAT_ID))
                .thenThrow(new RuntimeException("Database error"));

        assertThrows(RuntimeException.class,
                () -> messageDao.markasReadUser(CHAT_ID));
    }
}