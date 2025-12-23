package com.homemate.analysis.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class WorkedHoursRangesResponse {
    private Map<String, Long> ranges;
}
