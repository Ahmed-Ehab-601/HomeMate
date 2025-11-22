package com.homemate.Authentication.Entity;

import java.time.LocalDateTime;

public class Tasker {
    private Integer taskerID;
    private String firstName;
    private String lastName;
    private String username;
    private String password;     // hashed password
    private String email;
    private LocalDateTime birthDate;
    private String phone;
    private String gender;       // 'M' / 'F'
    private byte[] image;        // LONGBLOB
    private String availability; // 'available' / 'unavailable'
    private double rating;       // DECIMAL(5,2)
    private double hourRate;     // DECIMAL(10,2)
    private String bio;
    private Integer serviceID;
    private double totalEarning;
    private double workedHours;
    private String addressCity;

    // Getters & Setters
    public Integer getTaskerID() { return taskerID; }
    public void setTaskerID(Integer taskerID) { this.taskerID = taskerID; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public LocalDateTime getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDateTime birthDate) { this.birthDate = birthDate; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public byte[] getImage() { return image; }
    public void setImage(byte[] image) { this.image = image; }

    public String getAvailability() { return availability; }
    public void setAvailability(String availability) { this.availability = availability; }

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }

    public double getHourRate() { return hourRate; }
    public void setHourRate(double hourRate) { this.hourRate = hourRate; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public Integer getServiceID() { return serviceID; }
    public void setServiceID(Integer serviceID) { this.serviceID = serviceID; }

    public double getTotalEarning() { return totalEarning; }
    public void setTotalEarning(double totalEarning) { this.totalEarning = totalEarning; }

    public double getWorkedHours() { return workedHours; }
    public void setWorkedHours(double workedHours) { this.workedHours = workedHours; }

    public String getAddressCity() { return addressCity; }
    public void setAddressCity(String addressCity) { this.addressCity = addressCity; }
}

