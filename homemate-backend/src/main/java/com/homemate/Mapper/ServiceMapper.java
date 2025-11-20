package com.homemate.Mapper;

import com.homemate.Dto.ServiceDto;
import com.homemate.Model.ServiceEntity;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component

public class ServiceMapper {
    public  ServiceEntity mapFromDto(ServiceDto serviceDto) {
        return ServiceEntity.builder()
        .id(serviceDto.getId())
        .name(serviceDto.getName())
        .description(serviceDto.getDescription())
        .imageData(serviceDto.getImageData() != null ?
                        Base64.getDecoder().decode(serviceDto.getImageData()) : null)
        .imageName(serviceDto.getImageName())
        .imageType(serviceDto.getImageType())
        .build();
    }
    public  ServiceDto mapToDto(ServiceEntity service) {
        return ServiceDto.builder()
        .id(service.getId())
        .name(service.getName())
        .description(service.getDescription())
        .imageData(service.getImageData() != null ?
                Base64.getEncoder().encodeToString(service.getImageData()) : null)
        .imageName(service.getImageName())
        .imageType(service.getImageType())
        .build();
    }
    
}
