package com.homemate.admin.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class TaskerDto {
    Long taskerID;
    String username;
    String email;
    String phone;
    boolean admin;
    boolean suspended;
    Double AvgRating;
    String fName;
    String lName;
    Double hourRate;
    Gender gender;

    public enum Gender {
        M,
        F,
    }

}
