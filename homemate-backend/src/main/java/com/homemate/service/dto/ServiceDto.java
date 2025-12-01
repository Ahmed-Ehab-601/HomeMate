package com.homemate.service.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ServiceDto {

    private Long id;

    @NotBlank(message = "Service name is required")
    @Size(min = 1, max = 100, message = "Name must be between 1 and 100 characters")
    private String name;

    @NotBlank(message = "Description is required")
    @Size(min = 1, max = 100, message = "Description must be between 1 and 100 characters")
    private String description;

    @Size(max = 22369621, message = "Image data must not exceed 16MB")
    private String imageData;

    @Size(max = 200, message = "Image name must not exceed 200 characters")
    private String imageName;

    @Size(max = 200, message = "Image type must not exceed 200 characters")
    private String imageType;
}