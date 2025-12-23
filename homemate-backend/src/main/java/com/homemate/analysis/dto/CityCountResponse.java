package com.homemate.analysis.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class CityCountResponse {
    private Map<String, Long> counts; // Map of city name to count
}
