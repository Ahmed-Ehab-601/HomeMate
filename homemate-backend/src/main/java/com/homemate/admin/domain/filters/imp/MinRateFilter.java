package com.homemate.admin.domain.filters.imp;

import com.homemate.admin.domain.filters.Filter;

import java.util.List;
import java.util.Map;

public class MinRateFilter implements Filter {
    private final Double minRate;

    public MinRateFilter(Double minRate) {
        this.minRate = minRate;
    }

    @Override
    public Map<String, Object> getParams() {
        return minRate != null ? Map.of("minRate", minRate) : Map.of();
    }


    @Override
    public List<String> getConditions() {
        return minRate != null ? List.of("rating >= ?") : List.of();
    }

}
