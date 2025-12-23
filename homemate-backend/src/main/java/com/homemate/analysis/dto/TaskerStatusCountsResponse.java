package com.homemate.analysis.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TaskerStatusCountsResponse {
    private long suspended;
    private long active;
}
