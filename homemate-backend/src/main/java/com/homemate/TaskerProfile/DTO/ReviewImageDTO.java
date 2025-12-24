package com.homemate.TaskerProfile.DTO;

import lombok.Data;

@Data
public class ReviewImageDTO {
    private int imgId;
    private String format;
    private byte[] imgFile;
    private String imgName;

}
