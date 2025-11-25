package com.homemate.TaskerProfile.DTO;

public class ChangeAvailabilityDTO {
    private Long taskerID;
    private String newAvailability;

    public Long getTaskerID() {
        return taskerID;
    }

    public void setTaskerID(Long taskerID) {
        this.taskerID = taskerID;
    }

    public String getNewAvailability() {
        return newAvailability;
    }

    public void setNewAvailability(String newAvailability) {
        this.newAvailability = newAvailability;
    }
}

