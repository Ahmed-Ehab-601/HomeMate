package com.homemate.Mapper;

import com.homemate.Dto.ServiceDto;
import com.homemate.Model.ServiceEntity;

import java.nio.charset.StandardCharsets;

public class ServiceMapper {
    public static ServiceEntity mapFromDto(ServiceDto serviceDto) {
        return ServiceEntity.builder()
        .id(serviceDto.getId())
        .name(serviceDto.getName())
        .description(serviceDto.getDescription())
        .imageData(serviceDto.getImageData()!=null?serviceDto.getImageData().getBytes(StandardCharsets.UTF_8): null)
        .imageName(serviceDto.getImageName())
        .imageType(serviceDto.getImageType())
        .build();
    }
    public static ServiceDto mapToDto(ServiceEntity service) {
        return ServiceDto.builder()
        .id(service.getId())
        .name(service.getName())
        .description(service.getDescription())
        .imageData(service.getImageData()!=null?new String(service.getImageData(), StandardCharsets.UTF_8) : null)
        .imageName(service.getImageName())
        .imageType(service.getImageType())
        .build();
    }
    
}
