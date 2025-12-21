package com.homemate.taskerdiscovery.model;

import lombok.Data;


@Data
public class Tasker {

    private int taskerID;
    private String firstName;
    private String lastName;
    private String image;
    private String availability;
    private double rating;
    private double hourRate;
    private String bio;
    private String addressCity;

}
