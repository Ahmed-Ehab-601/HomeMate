package com.homemate.TaskerProfile.DTO;

public class HourRateDTO {
    private Long taskerID;
    private Double newHourRate;

    public Long getTaskerID() {
        return taskerID;
    }

    public void setTaskerID(Long taskerID) {
        this.taskerID = taskerID;
    }

    public Double getNewHourRate() {
        return newHourRate;
    }

    public void setNewHourRate(Double newHourRate) {
        this.newHourRate = newHourRate;
    }
}

