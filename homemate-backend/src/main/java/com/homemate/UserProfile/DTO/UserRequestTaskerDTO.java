package com.homemate.UserProfile.DTO;

import com.homemate.UserProfile.Models.UserTaskerAvailability;

public class UserRequestTaskerDTO {

    private Long taskerId;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String username;
    private String image;
    private Double rating;
    private String bio;
    private String serviceName;
    private String addressCity;
    private UserTaskerAvailability availability;
    private Double hourRate;
    private Double workedHours;

    public Long getTaskerId() {
        return taskerId;
    }

    public void setTaskerId(Long taskerId) {
        this.taskerId = taskerId;
    }

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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getAddressCity() {
        return addressCity;
    }

    public void setAddressCity(String addressCity) {
        this.addressCity = addressCity;
    }

    public UserTaskerAvailability getAvailability() {
        return availability;
    }

    // Setter
    public void setAvailability(UserTaskerAvailability availability) {
        this.availability = availability;
    }
    public Double getHourRate() {
        return hourRate;
    }

    public void setHourRate(Double hourRate) {
        this.hourRate = hourRate;
    }

    public Double getWorkedHours() {
        return workedHours;
    }

    public void setWorkedHours(Double workedHours) {
        this.workedHours = workedHours;
    }
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
    }
