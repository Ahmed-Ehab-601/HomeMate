package com.homemate.UserProfile.DTO;

import lombok.Data;

import java.sql.Timestamp;
@Data
public class DateOfBirthDTO {

    private Long userId;
    private Timestamp dateOfBirth;
}

