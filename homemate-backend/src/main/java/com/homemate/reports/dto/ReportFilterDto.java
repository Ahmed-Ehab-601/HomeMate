package com.homemate.reports.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReportFilterDto {
    private String header;
    private String body;
    private Integer taskID;
    private Boolean reporter;
    private String adminStatus;
}
