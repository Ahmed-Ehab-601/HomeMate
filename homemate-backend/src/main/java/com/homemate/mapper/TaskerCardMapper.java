package com.homemate.mapper;

import com.homemate.dto.TaskerCardDto;
import com.homemate.model.Tasker;
import org.springframework.stereotype.Component;
import java.util.Base64;

@Component
public class TaskerCardMapper {

    public TaskerCardDto mapToDto(Tasker tasker) {
        TaskerCardDto dto = new TaskerCardDto();
        dto.setTaskerId(tasker.getTaskerID());
        dto.setFirstName(tasker.getFirstName());
        dto.setLastName(tasker.getLastName());
        dto.setRating(tasker.getRating());
        dto.setAvailability(tasker.getAvailability());
        dto.setBio(tasker.getBio());
        dto.setAddressCity(tasker.getAddressCity());
        dto.setHourRate(tasker.getHourRate());

        if (tasker.getImage() != null && tasker.getImage().length > 0) {
            dto.setImageBase64(Base64.getEncoder().encodeToString(tasker.getImage()));
        }
        return dto;
    }
}
