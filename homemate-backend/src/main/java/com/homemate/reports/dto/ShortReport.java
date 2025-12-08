package com.homemate.reports.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ShortReport {
    private int reportID;
    private String header;
    private int taskID;
    private boolean reporter; // true -> user, false -> tasker
    private String adminStatus;
}
