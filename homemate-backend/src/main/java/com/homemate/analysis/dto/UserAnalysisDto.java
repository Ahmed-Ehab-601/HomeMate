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
public class UserAnalysisDto {
    private Map<String, Integer> genderDistribution;
    private Map<String, Integer> ageDistribution; // Age -> Count
    
    // Activity
    private Integer suspendedUsersCount;
    
    // Retention & Engagement
    private Double averageTasksPerUser;
    private Integer usersWithNoCompletedTasksCount;
}
