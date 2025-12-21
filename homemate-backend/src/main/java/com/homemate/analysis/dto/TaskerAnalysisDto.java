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
public class TaskerAnalysisDto {
    private Map<String, Integer> taskersPerService;
    private Map<String, Integer> availabilityDistribution;
}
