package com.homemate.chat;

import com.homemate.chat.Enum.MessageStatus;
import com.homemate.chat.Service.MessageService;
import com.homemate.chat.dao.ChatDao;
import com.homemate.chat.dao.MessageDao;
import com.homemate.chat.dto.ChatDto;
import com.homemate.chat.dto.MessageDto;
import com.homemate.security.model.AppUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MessageTest {

    @Mock
    private MessageDao messageDao;

    @Mock
    private ChatDao chatDao;

    @Mock
    private AppUserDetails userDetails;

    @InjectMocks
    private MessageService messageService;

    private ChatDto testChat;
    private MessageDto testMessage;
    private static final Long CHAT_ID = 1L;
    private static final Long USER_ID = 1L;
    private static final Long TASKER_ID = 2L;
    private static final Long MESSAGE_ID = 10L;
    private static final Long UNAUTHORIZED_ID = 999L;

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
        when(userDetails.getId()).thenReturn(99L);

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
    public void testSendMessage_BothAuthorizationChecksSuccess() throws Exception {
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
        verify(chatDao, times(1)).getChat(CHAT_ID);
    }

    @Test
    public void testSendMessage_TaskerAuthorized() throws Exception {
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
        verify(chatDao, times(2)).getChat(CHAT_ID);
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

        verify(chatDao, times(2)).getChat(CHAT_ID);
        verify(messageDao).markasReadTasker(CHAT_ID);
    }

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

    @Test
    public void testMarkAsReadUser_UnauthorizedUser() throws Exception {
        when(chatDao.getChat(CHAT_ID)).thenReturn(testChat);
        when(userDetails.getId()).thenReturn(999L);

        Exception exception = assertThrows(Exception.class,
                () -> messageService.markAllAsReadUser(CHAT_ID, userDetails));

        assertTrue(exception.getMessage().contains("Couldn't change Message Status to read"));
        verify(messageDao, never()).markasReadUser(CHAT_ID);
    }

    @Test
    public void testMarkAsReadTasker_UnauthorizedUser() throws Exception {
        when(chatDao.getChat(CHAT_ID)).thenReturn(testChat);
        when(userDetails.getId()).thenReturn(999L);

        Exception exception = assertThrows(Exception.class,
                () -> messageService.markAllAsReadTasker(CHAT_ID, userDetails));

        assertTrue(exception.getMessage().contains("Couldn't change Message Status to read"));
        verify(messageDao, never()).markasReadTasker(CHAT_ID);
    }

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
}