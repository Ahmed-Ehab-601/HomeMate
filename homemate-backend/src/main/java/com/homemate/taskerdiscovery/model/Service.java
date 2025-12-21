package com.homemate.taskerdiscovery.model;


import lombok.Data;

@Data
public class Service {
    int serviceId;
    String serviceName;
    String description;
    private String imageData;
    private String imageName;
    private String imageType;


    public Service() {}

    public Service(int serviceid, String servicename, String description,
                   String imageData, String imageName, String imageType) {
        this.serviceId = serviceid;
        this.serviceName = servicename;
        this.description = description;
        this.imageData = imageData;
        this.imageName = imageName;
        this.imageType = imageType;
    }
}
