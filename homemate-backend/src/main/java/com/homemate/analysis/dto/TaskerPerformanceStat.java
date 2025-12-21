package com.homemate.analysis.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TaskerPerformanceStat {
    private int taskerId;
    private String username;
    private double totalEarnings;
    private double workedHours;
    private double averageRating;
    // Task counts
    private int tasksCompleted; // Done
    private int tasksPending;   // InReview/Accepted
    private int tasksRejected;
}
