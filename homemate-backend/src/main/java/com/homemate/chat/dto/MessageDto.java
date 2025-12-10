package com.homemate.chat.dto;

import com.homemate.chat.Enum.MessageStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageDto {
    private Long messageId;
    @NotNull(message = "chatId must not be null")
    private Long chatId;
    private boolean IsUserSender;
    private MessageStatus messageStatus;
    @Size(max = 1000, message = "content must be between 0 and 1000 characters")
    private String content;
    private LocalDateTime timestamp;
    @NotNull(message = "senderId must not be null")
    private Long senderId;
    @NotNull(message = "receiverId must not be null")
    private Long receiverId;
    private ImageDto imageDto;
}
