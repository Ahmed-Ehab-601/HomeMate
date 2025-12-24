package com.homemate.chat;

import com.homemate.chat.dao.MessageDao;
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
public class MessageUnreadTest {
        @Mock
        private JdbcTemplate jdbcTemplate;

        @InjectMocks
        private MessageDao messageDao;

        private static final Long TEST_USER_ID = 1L;
        private static final Long TEST_TASKER_ID = 2L;

        @BeforeEach
        void setUp() {
            // Any common setup can go here
        }

        @Test
        public void testGetUnreadOnesForUser_Success() throws SQLException {
            // Arrange
            String updateSql = "UPDATE Chat c " +
                    "SET userUnreadMessages = (" +
                    " SELECT COUNT(*)" +
                    " FROM Message m" +
                    " WHERE m.chatID = c.chatID" +
                    " AND m.status IN ('sent', 'received')" +
                    " AND m.isUserSender = FALSE" +
                    " )" +
                    " WHERE c.userID = ?";

            String checkSql = "SELECT EXISTS (" +
                    "    SELECT 1" +
                    "    FROM Chat c" +
                    "    WHERE c.userID = ?" +
                    "    AND c.userUnreadMessages > 0" +
                    " )";

            // Mock update operation (returns number of rows affected)
            when(jdbcTemplate.update(eq(updateSql), eq(TEST_USER_ID)))
                    .thenReturn(1);

            // Mock check operation (returns true when unread messages exist)
            when(jdbcTemplate.queryForObject(eq(checkSql), eq(Boolean.class), eq(TEST_USER_ID)))
                    .thenReturn(true);

            // Act
            Boolean result = messageDao.getUnreadOnesForUser(TEST_USER_ID);

            // Assert
            assertTrue(result, "Should return true when user has unread messages");
            verify(jdbcTemplate, times(1)).update(eq(updateSql), eq(TEST_USER_ID));
            verify(jdbcTemplate, times(1)).queryForObject(eq(checkSql), eq(Boolean.class), eq(TEST_USER_ID));
        }

        @Test
        public void testGetUnreadOnesForUser_Zero() throws SQLException {
            // Arrange
            String updateSql = "UPDATE Chat c " +
                    "SET userUnreadMessages = (" +
                    " SELECT COUNT(*)" +
                    " FROM Message m" +
                    " WHERE m.chatID = c.chatID" +
                    " AND m.status IN ('sent', 'received')" +
                    " AND m.isUserSender = FALSE" +
                    " )" +
                    " WHERE c.userID = ?";

            String checkSql = "SELECT EXISTS (" +
                    "    SELECT 1" +
                    "    FROM Chat c" +
                    "    WHERE c.userID = ?" +
                    "    AND c.userUnreadMessages > 0" +
                    " )";

            // Mock update operation
            when(jdbcTemplate.update(eq(updateSql), eq(TEST_USER_ID)))
                    .thenReturn(1);

            // Mock check operation (returns false when no unread messages)
            when(jdbcTemplate.queryForObject(eq(checkSql), eq(Boolean.class), eq(TEST_USER_ID)))
                    .thenReturn(false);

            // Act
            Boolean result = messageDao.getUnreadOnesForUser(TEST_USER_ID);

            // Assert
            assertFalse(result, "Should return false when user has no unread messages");
            verify(jdbcTemplate, times(1)).update(eq(updateSql), eq(TEST_USER_ID));
            verify(jdbcTemplate, times(1)).queryForObject(eq(checkSql), eq(Boolean.class), eq(TEST_USER_ID));
        }

