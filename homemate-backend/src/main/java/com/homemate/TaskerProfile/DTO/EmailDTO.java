package com.homemate.TaskerProfile.DTO;

public class EmailDTO {
    private Long taskerID;
    private String newEmail;

    public Long getTaskerID() {
        return taskerID;
    }

    public void setTaskerID(Long taskerID) {
        this.taskerID = taskerID;
    }

    public String getNewEmail() {
        return newEmail;
    }

    public void setNewEmail(String newEmail) {
        this.newEmail = newEmail;
    }
}

