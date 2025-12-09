package com.homemate.reviews.DTO;

import java.sql.Timestamp;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class ReviewsDTO {
    private int reviewId;
    
    @Size(max = 50, message = "Review text cannot exceed 50 characters")
    private String text;
    
    @NotNull(message = "Rating is required")
    @DecimalMin(value = "0.0", message = "Rating must be at least 0.0")
    @DecimalMax(value = "5.0", message = "Rating cannot exceed 5.0")
    private double rate;
    
    private Timestamp time;
    
    @NotNull(message = "Task ID is required")
    @Positive(message = "Task ID must be positive")
    private int taskId;
    
    @Valid // Validates nested ReviewImagesDTO objects
    @Size(max = 5, message = "Cannot add more than 5 images to a review")
    private List<ReviewImagesDTO> reviewImages;

    public int getReviewId() {
        return reviewId;
    }

    public void setReviewId(int reviewId) {
        this.reviewId = reviewId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public double getRate() {
        return rate;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }

    public Timestamp getTime() {
        return time;
    }

    public void setTime(Timestamp time) {
        this.time = time;
    }

    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public List<ReviewImagesDTO> getReviewImages() {
        return reviewImages;
    }

    public void setReviewImages(List<ReviewImagesDTO> reviewImages) {
        this.reviewImages = reviewImages;
    }
}
