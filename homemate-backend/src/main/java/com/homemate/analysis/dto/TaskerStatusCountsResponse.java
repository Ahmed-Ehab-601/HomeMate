package com.homemate.analysis.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TaskerStatusCountsResponse {
    private Long suspended;
    private Long active;
}
