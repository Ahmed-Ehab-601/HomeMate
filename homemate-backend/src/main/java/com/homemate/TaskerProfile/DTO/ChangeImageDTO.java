package com.homemate.TaskerProfile.DTO;

public class ChangeImageDTO {
    private Long taskerID;
    private byte[] newImage;

    public Long getTaskerID() {
        return taskerID;
    }

    public void setTaskerID(Long taskerID) {
        this.taskerID = taskerID;
    }

    public byte[] getNewImage() {
        return newImage;
    }

    public void setNewImage(byte[] newImage) {
        this.newImage = newImage;
    }
}

