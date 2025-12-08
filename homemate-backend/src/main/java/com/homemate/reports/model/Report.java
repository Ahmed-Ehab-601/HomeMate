package com.homemate.reports.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class Report {
    private int reportID;
    private String header;
    private String body;
    private int taskID;
    private boolean reporter; // true -> user, false -> tasker
    private String adminStatus;
}
