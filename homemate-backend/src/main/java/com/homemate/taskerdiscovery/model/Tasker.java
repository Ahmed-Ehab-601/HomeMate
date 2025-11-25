package com.homemate.taskerdiscovery.model;

public class Tasker {

    private int taskerID;
    private String firstName;
    private String lastName;
    private byte[] image;
    private String availability;
    private double rating;
    private double hourRate;
    private String bio;
    private String addressCity;

    public int getTaskerID() {
        return taskerID;
    }
    public void setTaskerID(int taskerID) {
        this.taskerID = taskerID; }

    public String getFirstName() {
        return firstName;
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public byte[] getImage() {
        return image;
    }
    public void setImage(byte[] image) {
        this.image = image;
    }

    public String getAvailability() {
        return availability;
    }
    public void setAvailability(String availability) {
        this.availability = availability;
    }

    public double getRating() {
        return rating;
    }
    public void setRating(double rating) {
        this.rating = rating;
    }

    public double getHourRate() {
        return hourRate;
    }
    public void setHourRate(double hourRate) {
        this.hourRate = hourRate;
    }

    public String getBio() {
        return bio;
    }
    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getAddressCity() {
        return addressCity;
    }
    public void setAddressCity(String addressCity) {
        this.addressCity = addressCity;
    }
}
