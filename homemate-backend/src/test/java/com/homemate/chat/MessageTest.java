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

    @BeforeEach
    public void setUp() {
        testChat = new ChatDto();
        testChat.setChatId(CHAT_ID);
        testChat.setUserId(USER_ID);
        testChat.setTaskerId(TASKER_ID);
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
    }
}