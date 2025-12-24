package com.homemate.analysis.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class NewAccountsResponse {
    private List<Long> counts;
}
