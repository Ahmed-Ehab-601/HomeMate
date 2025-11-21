package com.homemate.Admin.domain.filters;

import java.util.List;
import java.util.Map;

public interface Filter {
    /**
     * Returns filter parameters as key-value pairs
     */
    Map<String, Object> getParams();

    /**
     * Returns filter conditions/criteria
     */
    List<String> getConditions();
}
