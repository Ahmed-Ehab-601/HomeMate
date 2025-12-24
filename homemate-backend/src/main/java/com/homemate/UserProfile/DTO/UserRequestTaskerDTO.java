package com.homemate.UserProfile.DTO;

import com.homemate.UserProfile.Models.UserTaskerAvailability;
import lombok.Data;

@Data
public class UserRequestTaskerDTO {

    private Long taskerId;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String username;
    private byte[] image;
    private Double rating;
    private String bio;
    private String serviceName;
    private String addressCity;
    private UserTaskerAvailability availability;
    private Double hourRate;
    private Double workedHours;
}
