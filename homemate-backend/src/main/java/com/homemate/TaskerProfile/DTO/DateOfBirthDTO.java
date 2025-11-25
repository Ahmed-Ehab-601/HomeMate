package com.homemate.TaskerProfile.DTO;

import java.sql.Timestamp;

public class DateOfBirthDTO {
    private Long taskerID;
    private Timestamp newDateOfBirth;

    public Long getTaskerID() {
        return taskerID;
    }

    public void setTaskerID(Long taskerID) {
        this.taskerID = taskerID;
    }

    public Timestamp getNewDateOfBirth() {
        return newDateOfBirth;
    }

    public void setNewDateOfBirth(Timestamp newDateOfBirth) {
        this.newDateOfBirth = newDateOfBirth;
    }
}

