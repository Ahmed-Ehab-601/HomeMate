package com.homemate.chat;

import com.homemate.chat.dao.ChatDao;
import com.homemate.chat.dto.ChatDto;
import com.homemate.chat.rowMapper.ChatMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ChatDaoTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private ChatDao chatDao;

    private static final Long CHAT_ID = 1L;
    private static final Long USER_ID = 100L;
    private static final Long TASKER_ID = 200L;
    private ChatDto testChat;

    @BeforeEach
    public void setUp() {
        testChat = new ChatDto();
        testChat.setChatId(CHAT_ID);
        testChat.setUserId(USER_ID);
        testChat.setTaskerId(TASKER_ID);
    }

    // ==================== GET USER PHONE TESTS ====================

    @Test
    public void testGetUserPhone_Success() throws SQLException {
        String expectedPhone = "1234567890";
        String sql = "SELECT phone FROM Users u INNER JOIN Chat c on c.userID = u.userID WHERE c.chatID = ? ";

        when(jdbcTemplate.queryForObject(sql, String.class, CHAT_ID))
                .thenReturn(expectedPhone);

        String result = chatDao.getUserPhone(CHAT_ID);

        assertEquals(expectedPhone, result);
        verify(jdbcTemplate).queryForObject(sql, String.class, CHAT_ID);
    }

    @Test
    public void testGetUserPhone_ThrowsException() {
        String sql = "SELECT phone FROM Users u INNER JOIN Chat c on c.userID = u.userID WHERE c.chatID = ? ";

        when(jdbcTemplate.queryForObject(sql, String.class, CHAT_ID))
                .thenThrow(new RuntimeException("Database error"));

        assertThrows(RuntimeException.class, () -> chatDao.getUserPhone(CHAT_ID));
    }

    // ==================== GET TASKER PHONE TESTS ====================

    @Test
    public void testGetTaskerPhone_Success() throws SQLException {
        String expectedPhone = "0987654321";
        String sql = "SELECT phone FROM Tasker t INNER JOIN Chat c on c.taskerID = t.taskerID WHERE c.chatID = ? ";

        when(jdbcTemplate.queryForObject(sql, String.class, CHAT_ID))
                .thenReturn(expectedPhone);

        String result = chatDao.getTaskerPhone(CHAT_ID);

        assertEquals(expectedPhone, result);
        verify(jdbcTemplate).queryForObject(sql, String.class, CHAT_ID);
    }

    @Test
    public void testGetTaskerPhone_ThrowsException() {
        String sql = "SELECT phone FROM Tasker t INNER JOIN Chat c on c.taskerID = t.taskerID WHERE c.chatID = ? ";

        when(jdbcTemplate.queryForObject(sql, String.class, CHAT_ID))
                .thenThrow(new RuntimeException("Database error"));

        assertThrows(RuntimeException.class, () -> chatDao.getTaskerPhone(CHAT_ID));
    }

    // ==================== GET CHAT TESTS ====================

    @Test
    public void testGetChat_Success() throws SQLException {
        String sql = "SELECT * FROM Chat WHERE chatID = ?";

        when(jdbcTemplate.queryForObject(eq(sql), any(ChatMapper.class), eq(CHAT_ID)))
                .thenReturn(testChat);

        ChatDto result = chatDao.getChat(CHAT_ID);

        assertNotNull(result);
        assertEquals(CHAT_ID, result.getChatId());
        assertEquals(USER_ID, result.getUserId());
        assertEquals(TASKER_ID, result.getTaskerId());
        verify(jdbcTemplate).queryForObject(eq(sql), any(ChatMapper.class), eq(CHAT_ID));
    }

    @Test
    public void testGetChat_NotFound() {
        String sql = "SELECT * FROM Chat WHERE chatID = ?";

        when(jdbcTemplate.queryForObject(eq(sql), any(ChatMapper.class), eq(CHAT_ID)))
                .thenThrow(new RuntimeException("Chat not found"));

        assertThrows(RuntimeException.class, () -> chatDao.getChat(CHAT_ID));
    }

    // ==================== GET NAME TESTS ====================

    @Test
    public void testGetNameTasker_Success() {
        String expectedUsername = "tasker_john";
        String sql = "SELECT username FROM Tasker WHERE taskerID = ?";

        when(jdbcTemplate.queryForObject(sql, String.class, TASKER_ID))
                .thenReturn(expectedUsername);

        String result = chatDao.getNameTasker(TASKER_ID);

        assertEquals(expectedUsername, result);
        verify(jdbcTemplate).queryForObject(sql, String.class, TASKER_ID);
    }

    @Test
    public void testGetNameUser_Success() {
        String expectedUsername = "user_jane";
        String sql = "SELECT username FROM Users WHERE userID = ?";

        when(jdbcTemplate.queryForObject(sql, String.class, USER_ID))
                .thenReturn(expectedUsername);

        String result = chatDao.getNameUser(USER_ID);

        assertEquals(expectedUsername, result);
        verify(jdbcTemplate).queryForObject(sql, String.class, USER_ID);
    }

    // ==================== CHANGE STATUS TESTS ====================

    @Test
    public void testChangeTaskerStatus_Success() throws SQLException {
        String sql = "UPDATE Chat SET taskerIsActive = FALSE where taskerID = ?";

        when(jdbcTemplate.update(sql, TASKER_ID)).thenReturn(1);

        chatDao.changeTaskerStatus(TASKER_ID);

        verify(jdbcTemplate).update(sql, TASKER_ID);
    }

    @Test
    public void testChangeUserStatus_Success() throws SQLException {
        String sql = "UPDATE Chat SET userIsActive = FALSE where userID = ?";

        when(jdbcTemplate.update(sql, USER_ID)).thenReturn(1);

        chatDao.changeUserStatus(USER_ID);

        verify(jdbcTemplate).update(sql, USER_ID);
    }

    // ==================== SET ONLINE TESTS ====================

    @Test
    public void testSetTaskerOnline_Success() throws SQLException {
        String sql = "UPDATE Chat SET taskerIsActive = TRUE WHERE taskerID = ?";

        when(jdbcTemplate.update(sql, TASKER_ID)).thenReturn(1);

        chatDao.setTaskerOnline(TASKER_ID);

        verify(jdbcTemplate).update(sql, TASKER_ID);
    }

    @Test
    public void testSetUserOnline_Success() throws SQLException {
        String sql = "UPDATE Chat SET userIsActive = TRUE WHERE userID = ?";

        when(jdbcTemplate.update(sql, USER_ID)).thenReturn(1);

        chatDao.setUserOnline(USER_ID);

        verify(jdbcTemplate).update(sql, USER_ID);
    }

    // ==================== GET CHAT BY ID TESTS ====================

    @Test
    public void testGetChatByTasker_Success() throws SQLException {
        String sql = "SELECT chatID FROM Chat WHERE taskerID = ?";

        when(jdbcTemplate.queryForObject(sql, Long.class, TASKER_ID))
                .thenReturn(CHAT_ID);

        Long result = chatDao.getchatbytasker(TASKER_ID);

        assertEquals(CHAT_ID, result);
        verify(jdbcTemplate).queryForObject(sql, Long.class, TASKER_ID);
    }

    @Test
    public void testGetChatByTasker_NotFound() {
        String sql = "SELECT chatID FROM Chat WHERE taskerID = ?";

        when(jdbcTemplate.queryForObject(sql, Long.class, TASKER_ID))
                .thenThrow(new RuntimeException("Chat not found"));

        assertThrows(RuntimeException.class, () -> chatDao.getchatbytasker(TASKER_ID));
    }

    @Test
    public void testGetChatByUser_Success() throws SQLException {
        String sql = "SELECT chatID FROM Chat WHERE userID = ?";

        when(jdbcTemplate.queryForObject(sql, Long.class, USER_ID))
                .thenReturn(CHAT_ID);

        Long result = chatDao.getchatbyuser(USER_ID);

        assertEquals(CHAT_ID, result);
        verify(jdbcTemplate).queryForObject(sql, Long.class, USER_ID);
    }

    @Test
    public void testGetChatByUser_NotFound() {
        String sql = "SELECT chatID FROM Chat WHERE userID = ?";

        when(jdbcTemplate.queryForObject(sql, Long.class, USER_ID))
                .thenThrow(new RuntimeException("Chat not found"));

        assertThrows(RuntimeException.class, () -> chatDao.getchatbyuser(USER_ID));
    }
}