package com.homemate.taskerdiscovery.mapper;

import com.homemate.taskerdiscovery.dto.ServiceDto;
import com.homemate.taskerdiscovery.model.Service;
import org.springframework.stereotype.Component;

import java.util.Base64;

@Component("taskerDiscoveryServiceMapper")
public class ServiceMapper {

    public ServiceDto mapToServiceDto(Service service, int totalTasks) {

        ServiceDto dto = new ServiceDto();
        dto.setServiceId(service.getServiceId());
        dto.setServiceName(service.getServiceName());
        dto.setDescription(service.getDescription());

        if (service.getImageData() != null && !service.getImageData().isEmpty()) {
            dto.setImageData(service.getImageData());
            dto.setImageName(service.getImageName());
            dto.setImageType(service.getImageType());
        } else {
            dto.setImageData(null);
        }

        dto.setTotalTasks(totalTasks);

        return dto;
    }
}
