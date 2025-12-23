package com.homemate.TaskerProfile.models;

import java.sql.Timestamp;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Tasker {
    private Long taskerID;
    private String firstName;
    private String lastName;
    private String username;
    private String password;
    private String email;
    private Timestamp birthDate;
    private String phone;
    private Character gender;
    private byte[] image;
    private String availability;
    private Double rating;
    private Double hourrate;
    private String bio;
    private Long serviceID;
    private Double totalEarning;
    private Double workedHours;
    private String addressCity;
    private String serviceName;
    private Boolean isSuspended;
}
