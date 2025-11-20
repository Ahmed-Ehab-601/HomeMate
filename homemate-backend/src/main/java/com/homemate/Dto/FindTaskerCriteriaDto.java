package com.homemate.Dto;

public class FindTaskerCriteriaDto {

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

    public Integer getServiceID() { return serviceID; }
    public void setServiceID(Integer serviceID) { this.serviceID = serviceID; }

    public String getAvailability() { return availability; }
    public void setAvailability(String availability) { this.availability = availability; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public Double getMinRating() { return minRating; }
    public void setMinRating(Double minRating) { this.minRating = minRating; }

    public Double getMaxRating() { return maxRating; }
    public void setMaxRating(Double maxRating) { this.maxRating = maxRating; }

    public Double getMinHourRate() { return minHourRate; }
    public void setMinHourRate(Double minHourRate) { this.minHourRate = minHourRate; }

    public Double getMaxHourRate() { return maxHourRate; }
    public void setMaxHourRate(Double maxHourRate) { this.maxHourRate = maxHourRate; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getSortBy() { return sortBy; }
    public void setSortBy(String sortBy) { this.sortBy = sortBy; }

    public String getSortOrder() { return sortOrder; }
    public void setSortOrder(String sortOrder) { this.sortOrder = sortOrder; }

    public String getSearch() { return search; }
    public void setSearch(String search) { this.search = search; }
}
