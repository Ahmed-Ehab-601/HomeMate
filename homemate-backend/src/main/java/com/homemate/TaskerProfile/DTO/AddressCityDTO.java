package com.homemate.TaskerProfile.DTO;

public class AddressCityDTO {
    private Long taskerID;
    private String newAddressCity;

    public Long getTaskerID() {
        return taskerID;
    }

    public void setTaskerID(Long taskerID) {
        this.taskerID = taskerID;
    }

    public String getNewAddressCity() {
        return newAddressCity;
    }

    public void setNewAddressCity(String newAddressCity) {
        this.newAddressCity = newAddressCity;
    }
}