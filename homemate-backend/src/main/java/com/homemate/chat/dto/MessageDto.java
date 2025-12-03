package com.homemate.chat.dto;

import com.homemate.chat.Enum.MessageStatus;
import lombok.*;

import java.time.LocalDateTime;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageDto {
    private Long messageId;
    private Long chatId;
    private boolean IsUserSender;
    private MessageStatus messageStatus;
    private String content;
    private LocalDateTime timestamp;
    private Long senderId;
    private Long receiverId;
    private ImageDto imageDto;
}
