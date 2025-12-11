package com.homemate.chat.dto;

import jakarta.validation.constraints.Size;
import lombok.*;

import java.security.Timestamp;

import lombok.*;

import java.security.Timestamp;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder

public class ImageDto {

    private Long Id;
    @Size(max = 22369621, message = "Image data must not exceed 16MB")
    private String fileData;
    @Size(min = 1, max = 100, message = "content must be between 1 and 100 characters")
    private String FileName;
    @Size(min = 1, max = 50, message = "content must be between 1 and 50 characters")
    private String FileFormat;
    private Long messageId;
}
