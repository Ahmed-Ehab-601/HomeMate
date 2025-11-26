package com.homemate.TaskerProfile.DTO;

public class PasswordDTO {
    private Long taskerID;
    private String oldPassword;
    private String newPassword;

    public Long getTaskerID() {
        return taskerID;
    }

    public void setTaskerID(Long taskerID) {
        this.taskerID = taskerID;
    }

    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}

