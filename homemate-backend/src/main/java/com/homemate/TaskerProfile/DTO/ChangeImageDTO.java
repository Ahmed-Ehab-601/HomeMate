package com.homemate.TaskerProfile.DTO;

import lombok.Data;

@Data
public class ChangeImageDTO {
    private Long taskerID;
    private byte[] newImage;

}

