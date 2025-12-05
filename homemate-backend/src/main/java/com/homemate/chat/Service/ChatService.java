package com.homemate.chat.Service;

import com.homemate.chat.dao.ChatDao;
import com.homemate.chat.dao.MessageDao;
import com.homemate.chat.dto.MessageDto;
import com.homemate.chat.dto.PaginatedResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatService {
    @Autowired
    private MessageDao messageDao;
    private ChatDao chatDao;
    public ChatService(MessageDao messageDao, ChatDao chatDao) {
        this.messageDao = messageDao;
        this.chatDao = chatDao;
    }

    public PaginatedResponse getChatHistory(Long chatId, int page, int size) throws Exception{
        List<MessageDto> messages = messageDao.getAllMessages(chatId, page, size);

        Long totalCount = messageDao.getTotalCount(chatId);

        Long totalPages = (long) Math.ceil((double) totalCount / size);

        return PaginatedResponse.builder()
                .messages(messages)
                .page(page)
                .pageSize(size)
                .totalCount(totalCount)
                .totalPages(totalPages)
                .hasNext(page < totalPages - 1)
                .hasPrevious(page > 0)
                .build();
    }

    public String getUserPhoneNumber(Long Id) throws Exception{
        String str=chatDao.getUserPhone(Id);
        if(str==null)
            throw new RuntimeException("No Phone Number Available");

        return str;
    }

    public String getTaskerPhoneNumber(Long Id)throws Exception {
        String str=chatDao.getTaskerPhone(Id);
        if(str==null)
            throw new RuntimeException("No Phone Number Available");

        return str;
    }
}

