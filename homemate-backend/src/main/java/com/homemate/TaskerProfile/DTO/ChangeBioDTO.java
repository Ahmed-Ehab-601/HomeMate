package com.homemate.TaskerProfile.DTO;

public class ChangeBioDTO {
    private Long taskerID;
    private String newBio;

    public Long getTaskerID() {
        return taskerID;
    }

    public void setTaskerID(Long taskerID) {
        this.taskerID = taskerID;
    }

    public String getNewBio() {
        return newBio;
    }

    public void setNewBio(String newBio) {
        this.newBio = newBio;
    }
}

