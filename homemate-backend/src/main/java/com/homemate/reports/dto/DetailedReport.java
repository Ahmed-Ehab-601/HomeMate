package com.homemate.reports.dto;

import com.homemate.reports.model.AdminStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class DetailedReport {
    private int reportID;
    private String header;
    private String body;
    private int taskID;
    private int userID;
    private String userUsername;
    private String userEmail;
    private boolean userSuspended;
    private int taskerID;
    private String taskerEmail;
    private String taskerUsername;
    private boolean taskerSuspended;
    private boolean reporter; // true -> user, false -> tasker
    private AdminStatus adminStatus;
}