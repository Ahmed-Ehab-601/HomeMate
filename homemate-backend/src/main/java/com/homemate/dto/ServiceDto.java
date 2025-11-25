package com.homemate.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ServiceDto {
    int serviceId;
    String serviceName;
    String description;
    private String imageData;
    private String imageName;
    private String imageType;
    private int totalTasks;
}