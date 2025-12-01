package com.homemate.UserProfile.DTO;

import java.sql.Timestamp;

public class DateOfBirthDTO {

    private Long userId;
    private Timestamp dateOfBirth;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Timestamp getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Timestamp dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }
}