        @Test
        public void testGetUnreadOnesForTasker_Success() throws SQLException {
            // Arrange
            String updateSql = "UPDATE Chat c " +
                    "SET taskerUnreadMessages = (" +
                    " SELECT COUNT(*)" +
                    " FROM Message m" +
                    " WHERE m.chatID = c.chatID" +
                    " AND m.status IN ('sent', 'received')" +
                    " AND m.isUserSender = TRUE" +
                    " )" +
                    " WHERE c.taskerID = ?";

            String checkSql = "SELECT EXISTS (" +
                    "    SELECT 1" +
                    "    FROM Chat c" +
                    "    WHERE c.taskerID = ?" +
                    "    AND c.taskerUnreadMessages > 0" +
                    " )";

            // Mock update operation
            when(jdbcTemplate.update(eq(updateSql), eq(TEST_TASKER_ID)))
                    .thenReturn(1);

            // Mock check operation (returns true when unread messages exist)
            when(jdbcTemplate.queryForObject(eq(checkSql), eq(Boolean.class), eq(TEST_TASKER_ID)))
                    .thenReturn(true);

            // Act
            Boolean result = messageDao.getUnreadOnesForTasker(TEST_TASKER_ID);

            // Assert
            assertTrue(result, "Should return true when tasker has unread messages");
            verify(jdbcTemplate, times(1)).update(eq(updateSql), eq(TEST_TASKER_ID));
            verify(jdbcTemplate, times(1)).queryForObject(eq(checkSql), eq(Boolean.class), eq(TEST_TASKER_ID));
        }

        @Test
        public void testGetUnreadOnesForTasker_Zero() throws SQLException {
            // Arrange
            String updateSql = "UPDATE Chat c " +
                    "SET taskerUnreadMessages = (" +
                    " SELECT COUNT(*)" +
                    " FROM Message m" +
                    " WHERE m.chatID = c.chatID" +
                    " AND m.status IN ('sent', 'received')" +
                    " AND m.isUserSender = TRUE" +
                    " )" +
                    " WHERE c.taskerID = ?";

            String checkSql = "SELECT EXISTS (" +
                    "    SELECT 1" +
                    "    FROM Chat c" +
                    "    WHERE c.taskerID = ?" +
                    "    AND c.taskerUnreadMessages > 0" +
                    " )";

            // Mock update operation
            when(jdbcTemplate.update(eq(updateSql), eq(TEST_TASKER_ID)))
                    .thenReturn(1);

            // Mock check operation (returns false when no unread messages)
            when(jdbcTemplate.queryForObject(eq(checkSql), eq(Boolean.class), eq(TEST_TASKER_ID)))
                    .thenReturn(false);

            // Act
            Boolean result = messageDao.getUnreadOnesForTasker(TEST_TASKER_ID);

            // Assert
            assertFalse(result, "Should return false when tasker has no unread messages");
            verify(jdbcTemplate, times(1)).update(eq(updateSql), eq(TEST_TASKER_ID));
            verify(jdbcTemplate, times(1)).queryForObject(eq(checkSql), eq(Boolean.class), eq(TEST_TASKER_ID));
        }

        // Additional edge case tests

        @Test
        public void testGetUnreadOnesForUser_NullResult() throws SQLException {
            // Arrange
            String updateSql = "UPDATE Chat c " +
                    "SET userUnreadMessages = (" +
                    " SELECT COUNT(*)" +
                    " FROM Message m" +
                    " WHERE m.chatID = c.chatID" +
                    " AND m.status IN ('sent', 'received')" +
                    " AND m.isUserSender = FALSE" +
                    " )" +
                    " WHERE c.userID = ?";

            String checkSql = "SELECT EXISTS (" +
                    "    SELECT 1" +
                    "    FROM Chat c" +
                    "    WHERE c.userID = ?" +
                    "    AND c.userUnreadMessages > 0" +
                    " )";

            when(jdbcTemplate.update(eq(updateSql), eq(TEST_USER_ID)))
                    .thenReturn(1);

            // Mock returns null
            when(jdbcTemplate.queryForObject(eq(checkSql), eq(Boolean.class), eq(TEST_USER_ID)))
                    .thenReturn(null);

            // Act
            Boolean result = messageDao.getUnreadOnesForUser(TEST_USER_ID);

            // Assert
            assertNull(result, "Should return null when query returns null");
            verify(jdbcTemplate, times(1)).update(eq(updateSql), eq(TEST_USER_ID));
            verify(jdbcTemplate, times(1)).queryForObject(eq(checkSql), eq(Boolean.class), eq(TEST_USER_ID));
        }

