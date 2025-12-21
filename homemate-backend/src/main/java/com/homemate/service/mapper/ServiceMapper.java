package com.homemate.service.mapper;

import com.homemate.service.dto.ServiceDto;
import com.homemate.service.model.ServiceEntity;
import org.springframework.stereotype.Component;

import java.util.Base64;

@Component("serviceMapper")

public class ServiceMapper {
    public  ServiceEntity mapFromDto(ServiceDto serviceDto) {
        return ServiceEntity.builder()
        .id(serviceDto.getId())
        .name(serviceDto.getName())
        .description(serviceDto.getDescription())
        .imageData(serviceDto.getImageData())
        .imageName(serviceDto.getImageName())
        .imageType(serviceDto.getImageType())
        .build();
    }
    public  ServiceDto mapToDto(ServiceEntity service) {
        return ServiceDto.builder()
        .id(service.getId())
        .name(service.getName())
        .description(service.getDescription())
        .imageData(service.getImageData())
        .imageName(service.getImageName())
        .imageType(service.getImageType())
        .build();
    }
    
}
