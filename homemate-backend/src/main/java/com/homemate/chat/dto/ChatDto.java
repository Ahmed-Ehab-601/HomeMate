package com.homemate.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
public class ChatDto {
    private Long chatId;
    private Long userId;
    private Long taskerId;
    private boolean userIsActive;
    private boolean taskerIsActive;
}
