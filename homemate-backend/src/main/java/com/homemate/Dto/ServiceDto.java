package com.homemate.Dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ServiceDto {
    private long id;
    private String name;
    private String description;
    private String imageData;
    private String imageName;
    private String imageType;

}


