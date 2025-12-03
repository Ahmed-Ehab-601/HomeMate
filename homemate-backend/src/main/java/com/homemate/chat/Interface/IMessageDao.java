package com.homemate.chat.Interface;

import com.homemate.chat.dto.MessageDto;

import java.util.List;

public interface IMessageDao <M>{


    List<MessageDto> getAllMessages(Long chatId, int page, int size);
}
