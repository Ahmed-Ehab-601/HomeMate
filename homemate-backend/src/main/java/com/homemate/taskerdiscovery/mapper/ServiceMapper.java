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

        if (service.getImageData() != null && service.getImageData().length > 0) {
            String base64Image = Base64.getEncoder().encodeToString(service.getImageData());
            dto.setImageData(base64Image);
            dto.setImageName(service.getImageName());
            dto.setImageType(service.getImageType());
        } else {
            dto.setImageData(null);
        }

        dto.setTotalTasks(totalTasks);

        return dto;
    }
}
