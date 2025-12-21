package com.homemate.analysis.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TaskAnalysisDto {
    private Map<String, Integer> statusOverview;
    private Map<String, Integer> tasksPerService;
    private Map<String, Double> averageBillPerService;
    
    // Time Analysis
    private Double averageTaskDurationHours;
    private Map<Integer, Integer> peakCreationHours; // Hour (0-23) -> Count
    
    // Problematic
    private Integer tasksWithReportsCount;
    private Integer tasksWithDelayedCompletionCount;
}
