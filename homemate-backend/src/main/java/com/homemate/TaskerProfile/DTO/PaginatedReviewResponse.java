package com.homemate.TaskerProfile.DTO;

import lombok.Data;

import java.util.List;
@Data
public class PaginatedReviewResponse {
    private List<ReviewDTO> reviews;
    private int currentPage;
    private int pageSize;
    private long totalReviews;
    private int totalPages;

}

