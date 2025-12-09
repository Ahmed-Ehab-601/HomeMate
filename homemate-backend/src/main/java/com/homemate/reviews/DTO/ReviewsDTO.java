package com.homemate.reviews.DTO;

import java.sql.Timestamp;
import java.util.List;

public class ReviewsDTO {
    private int reviewId;
    private String text;
    private double rate;
    private Timestamp time;
    private int taskId;
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
