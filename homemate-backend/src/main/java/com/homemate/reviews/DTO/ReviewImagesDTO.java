package com.homemate.reviews.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ReviewImagesDTO {
    private int imgId;
    
    @NotBlank(message = "Image format is required")
    @Size(max = 50, message = "Image format cannot exceed 50 characters")
    private String format;
    
    @NotNull(message = "Image file is required")
    @Size(max = 67108864, message = "Image file size cannot exceed 64MB") // 64MB in bytes
    private byte[] imgFile;
    
    @NotBlank(message = "Image name is required")
    @Size(max = 100, message = "Image name cannot exceed 100 characters")
    private String imgName;

    public int getImgId() {
        return imgId;
    }

    public void setImgId(int imgId) {
        this.imgId = imgId;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public byte[] getImgFile() {
        return imgFile;
    }

    public void setImgFile(byte[] imgFile) {
        this.imgFile = imgFile;
    }

    public String getImgName() {
        return imgName;
    }

    public void setImgName(String imgName) {
        this.imgName = imgName;
    }
}
