package com.homemate.analysis.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AvgRatingPerServiceResponse {
    private Map<String, Double> ratings;
}
