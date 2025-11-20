package com.homemate.Dto;

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


    public int getTaskerId() { return taskerId; }
    public void setTaskerId(int taskerId) { this.taskerId = taskerId; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getImageBase64() { return imageBase64; }
    public void setImageBase64(String imageBase64) { this.imageBase64 = imageBase64; }

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }

    public String getAvailability() { return availability; }
    public void setAvailability(String availability) { this.availability = availability; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getAddressCity() { return addressCity; }
    public void setAddressCity(String addressCity) { this.addressCity = addressCity; }

    public double getHourRate() { return hourRate; }
    public void setHourRate(double hourRate) { this.hourRate = hourRate; }
}
