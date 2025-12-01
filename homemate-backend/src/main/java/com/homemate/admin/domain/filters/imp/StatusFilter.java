package com.homemate.admin.domain.filters.imp;

import com.homemate.admin.domain.filters.Filter;

import java.util.List;
import java.util.Map;

public class StatusFilter implements Filter {
    private  final Boolean suspended;

    public StatusFilter(Boolean suspended) {
        this.suspended = suspended;
    }

    @Override
    public Map<String, Object> getParams() {
        return suspended!=null ? Map.of("suspended", suspended) :Map.of();
    }

    @Override
    public List<String> getConditions() {
        return suspended!=null? List.of("suspended = ?"):List.of();
    }
}
