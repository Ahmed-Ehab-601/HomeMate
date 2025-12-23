package com.homemate.analysis.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StatusCountsResponse {
    private Long suspended;
    private Long adminActive;
    private Long nonAdminActive;
}
