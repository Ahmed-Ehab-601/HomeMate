package com.homemate.chat;

import com.homemate.chat.Enum.MessageStatus;
import com.homemate.chat.Service.ChatService;
import com.homemate.chat.Service.MessageService;
import com.homemate.chat.dao.ChatDao;
import com.homemate.chat.dao.MessageDao;
import com.homemate.chat.dto.ChatDto;
import com.homemate.chat.dto.MessageDto;
import com.homemate.chat.dto.PaginatedResponse;
import com.homemate.security.model.AppUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MessageTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private MessageDao messageDao;

    @Mock
    private ChatDao chatDao;

    @Mock
    private AppUserDetails userDetails;

    @InjectMocks
    private MessageService messageService;

    @InjectMocks
    private ChatService chatService;

    private ChatDto testChat;
    private static final Long CHAT_ID = 1L;
    private static final Long USER_ID = 1L;
    private static final Long TASKER_ID = 2L;
    private static final Long UNAUTHORIZED_ID = 999L;
    private static final Long MESSAGE_ID = 10L;
    private MessageDto testMessage;
    @BeforeEach
    public void setUp() {
        testChat = new ChatDto();
        testChat.setChatId(CHAT_ID);
        testChat.setUserId(USER_ID);
        testChat.setTaskerId(TASKER_ID);
        testMessage = MessageDto.builder()
                .messageId(MESSAGE_ID)
                .senderId(USER_ID)
                .receiverId(TASKER_ID)
                .content("Test message")
                .IsUserSender(true)
                .messageStatus(MessageStatus.sent)
                .build();
    }

    // ==================== SEND MESSAGE TESTS ====================

    @Test
    public void sendMessageTest() throws Exception {
        when(chatDao.getChat(CHAT_ID)).thenReturn(testChat);
        when(userDetails.getId()).thenReturn(USER_ID);

        MessageDto messageDto = MessageDto.builder()
                .senderId(USER_ID)
                .receiverId(TASKER_ID)
                .IsUserSender(true)
                .content("Hello")
                .build();
        when(messageDao.getTaskerStatus(CHAT_ID)).thenReturn(true);

        MessageDto savedMessage = MessageDto.builder()
                .messageId(10L)
                .senderId(USER_ID)
                .receiverId(TASKER_ID)
                .content("Hello")
                .IsUserSender(true)
                .messageStatus(MessageStatus.received)
                .build();
        when(messageDao.save(eq(CHAT_ID), any(MessageDto.class))).thenReturn(savedMessage);

        MessageDto result = messageService.sendMessage(CHAT_ID, messageDto, userDetails);

        assertNotNull(result);
        assertEquals(10L, result.getMessageId());
        assertEquals(MessageStatus.received, result.getMessageStatus());
        verify(messageDao).save(eq(CHAT_ID), any(MessageDto.class));
    }


    @Test
    public void sendMessageTestFailed_UnauthorizedUser() throws Exception {
        when(chatDao.getChat(CHAT_ID)).thenReturn(testChat);
        when(userDetails.getId()).thenReturn(99L); // Unauthorized user

        MessageDto messageDto = MessageDto.builder()
                .senderId(USER_ID)
                .receiverId(TASKER_ID)
                .IsUserSender(true)
                .content("Hello")
                .build();

        Exception exception = assertThrows(Exception.class,
                () -> messageService.sendMessage(CHAT_ID, messageDto, userDetails));
        assertTrue(exception.getMessage().contains("unauthorized"));
    }

    // ==================== MARK AS READ TESTS ====================

    @Test
    public void testMarkAsReadUser_Success() throws Exception {
        when(chatDao.getChat(CHAT_ID)).thenReturn(testChat);
        when(userDetails.getId()).thenReturn(USER_ID);

        messageService.markAllAsReadUser(CHAT_ID, userDetails);

        verify(chatDao).getChat(CHAT_ID);
        verify(messageDao).markasReadUser(CHAT_ID);
    }


    @Test
    public void testMarkAsReadTasker_Success() throws Exception {
        when(chatDao.getChat(CHAT_ID)).thenReturn(testChat);
        when(userDetails.getId()).thenReturn(TASKER_ID);

        messageService.markAllAsReadTasker(CHAT_ID, userDetails);

        verify(chatDao, times(2)).getChat(CHAT_ID); // Called twice in authorization check
        verify(messageDao).markasReadTasker(CHAT_ID);
    }

