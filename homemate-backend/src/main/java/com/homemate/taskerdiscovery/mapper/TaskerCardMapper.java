package com.homemate.taskerdiscovery.mapper;

import com.homemate.taskerdiscovery.dto.TaskerCardDto;
import com.homemate.taskerdiscovery.model.Tasker;
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
        dto.setStripe_account_id(tasker.getStripe_account_id());

        if (tasker.getImage() != null && !tasker.getImage().isEmpty()) {
            dto.setImageBase64(tasker.getImage());
        }
        return dto;
    }
}
