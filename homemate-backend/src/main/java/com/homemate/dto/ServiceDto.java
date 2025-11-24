package com.homemate.dto;

public class ServiceDto {
    int serviceid;
    String servicename;
    String description;
    private String imageData;
    private String imageName;
    private String imageType;


    private int totalTasks;

    public ServiceDto() {
    }

    public int getServiceid() {
        return serviceid;
    }

    public void setServiceid(int serviceid) {

        this.serviceid = serviceid;
        this.totalTasks=0;
    }

    public String getServicename() {

        return servicename;
    }

    public void setServicename(String servicename) {

        this.servicename = servicename;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageData() {
        return imageData;
    }

    public void setImageData(String imageData) {
        this.imageData = imageData;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public String getImageType() {
        return imageType;
    }

    public void setImageType(String imageType) {
        this.imageType = imageType;
    }

    public int getTotalTasks() {
        return totalTasks;
    }

    public void setTotalTasks(int totalTasks) {
        this.totalTasks = totalTasks;
    }

}