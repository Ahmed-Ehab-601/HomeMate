package com.homemate.taskerdiscovery.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ServiceDto {
    private int serviceId;
    private String serviceName;
    private String description;
    private String imageData;
    private String imageName;
    private String imageType;
    private int totalTasks;
}