package com.homemate.UserProfile.DTO;

public class AddressDTO {

    private Long addressId;
    private Long userID;    
    private String country;
    private String city;
    private String street;
    private String apartment;

    public Long getAddressId() {
        return addressId;
    }

    public void setAddressId(Long addressId) {
        this.addressId = addressId;
    }
    public Long getUserId() {
        return userID;
    }

    public void setUserId(Long userID) {
        this.userID = userID;
    }
   

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getApartment() {
        return apartment;
    }

    public void setApartment(String apartment) {
        this.apartment = apartment;
    }
}

