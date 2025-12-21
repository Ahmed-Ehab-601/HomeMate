package com.homemate.reviews.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReviewImagesDTO {
    private int imgId;
    
    @NotBlank(message = "Image format is required")
    @Size(max = 50, message = "Image format cannot exceed 50 characters")
    private String format;
    
    @NotNull(message = "Image file is required")
    @Size(max = 500, message = "Image file cannot exceed 500 characters")
    private String imgFile;

    @NotBlank(message = "Image name is required")
    @Size(max = 100, message = "Image name cannot exceed 100 characters")
    private String imgName;


}
