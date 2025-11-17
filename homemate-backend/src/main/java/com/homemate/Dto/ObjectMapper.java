package com.homemate.Dto;

import com.homemate.Model.Service;
import org.springframework.stereotype.Component;

@Component
public class ObjectMapper {

    public ServiceDto mapToServiceDto(Service service) {

        ServiceDto dto = new ServiceDto();
        dto.setServiceid(service.getServiceid());
        dto.setServicename(service.getServicename());
        dto.setDescription(service.getDescription());

        return dto;
    }
}
