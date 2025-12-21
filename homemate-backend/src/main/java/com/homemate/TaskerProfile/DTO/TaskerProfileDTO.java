package com.homemate.TaskerProfile.DTO;

import lombok.Data;

import java.sql.Timestamp;
@Data
public class TaskerProfileDTO {
    private Long taskerID;
    private String firstName;
    private String lastName;
    private String username;
    private String email;
    private Timestamp birthDate;
    private String phone;
    private Character gender;
    private String availability;
    private Double rating;
    private Double hourrate;
    private String bio;
    private Long serviceID;
    private Double totalEarning;
    private Double workedHours;
    private String addressCity;
    private byte[] image;
    private String stripeAccountId;

    public String getStripeAccountId() {
        return stripeAccountId;
    }

    public void setStripeAccountId(String stripeAccountId) {
        this.stripeAccountId = stripeAccountId;
    }

}

