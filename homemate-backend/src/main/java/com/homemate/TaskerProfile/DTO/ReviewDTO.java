package com.homemate.TaskerProfile.DTO;

import lombok.Data;

import java.sql.Timestamp;
import java.util.List;
@Data
public class ReviewDTO {
    private int reviewId;
    private String text;
    private double rate;
    private Timestamp time;
    private int taskId;
    private List<ReviewImageDTO> reviewImages;
    private String reviewerUsername;

}
