package com.homemate.analysis.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskStatusCountsResponse {
    private Long inReview;
    private Long accepted;
    private Long inProgress;
    private Long suspended;
    private Long done;
    private Long rejected;
}
