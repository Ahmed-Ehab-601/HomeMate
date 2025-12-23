package com.homemate.analysis.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StatusCountsResponse {
    private long suspended;
    private long adminActive;
    private long nonAdminActive;
}
