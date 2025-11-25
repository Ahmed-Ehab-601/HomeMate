package com.homemate.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FindTaskerCriteriaDto {
    @NotNull
    private Integer serviceID;
    private String availability;
    private String gender;
    private Double minRating;
    private Double maxRating;
    private Double minHourRate;
    private Double maxHourRate;
    private String city;
    private String sortBy;
    private String sortOrder;
    private String search;
}
