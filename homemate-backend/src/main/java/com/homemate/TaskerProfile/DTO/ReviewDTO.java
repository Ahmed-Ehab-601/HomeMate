package com.homemate.TaskerProfile.DTO;

import java.sql.Timestamp;
import java.util.List;

public class ReviewDTO {
    private int reviewId;
    private String text;
    private double rate;
    private Timestamp time;
    private int taskId;
    private List<ReviewImageDTO> reviewImages;

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

    public List<ReviewImageDTO> getReviewImages() {
        return reviewImages;
    }

    public void setReviewImages(List<ReviewImageDTO> reviewImages) {
        this.reviewImages = reviewImages;
    }
}
