package com.homemate.TaskerProfile.DTO;

import lombok.Data;

@Data
public class ChangeAvailabilityDTO {
    private Long taskerID;
    private String newAvailability;
}

