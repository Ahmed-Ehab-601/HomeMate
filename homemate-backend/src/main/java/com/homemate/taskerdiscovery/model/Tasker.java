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
    private String stripe_account_id;


    public String getStripe_account_id() {
        return stripe_account_id;
    }

    public void setStripe_account_id(String stripe_account_id) {
        this.stripe_account_id = stripe_account_id;
    }


}