//    @Test
//    public void testMarkAsReadUser_Unauthorized() throws Exception {
//        when(chatDao.getChat(CHAT_ID)).thenReturn(testChat);
//        when(userDetails.getId()).thenReturn(99L);
//
//        Exception exception = assertThrows(Exception.class,
//                () -> messageService.markAllAsReadUser(CHAT_ID, userDetails));
//        assertEquals(true,exception.getMessage().contains("unauthorized"));
//    }

    // ==================== MARK AS RECEIVED TESTS ====================

    @Test
    public void testMarkAsReceivedForUser_Success() throws Exception {
        doNothing().when(messageDao).markasReceivedUser(USER_ID);

        messageService.markAllAsReceivedForUser(USER_ID);

        verify(messageDao).markasReceivedUser(USER_ID);
    }

    @Test
    public void testMarkAsReceivedForTasker_Success() throws Exception {
        doNothing().when(messageDao).markasReceivedTasker(TASKER_ID);

        messageService.markAllAsReceivedForTasker(TASKER_ID);

        verify(messageDao).markasReceivedTasker(TASKER_ID);
    }

//    // ==================== GET UNREAD MESSAGES TESTS ====================

//    @Test
//    public void testGetUnreadMessagesForUser_Success() throws Exception {
//        when(chatDao.getChat(CHAT_ID)).thenReturn(testChat);
//        when(userDetails.getId()).thenReturn(USER_ID);
//        when(messageDao.getUnreadOnesForUser(CHAT_ID)).thenReturn(5);
//
//        int unreadCount = messageService.getUnreadMessagesForUser(CHAT_ID, userDetails);
//
//        assertEquals(5, unreadCount);
//        verify(chatDao, times(2)).getChat(CHAT_ID);
//        verify(messageDao).getUnreadOnesForUser(CHAT_ID);
//    }
//
//    @Test
//    public void testGetUnreadMessagesForTasker_Success() throws Exception {
//        when(chatDao.getChat(CHAT_ID)).thenReturn(testChat);
//        when(userDetails.getId()).thenReturn(TASKER_ID);
//        when(messageDao.getUnreadOnesForTasker(CHAT_ID)).thenReturn(3);
//
//        int unreadCount = messageService.getUnreadMessagesForTasker(CHAT_ID, userDetails);
//
//        assertEquals(3, unreadCount);
//        verify(chatDao, times(2)).getChat(CHAT_ID);
//        verify(messageDao).getUnreadOnesForTasker(CHAT_ID);
//    }

    // ==================== CHAT HISTORY TESTS ====================

    @Test
    public void testGetChatHistory_Success() throws Exception {
        when(chatDao.getChat(CHAT_ID)).thenReturn(testChat);
        when(userDetails.getId()).thenReturn(USER_ID);

        MessageDto msg1 = MessageDto.builder()
                .messageId(1L)
                .content("Message 1")
                .senderId(USER_ID)
                .receiverId(TASKER_ID)
                .IsUserSender(true)
                .build();

        MessageDto msg2 = MessageDto.builder()
                .messageId(2L)
                .content("Message 2")
                .senderId(TASKER_ID)
                .receiverId(USER_ID)
                .IsUserSender(false)
                .build();

        List<MessageDto> messages = Arrays.asList(msg1, msg2);
        when(messageDao.getAllMessages(CHAT_ID, 0, 10)).thenReturn(messages);
        when(messageDao.getTotalCount(CHAT_ID)).thenReturn(2L);

        PaginatedResponse response = chatService.getChatHistory(CHAT_ID, 0, 10, userDetails);

        assertNotNull(response);
        assertEquals(2, response.getMessages().size());
        assertEquals(0, response.getPage());
        assertEquals(10, response.getPageSize());
        assertEquals(2L, response.getTotalCount());
        assertEquals(1L, response.getTotalPages());
        assertFalse(response.isHasNext());
        assertFalse(response.isHasPrevious());

        verify(messageDao).getAllMessages(CHAT_ID, 0, 10);
        verify(messageDao).getTotalCount(CHAT_ID);
    }

    @Test
    public void testGetChatHistory_Pagination() throws Exception {
        when(chatDao.getChat(CHAT_ID)).thenReturn(testChat);
        when(userDetails.getId()).thenReturn(USER_ID);

        List<MessageDto> messages = Arrays.asList(
                MessageDto.builder().messageId(1L).content("Msg 1").build(),
                MessageDto.builder().messageId(2L).content("Msg 2").build()
        );

        when(messageDao.getAllMessages(CHAT_ID, 1, 5)).thenReturn(messages);
        when(messageDao.getTotalCount(CHAT_ID)).thenReturn(12L);

        PaginatedResponse response = chatService.getChatHistory(CHAT_ID, 1, 5, userDetails);

        assertEquals(1, response.getPage());
        assertEquals(5, response.getPageSize());
        assertEquals(12L, response.getTotalCount());
        assertEquals(3L, response.getTotalPages());
        assertTrue(response.isHasNext());
        assertTrue(response.isHasPrevious());
    }

    @Test
    public void testGetChatHistory_Unauthorized() throws Exception {
        when(chatDao.getChat(CHAT_ID)).thenReturn(testChat);
        when(userDetails.getId()).thenReturn(99L);

        Exception exception = assertThrows(Exception.class,
                () -> chatService.getChatHistory(CHAT_ID, 0, 10, userDetails));
        assertTrue(exception.getMessage().contains("unauthorized"));
    }

    // ==================== PHONE NUMBER TESTS ====================

    @Test
    public void testGetUserPhoneNumber_Success() throws Exception {
        when(chatDao.getChat(CHAT_ID)).thenReturn(testChat);
        when(userDetails.getId()).thenReturn(TASKER_ID);
        when(chatDao.getUserPhone(CHAT_ID)).thenReturn("1234567890");

        String phone = chatService.getUserPhoneNumber(CHAT_ID, userDetails);

        assertEquals("1234567890", phone);
        verify(chatDao).getUserPhone(CHAT_ID);
    }

    @Test
    public void testGetTaskerPhoneNumber_Success() throws Exception {
        when(chatDao.getChat(CHAT_ID)).thenReturn(testChat);
        when(userDetails.getId()).thenReturn(USER_ID);
        when(chatDao.getTaskerPhone(CHAT_ID)).thenReturn("0987654321");

        String phone = chatService.getTaskerPhoneNumber(CHAT_ID, userDetails);

        assertEquals("0987654321", phone);
        verify(chatDao).getTaskerPhone(CHAT_ID);
    }    @Test
    public void testGetChat_Success() throws Exception {
        when(chatDao.getChat(CHAT_ID)).thenReturn(testChat);

        ChatDto result = chatService.getChat(CHAT_ID);

        assertNotNull(result);
        assertEquals(CHAT_ID, result.getChatId());
        assertEquals(USER_ID, result.getUserId());
        assertEquals(TASKER_ID, result.getTaskerId());
        verify(chatDao).getChat(CHAT_ID);
    }

    @Test
    public void testGetChat_ThrowsException() throws Exception {
        when(chatDao.getChat(CHAT_ID)).thenThrow(new SQLException("Database error"));

        assertThrows(SQLException.class, () -> chatService.getChat(CHAT_ID));
        verify(chatDao).getChat(CHAT_ID);
    }

    @Test
    public void testGetChat_ReturnsNull() throws Exception {
        when(chatDao.getChat(CHAT_ID)).thenReturn(null);

        ChatDto result = chatService.getChat(CHAT_ID);

        assertNull(result);
        verify(chatDao).getChat(CHAT_ID);
    }

    // ==================== GET USER PHONE NUMBER TESTS ====================

    @Test
    public void testGetUserPhoneNumber_NullPhone() throws Exception {
        when(chatDao.getChat(CHAT_ID)).thenReturn(testChat);
        when(userDetails.getId()).thenReturn(TASKER_ID);
        when(chatDao.getUserPhone(CHAT_ID)).thenReturn(null);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> chatService.getUserPhoneNumber(CHAT_ID, userDetails));

        assertTrue(exception.getMessage().contains("No Phone Number Available"));
        verify(chatDao).getUserPhone(CHAT_ID);
    }

    @Test
    public void testGetUserPhoneNumber_AuthorizedUser() throws Exception {
        when(chatDao.getChat(CHAT_ID)).thenReturn(testChat);
        when(userDetails.getId()).thenReturn(USER_ID);
        when(chatDao.getUserPhone(CHAT_ID)).thenReturn("1234567890");

        String result = chatService.getUserPhoneNumber(CHAT_ID, userDetails);

        assertEquals("1234567890", result);
        verify(chatDao, times(1)).getChat(CHAT_ID);
    }

    @Test
    public void testGetUserPhoneNumber_UnauthorizedUser() throws Exception {
        when(chatDao.getChat(CHAT_ID)).thenReturn(testChat);
        when(userDetails.getId()).thenReturn(UNAUTHORIZED_ID);

        Exception exception = assertThrows(Exception.class,
                () -> chatService.getUserPhoneNumber(CHAT_ID, userDetails));

        assertTrue(exception.getMessage().contains("unauthorized"));
        verify(chatDao, never()).getUserPhone(CHAT_ID);
    }

    // ==================== GET TASKER PHONE NUMBER TESTS ====================

    @Test
    public void testGetTaskerPhoneNumber_NullPhone() throws Exception {
        when(chatDao.getChat(CHAT_ID)).thenReturn(testChat);
        when(userDetails.getId()).thenReturn(USER_ID);
        when(chatDao.getTaskerPhone(CHAT_ID)).thenReturn(null);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> chatService.getTaskerPhoneNumber(CHAT_ID, userDetails));

        assertTrue(exception.getMessage().contains("No Phone Number Available"));
        verify(chatDao).getTaskerPhone(CHAT_ID);
    }

    @Test
    public void testGetTaskerPhoneNumber_AuthorizedTasker() throws Exception {
        when(chatDao.getChat(CHAT_ID)).thenReturn(testChat);
        when(userDetails.getId()).thenReturn(TASKER_ID);
        when(chatDao.getTaskerPhone(CHAT_ID)).thenReturn("0987654321");

        String result = chatService.getTaskerPhoneNumber(CHAT_ID, userDetails);

        assertEquals("0987654321", result);
        verify(chatDao, times(2)).getChat(CHAT_ID);
    }

    @Test
    public void testGetTaskerPhoneNumber_UnauthorizedUser() throws Exception {
        when(chatDao.getChat(CHAT_ID)).thenReturn(testChat);
        when(userDetails.getId()).thenReturn(UNAUTHORIZED_ID);

        Exception exception = assertThrows(Exception.class,
                () -> chatService.getTaskerPhoneNumber(CHAT_ID, userDetails));

        assertTrue(exception.getMessage().contains("unauthorized"));
        verify(chatDao, never()).getTaskerPhone(CHAT_ID);
    }

    // ==================== GET RECIPIENT NAME TESTS ====================

    @Test
    public void testGetRecipientNameUser_Success() throws Exception {
        when(chatDao.getChat(CHAT_ID)).thenReturn(testChat);
        when(userDetails.getId()).thenReturn(TASKER_ID);
        when(chatDao.getNameUser(USER_ID)).thenReturn("john_user");

        String result = chatService.getRecipentNameUser(CHAT_ID, userDetails);

        assertEquals("john_user", result);
        verify(chatDao).getChat(CHAT_ID);
        verify(chatDao).getNameUser(USER_ID);
    }

    @Test
    public void testGetRecipientNameUser_NullChat() throws Exception {
        when(chatDao.getChat(CHAT_ID)).thenReturn(null);

        Exception exception = assertThrows(Exception.class,
                () -> chatService.getRecipentNameUser(CHAT_ID, userDetails));

        assertTrue(exception.getMessage().contains("Unknown chat"));
        verify(chatDao, never()).getNameUser(anyLong());
    }

    @Test
    public void testGetRecipientNameUser_UnauthorizedUser() throws Exception {
        when(chatDao.getChat(CHAT_ID)).thenReturn(testChat);
        when(userDetails.getId()).thenReturn(USER_ID); // User trying to get user name

        Exception exception = assertThrows(Exception.class,
                () -> chatService.getRecipentNameUser(CHAT_ID, userDetails));

        assertTrue(exception.getMessage().contains("Unauthorized user"));
        verify(chatDao, never()).getNameUser(anyLong());
    }

    @Test
    public void testGetRecipientNameTasker_Success() throws Exception {
        when(chatDao.getChat(CHAT_ID)).thenReturn(testChat);
        when(userDetails.getId()).thenReturn(USER_ID);
        when(chatDao.getNameTasker(TASKER_ID)).thenReturn("jane_tasker");

        String result = chatService.getRecipentNameTasker(CHAT_ID, userDetails);

        assertEquals("jane_tasker", result);
        verify(chatDao).getChat(CHAT_ID);
        verify(chatDao).getNameTasker(TASKER_ID);
    }

    @Test
    public void testGetRecipientNameTasker_NullChat() throws Exception {
        when(chatDao.getChat(CHAT_ID)).thenReturn(null);

        Exception exception = assertThrows(Exception.class,
                () -> chatService.getRecipentNameTasker(CHAT_ID, userDetails));

        assertTrue(exception.getMessage().contains("Unknown chat"));
        verify(chatDao, never()).getNameTasker(anyLong());
    }

    @Test
    public void testGetRecipientNameTasker_UnauthorizedUser() throws Exception {
        when(chatDao.getChat(CHAT_ID)).thenReturn(testChat);
        when(userDetails.getId()).thenReturn(TASKER_ID); // Tasker trying to get tasker name

        Exception exception = assertThrows(Exception.class,
                () -> chatService.getRecipentNameTasker(CHAT_ID, userDetails));

        assertTrue(exception.getMessage().contains("Unauthorized user"));
        verify(chatDao, never()).getNameTasker(anyLong());
    }

    // ==================== CHANGE STATUS TESTS ====================

    @Test
    public void testChangeStatusTasker_Success() throws Exception {
        doNothing().when(chatDao).changeTaskerStatus(TASKER_ID);

        chatService.changeStatusTasker(TASKER_ID);

        verify(chatDao).changeTaskerStatus(TASKER_ID);
    }

    @Test
    public void testChangeStatusTasker_ThrowsException() throws Exception {
        doThrow(new RuntimeException("Database error"))
                .when(chatDao).changeTaskerStatus(TASKER_ID);

        assertThrows(RuntimeException.class,
                () -> chatService.changeStatusTasker(TASKER_ID));
    }

    @Test
    public void testChangeStatusUser_Success() throws Exception {
        doNothing().when(chatDao).changeUserStatus(USER_ID);

        chatService.changeStatusUser(USER_ID);

        verify(chatDao).changeUserStatus(USER_ID);
    }

    @Test
    public void testChangeStatusUser_ThrowsException() throws Exception {
        doThrow(new RuntimeException("Database error"))
                .when(chatDao).changeUserStatus(USER_ID);

        assertThrows(RuntimeException.class,
                () -> chatService.changeStatusUser(USER_ID));
    }

    // ==================== SET ONLINE TESTS ====================

    @Test
    public void testSetTaskerOnline_Success() throws Exception {
        doNothing().when(messageDao).markasReceivedTasker(TASKER_ID);
        doNothing().when(chatDao).setTaskerOnline(TASKER_ID);

        chatService.setTaskerOnline(TASKER_ID);

        verify(messageDao).markasReceivedTasker(TASKER_ID);
        verify(chatDao).setTaskerOnline(TASKER_ID);
    }

    @Test
    public void testSetTaskerOnline_MarkAsReceivedFails() throws Exception {
        doThrow(new RuntimeException("Database error"))
                .when(messageDao).markasReceivedTasker(TASKER_ID);

        Exception exception = assertThrows(Exception.class,
                () -> chatService.setTaskerOnline(TASKER_ID));

        assertTrue(exception.getMessage().contains("Failed to set tasker online"));
        verify(messageDao).markasReceivedTasker(TASKER_ID);
        verify(chatDao, never()).setTaskerOnline(TASKER_ID);
    }

    @Test
    public void testSetTaskerOnline_SetOnlineFails() throws Exception {
        doNothing().when(messageDao).markasReceivedTasker(TASKER_ID);
        doThrow(new RuntimeException("Database error"))
                .when(chatDao).setTaskerOnline(TASKER_ID);

        Exception exception = assertThrows(Exception.class,
                () -> chatService.setTaskerOnline(TASKER_ID));

        assertTrue(exception.getMessage().contains("Failed to set tasker online"));
        verify(messageDao).markasReceivedTasker(TASKER_ID);
        verify(chatDao).setTaskerOnline(TASKER_ID);
    }

    @Test
    public void testSetUserOnline_Success() throws Exception {
        doNothing().when(messageDao).markasReceivedUser(USER_ID);
        doNothing().when(chatDao).setUserOnline(USER_ID);

        chatService.setUserOnline(USER_ID);

        verify(messageDao).markasReceivedUser(USER_ID);
        verify(chatDao).setUserOnline(USER_ID);
    }

    @Test
    public void testSetUserOnline_MarkAsReceivedFails() throws Exception {
        doThrow(new RuntimeException("Database error"))
                .when(messageDao).markasReceivedUser(USER_ID);

        Exception exception = assertThrows(Exception.class,
                () -> chatService.setUserOnline(USER_ID));

        assertTrue(exception.getMessage().contains("Failed to set user online"));
        verify(messageDao).markasReceivedUser(USER_ID);
        verify(chatDao, never()).setUserOnline(USER_ID);
    }

    @Test
    public void testSetUserOnline_SetOnlineFails() throws Exception {
        doNothing().when(messageDao).markasReceivedUser(USER_ID);
        doThrow(new RuntimeException("Database error"))
                .when(chatDao).setUserOnline(USER_ID);

        Exception exception = assertThrows(Exception.class,
                () -> chatService.setUserOnline(USER_ID));

        assertTrue(exception.getMessage().contains("Failed to set user online"));
        verify(messageDao).markasReceivedUser(USER_ID);
        verify(chatDao).setUserOnline(USER_ID);
    }

    // ==================== EDGE CASES ====================

    @Test
    public void testSetTaskerOnline_WithNullId() throws Exception {
        doThrow(new NullPointerException("ID cannot be null"))
                .when(messageDao).markasReceivedTasker(null);

        Exception exception = assertThrows(Exception.class,
                () -> chatService.setTaskerOnline(null));

        assertTrue(exception.getMessage().contains("Failed to set tasker online"));
    }

    @Test
    public void testSetUserOnline_WithNullId() throws Exception {
        doThrow(new NullPointerException("ID cannot be null"))
                .when(messageDao).markasReceivedUser(null);

        Exception exception = assertThrows(Exception.class,
                () -> chatService.setUserOnline(null));

        assertTrue(exception.getMessage().contains("Failed to set user online"));
    }

    @Test
    public void testGetUserPhoneNumber_WithEmptyPhoneString() throws Exception {
        when(chatDao.getChat(CHAT_ID)).thenReturn(testChat);
        when(userDetails.getId()).thenReturn(TASKER_ID);
        when(chatDao.getUserPhone(CHAT_ID)).thenReturn("");

        String result = chatService.getUserPhoneNumber(CHAT_ID, userDetails);

        assertEquals("", result);
    }

    @Test
    public void testGetTaskerPhoneNumber_WithEmptyPhoneString() throws Exception {
        when(chatDao.getChat(CHAT_ID)).thenReturn(testChat);
        when(userDetails.getId()).thenReturn(USER_ID);
        when(chatDao.getTaskerPhone(CHAT_ID)).thenReturn("");

        String result = chatService.getTaskerPhoneNumber(CHAT_ID, userDetails);

        assertEquals("", result);
    }

    @Test
    public void testGetRecipientNameUser_WithEmptyName() throws Exception {
        when(chatDao.getChat(CHAT_ID)).thenReturn(testChat);
        when(userDetails.getId()).thenReturn(TASKER_ID);
        when(chatDao.getNameUser(USER_ID)).thenReturn("");

        String result = chatService.getRecipentNameUser(CHAT_ID, userDetails);

        assertEquals("", result);
    }

    @Test
    public void testGetRecipientNameTasker_WithEmptyName() throws Exception {
        when(chatDao.getChat(CHAT_ID)).thenReturn(testChat);
        when(userDetails.getId()).thenReturn(USER_ID);
        when(chatDao.getNameTasker(TASKER_ID)).thenReturn("");

        String result = chatService.getRecipentNameTasker(CHAT_ID, userDetails);

        assertEquals("", result);
    }
    @Test
    public void testSendMessage_GetChatThrowsException() throws Exception {
        when(chatDao.getChat(CHAT_ID)).thenThrow(new RuntimeException("Database error"));
        when(userDetails.getId()).thenReturn(USER_ID);

        Exception exception = assertThrows(Exception.class,
                () -> messageService.sendMessage(CHAT_ID, testMessage, userDetails));

        assertNotNull(exception.getCause());
        verify(chatDao).getChat(CHAT_ID);
        verify(messageDao, never()).save(any(), any());
    }

    @Test
    public void testSendMessage_GetTaskerStatusThrowsException() throws Exception {
        when(chatDao.getChat(CHAT_ID)).thenReturn(testChat);
        when(userDetails.getId()).thenReturn(USER_ID);
        when(messageDao.getTaskerStatus(CHAT_ID)).thenThrow(new RuntimeException("Database error"));

        testMessage.setIsUserSender(true);

        Exception exception = assertThrows(Exception.class,
                () -> messageService.sendMessage(CHAT_ID, testMessage, userDetails));

        assertNotNull(exception.getCause());
        verify(messageDao).getTaskerStatus(CHAT_ID);
        verify(messageDao, never()).save(any(), any());
    }

    @Test
    public void testSendMessage_GetUserStatusThrowsException() throws Exception {
        when(chatDao.getChat(CHAT_ID)).thenReturn(testChat);
        when(userDetails.getId()).thenReturn(TASKER_ID);
        when(messageDao.getUserStatus(CHAT_ID)).thenThrow(new RuntimeException("Database error"));

        testMessage.setIsUserSender(false);
        testMessage.setSenderId(TASKER_ID);
        testMessage.setReceiverId(USER_ID);

        Exception exception = assertThrows(Exception.class,
                () -> messageService.sendMessage(CHAT_ID, testMessage, userDetails));

        assertNotNull(exception.getCause());
        verify(messageDao).getUserStatus(CHAT_ID);
        verify(messageDao, never()).save(any(), any());
    }

    @Test
    public void testSendMessage_SaveThrowsException() throws Exception {
        when(chatDao.getChat(CHAT_ID)).thenReturn(testChat);
        when(userDetails.getId()).thenReturn(USER_ID);
        when(messageDao.getTaskerStatus(CHAT_ID)).thenReturn(true);
        when(messageDao.save(eq(CHAT_ID), any(MessageDto.class)))
                .thenThrow(new RuntimeException("Database error"));

        Exception exception = assertThrows(Exception.class,
                () -> messageService.sendMessage(CHAT_ID, testMessage, userDetails));

        assertNotNull(exception.getCause());
        verify(messageDao).save(eq(CHAT_ID), any(MessageDto.class));
    }

    @Test
    public void testSendMessage_UserSenderTaskerOffline() throws Exception {
        when(chatDao.getChat(CHAT_ID)).thenReturn(testChat);
        when(userDetails.getId()).thenReturn(USER_ID);
        when(messageDao.getTaskerStatus(CHAT_ID)).thenReturn(false);

        MessageDto savedMessage = MessageDto.builder()
                .messageId(MESSAGE_ID)
                .senderId(USER_ID)
                .receiverId(TASKER_ID)
                .content("Test message")
                .IsUserSender(true)
                .messageStatus(MessageStatus.sent)
                .build();

        when(messageDao.save(eq(CHAT_ID), any(MessageDto.class))).thenReturn(savedMessage);

        testMessage.setIsUserSender(true);
        MessageDto result = messageService.sendMessage(CHAT_ID, testMessage, userDetails);

        assertNotNull(result);
        assertEquals(MessageStatus.sent, result.getMessageStatus());
        verify(messageDao).getTaskerStatus(CHAT_ID);
        verify(messageDao).save(eq(CHAT_ID), any(MessageDto.class));
    }

    @Test
    public void testSendMessage_TaskerSenderUserOffline() throws Exception {
        when(chatDao.getChat(CHAT_ID)).thenReturn(testChat);
        when(userDetails.getId()).thenReturn(TASKER_ID);
        when(messageDao.getUserStatus(CHAT_ID)).thenReturn(false);

        MessageDto savedMessage = MessageDto.builder()
                .messageId(MESSAGE_ID)
                .senderId(TASKER_ID)
                .receiverId(USER_ID)
                .content("Test message")
                .IsUserSender(false)
                .messageStatus(MessageStatus.sent)
                .build();

        when(messageDao.save(eq(CHAT_ID), any(MessageDto.class))).thenReturn(savedMessage);

        testMessage.setIsUserSender(false);
        testMessage.setSenderId(TASKER_ID);
        testMessage.setReceiverId(USER_ID);

        MessageDto result = messageService.sendMessage(CHAT_ID, testMessage, userDetails);

        assertNotNull(result);
        assertEquals(MessageStatus.sent, result.getMessageStatus());
        verify(messageDao).getUserStatus(CHAT_ID);
        verify(messageDao).save(eq(CHAT_ID), any(MessageDto.class));
    }

    // ==================== MARK AS READ EXCEPTION TESTS ====================

    @Test
    public void testMarkAsReadUser_GetChatThrowsException() throws Exception {
        when(chatDao.getChat(CHAT_ID)).thenThrow(new RuntimeException("Database error"));
        when(userDetails.getId()).thenReturn(USER_ID);

        Exception exception = assertThrows(Exception.class,
                () -> messageService.markAllAsReadUser(CHAT_ID, userDetails));

        assertTrue(exception.getMessage().contains("Couldn't change Message Status to read"));
        verify(chatDao).getChat(CHAT_ID);
        verify(messageDao, never()).markasReadUser(CHAT_ID);
    }

    @Test
    public void testMarkAsReadUser_MarkAsReadThrowsException() throws Exception {
        when(chatDao.getChat(CHAT_ID)).thenReturn(testChat);
        when(userDetails.getId()).thenReturn(USER_ID);
        doThrow(new RuntimeException("Database error")).when(messageDao).markasReadUser(CHAT_ID);

        Exception exception = assertThrows(Exception.class,
                () -> messageService.markAllAsReadUser(CHAT_ID, userDetails));

        assertTrue(exception.getMessage().contains("Couldn't change Message Status to read"));
        verify(messageDao).markasReadUser(CHAT_ID);
    }

    @Test
    public void testMarkAsReadTasker_GetChatThrowsException() throws Exception {
        when(chatDao.getChat(CHAT_ID)).thenThrow(new RuntimeException("Database error"));
        when(userDetails.getId()).thenReturn(TASKER_ID);

        Exception exception = assertThrows(Exception.class,
                () -> messageService.markAllAsReadTasker(CHAT_ID, userDetails));

        assertTrue(exception.getMessage().contains("Couldn't change Message Status to read"));
        verify(chatDao).getChat(CHAT_ID);
        verify(messageDao, never()).markasReadTasker(CHAT_ID);
    }

    @Test
    public void testMarkAsReadTasker_MarkAsReadThrowsException() throws Exception {
        when(chatDao.getChat(CHAT_ID)).thenReturn(testChat);
        when(userDetails.getId()).thenReturn(TASKER_ID);
        doThrow(new RuntimeException("Database error")).when(messageDao).markasReadTasker(CHAT_ID);

        Exception exception = assertThrows(Exception.class,
                () -> messageService.markAllAsReadTasker(CHAT_ID, userDetails));

        assertTrue(exception.getMessage().contains("Couldn't change Message Status to read"));
        verify(messageDao).markasReadTasker(CHAT_ID);
    }

    // ==================== MARK AS RECEIVED EXCEPTION TESTS ====================

    @Test
    public void testMarkAsReceivedForUser_ThrowsException() {
        doThrow(new RuntimeException("Database error")).when(messageDao).markasReceivedUser(USER_ID);

        Exception exception = assertThrows(Exception.class,
                () -> messageService.markAllAsReceivedForUser(USER_ID));

        assertTrue(exception.getMessage().contains("Couldn't change Message Status to received"));
        assertTrue(exception.getMessage().contains("Database error"));
        verify(messageDao).markasReceivedUser(USER_ID);
    }

    @Test
    public void testMarkAsReceivedForTasker_ThrowsException() {
        doThrow(new RuntimeException("Database error")).when(messageDao).markasReceivedTasker(TASKER_ID);

        Exception exception = assertThrows(Exception.class,
                () -> messageService.markAllAsReceivedForTasker(TASKER_ID));

        assertTrue(exception.getMessage().contains("Couldn't change Message Status to received"));
        assertTrue(exception.getMessage().contains("Database error"));
        verify(messageDao).markasReceivedTasker(TASKER_ID);
    }

    // ==================== AUTHORIZATION EXCEPTION TESTS ====================

    @Test
    public void testMarkAsReadUser_UnauthorizedUser() throws Exception {
        when(chatDao.getChat(CHAT_ID)).thenReturn(testChat);
        when(userDetails.getId()).thenReturn(999L); // Unauthorized

        Exception exception = assertThrows(Exception.class,
                () -> messageService.markAllAsReadUser(CHAT_ID, userDetails));

        assertTrue(exception.getMessage().contains("Couldn't change Message Status to read"));
        verify(messageDao, never()).markasReadUser(CHAT_ID);
    }

    @Test
    public void testMarkAsReadTasker_UnauthorizedUser() throws Exception {
        when(chatDao.getChat(CHAT_ID)).thenReturn(testChat);
        when(userDetails.getId()).thenReturn(999L); // Unauthorized

        Exception exception = assertThrows(Exception.class,
                () -> messageService.markAllAsReadTasker(CHAT_ID, userDetails));

        assertTrue(exception.getMessage().contains("Couldn't change Message Status to read"));
        verify(messageDao, never()).markasReadTasker(CHAT_ID);
    }

    // ==================== EDGE CASES ====================

    @Test
    public void testSendMessage_NullMessageStatus() throws Exception {
        when(chatDao.getChat(CHAT_ID)).thenReturn(testChat);
        when(userDetails.getId()).thenReturn(USER_ID);
        when(messageDao.getTaskerStatus(CHAT_ID)).thenReturn(false);

        testMessage.setMessageStatus(null);
        testMessage.setIsUserSender(true);

        MessageDto savedMessage = MessageDto.builder()
                .messageId(MESSAGE_ID)
                .messageStatus(MessageStatus.sent)
                .build();

        when(messageDao.save(eq(CHAT_ID), any(MessageDto.class))).thenReturn(savedMessage);

        MessageDto result = messageService.sendMessage(CHAT_ID, testMessage, userDetails);

        assertNotNull(result);
        verify(messageDao).save(eq(CHAT_ID), any(MessageDto.class));
    }

    @Test
    public void testMarkAsReceivedForUser_NullId() {
        doThrow(new NullPointerException("ID cannot be null"))
                .when(messageDao).markasReceivedUser(null);

        Exception exception = assertThrows(Exception.class,
                () -> messageService.markAllAsReceivedForUser(null));

        assertTrue(exception.getMessage().contains("Couldn't change Message Status to received"));
    }

    @Test
    public void testMarkAsReceivedForTasker_NullId() {
        doThrow(new NullPointerException("ID cannot be null"))
                .when(messageDao).markasReceivedTasker(null);

        Exception exception = assertThrows(Exception.class,
                () -> messageService.markAllAsReceivedForTasker(null));

        assertTrue(exception.getMessage().contains("Couldn't change Message Status to received"));
    }

    @Test
    public void testSendMessage_BothAuthorizationChecksSuccess() throws Exception {
        // Test that authorization passes when user is the actual user
        when(chatDao.getChat(CHAT_ID)).thenReturn(testChat);
        when(userDetails.getId()).thenReturn(USER_ID);
        when(messageDao.getTaskerStatus(CHAT_ID)).thenReturn(true);

        MessageDto savedMessage = MessageDto.builder()
                .messageId(MESSAGE_ID)
                .messageStatus(MessageStatus.received)
                .build();

        when(messageDao.save(eq(CHAT_ID), any(MessageDto.class))).thenReturn(savedMessage);

        MessageDto result = messageService.sendMessage(CHAT_ID, testMessage, userDetails);

        assertNotNull(result);
        verify(chatDao, times(1)).getChat(CHAT_ID); // Called twice for authorization
    }

    @Test
    public void testSendMessage_TaskerAuthorized() throws Exception {
        // Test that authorization passes when user is the tasker
        when(chatDao.getChat(CHAT_ID)).thenReturn(testChat);
        when(userDetails.getId()).thenReturn(TASKER_ID);
        when(messageDao.getUserStatus(CHAT_ID)).thenReturn(true);

        testMessage.setIsUserSender(false);
        testMessage.setSenderId(TASKER_ID);
        testMessage.setReceiverId(USER_ID);

        MessageDto savedMessage = MessageDto.builder()
                .messageId(MESSAGE_ID)
                .messageStatus(MessageStatus.received)
                .build();

        when(messageDao.save(eq(CHAT_ID), any(MessageDto.class))).thenReturn(savedMessage);

        MessageDto result = messageService.sendMessage(CHAT_ID, testMessage, userDetails);

        assertNotNull(result);
        verify(chatDao, times(2)).getChat(CHAT_ID); // Called twice for authorization
    }
}
