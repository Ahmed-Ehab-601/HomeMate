package com.homemate.analysis.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskStatusCountsResponse {
    private long inReview;
    private long accepted;
    private long inProgress;
    private long suspended;
    private long done;
    private long rejected;
}
