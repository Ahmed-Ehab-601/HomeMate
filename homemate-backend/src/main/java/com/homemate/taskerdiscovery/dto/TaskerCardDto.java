package com.homemate.taskerdiscovery.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskerCardDto {
    private int taskerId;
    private String firstName;
    private String lastName;
    private String imageBase64;
    private double rating;
    private String availability;
    private String bio;
    private String addressCity;
    private double hourRate;
    private String stripe_account_id;
}
