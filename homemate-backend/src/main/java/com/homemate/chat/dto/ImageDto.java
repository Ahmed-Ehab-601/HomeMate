package com.homemate.chat.dto;

import lombok.*;

import java.security.Timestamp;


@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder

public class ImageDto {

    private Long Id;
    private String fileData;
    private String FileName;
    private String FileFormat;
    private Long messageId;
}
