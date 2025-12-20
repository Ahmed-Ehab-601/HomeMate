package com.homemate.chat.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.homemate.chat.Enum.OnlineStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString // Add toString for better logging
public class PresenceUpdate {
    @JsonProperty("Id")
    private Long Id;

    @JsonProperty("UserType")
    private String UserType;

    @JsonProperty("onlineStatus")
    private OnlineStatus onlineStatus;

    @JsonProperty("time")
    private LocalDateTime time;
}