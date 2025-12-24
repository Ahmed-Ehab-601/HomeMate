package com.homemate.TaskerProfile.DTO;

import lombok.Data;

@Data
public class PaginatedReviewRequest {
    private Long taskerID;
    private int page;
    private int pageSize;

}

