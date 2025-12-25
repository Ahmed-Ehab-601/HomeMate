package com.homemate.UserProfile.DTO;

import lombok.Data;

@Data
public class AddressDTO {

    private Long addressId;
    private Long userID;    
    private String country;
    private String city;
    private String street;
    private String apartment;

}

