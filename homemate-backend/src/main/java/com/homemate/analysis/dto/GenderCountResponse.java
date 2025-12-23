package com.homemate.analysis.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GenderCountResponse {
    private long male;
    private long female;
}
