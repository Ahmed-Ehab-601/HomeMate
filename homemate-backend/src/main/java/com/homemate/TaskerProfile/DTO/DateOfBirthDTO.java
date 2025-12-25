package com.homemate.TaskerProfile.DTO;

import lombok.Data;

import java.sql.Timestamp;
@Data
public class DateOfBirthDTO {
    private Long taskerID;
    private Timestamp newDateOfBirth;

}

