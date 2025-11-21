package com.homemate.Dto;

import com.homemate.Model.Service;
import org.springframework.stereotype.Component;

import java.util.Base64;

@Component
public class ObjectMapper {

    public ServiceDto mapToServiceDto(Service service, int totalTasks) {

        ServiceDto dto = new ServiceDto();
        dto.setServiceid(service.getServiceid());
        dto.setServicename(service.getServicename());
        dto.setDescription(service.getDescription());

        if (service.getImagedata() != null && service.getImagedata().length > 0) {
            String base64Image = Base64.getEncoder().encodeToString(service.getImagedata());
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