        @Test
        public void testGetUnreadOnesForTasker_NullResult() throws SQLException {
            // Arrange
            String updateSql = "UPDATE Chat c " +
                    "SET taskerUnreadMessages = (" +
                    " SELECT COUNT(*)" +
                    " FROM Message m" +
                    " WHERE m.chatID = c.chatID" +
                    " AND m.status IN ('sent', 'received')" +
                    " AND m.isUserSender = TRUE" +
                    " )" +
                    " WHERE c.taskerID = ?";

            String checkSql = "SELECT EXISTS (" +
                    "    SELECT 1" +
                    "    FROM Chat c" +
                    "    WHERE c.taskerID = ?" +
                    "    AND c.taskerUnreadMessages > 0" +
                    " )";

            when(jdbcTemplate.update(eq(updateSql), eq(TEST_TASKER_ID)))
                    .thenReturn(1);

            // Mock returns null
            when(jdbcTemplate.queryForObject(eq(checkSql), eq(Boolean.class), eq(TEST_TASKER_ID)))
                    .thenReturn(null);

            // Act
            Boolean result = messageDao.getUnreadOnesForTasker(TEST_TASKER_ID);

            // Assert
            assertNull(result, "Should return null when query returns null");
            verify(jdbcTemplate, times(1)).update(eq(updateSql), eq(TEST_TASKER_ID));
            verify(jdbcTemplate, times(1)).queryForObject(eq(checkSql), eq(Boolean.class), eq(TEST_TASKER_ID));
        }

        @Test
        public void testGetUnreadOnesForUser_UpdateFails() throws SQLException {
            // Arrange
            String updateSql = "UPDATE Chat c " +
                    "SET userUnreadMessages = (" +
                    " SELECT COUNT(*)" +
                    " FROM Message m" +
                    " WHERE m.chatID = c.chatID" +
                    " AND m.status IN ('sent', 'received')" +
                    " AND m.isUserSender = FALSE" +
                    " )" +
                    " WHERE c.userID = ?";

            // Mock update operation throws exception
            when(jdbcTemplate.update(eq(updateSql), eq(TEST_USER_ID)))
                    .thenThrow(new RuntimeException("Database error"));

            // Act & Assert
            assertThrows(RuntimeException.class, () -> {
                messageDao.getUnreadOnesForUser(TEST_USER_ID);
            }, "Should throw exception when update fails");

            verify(jdbcTemplate, times(1)).update(eq(updateSql), eq(TEST_USER_ID));
            verify(jdbcTemplate, never()).queryForObject(anyString(), eq(Boolean.class), anyLong());
        }

        @Test
        public void testGetUnreadOnesForTasker_UpdateFails() throws SQLException {
            // Arrange
            String updateSql = "UPDATE Chat c " +
                    "SET taskerUnreadMessages = (" +
                    " SELECT COUNT(*)" +
                    " FROM Message m" +
                    " WHERE m.chatID = c.chatID" +
                    " AND m.status IN ('sent', 'received')" +
                    " AND m.isUserSender = TRUE" +
                    " )" +
                    " WHERE c.taskerID = ?";

            // Mock update operation throws exception
            when(jdbcTemplate.update(eq(updateSql), eq(TEST_TASKER_ID)))
                    .thenThrow(new RuntimeException("Database error"));

            // Act & Assert
            assertThrows(RuntimeException.class, () -> {
                messageDao.getUnreadOnesForTasker(TEST_TASKER_ID);
            }, "Should throw exception when update fails");

            verify(jdbcTemplate, times(1)).update(eq(updateSql), eq(TEST_TASKER_ID));
            verify(jdbcTemplate, never()).queryForObject(anyString(), eq(Boolean.class), anyLong());
        }
    
}
