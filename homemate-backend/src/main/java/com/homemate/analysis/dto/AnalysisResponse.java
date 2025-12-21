package com.homemate.analysis.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AnalysisResponse {
    private UserAnalysisDto userAnalysis;
    private TaskerAnalysisDto taskerAnalysis;
    private TaskAnalysisDto taskAnalysis;
}
