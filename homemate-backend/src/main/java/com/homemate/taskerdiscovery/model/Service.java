package com.homemate.taskerdiscovery.model;

public class Service {
    int serviceId;
    String serviceName;
    String description;
    private byte[] imageData;
    private String imageName;
    private String imageType;


    public Service() {}

    public Service(int serviceid, String servicename, String description,
                   byte[] imageData, String imageName, String imageType) {
        this.serviceId = serviceid;
        this.serviceName = servicename;
        this.description = description;
        this.imageData = imageData;
        this.imageName = imageName;
        this.imageType = imageType;
    }



    public int getServiceId() {

        return serviceId;
    }

    public void setServiceId(int serviceId) {

        this.serviceId = serviceId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {

        this.serviceName = serviceName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {

        this.description = description;
    }

    public byte[] getImageData() {
        return imageData;
    }

    public void setImageData(byte[] imageData) {
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

}
