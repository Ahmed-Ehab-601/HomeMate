package com.homemate.Admin.domain.filters.imp;

import com.homemate.Admin.domain.filters.Filter;

import java.util.List;
import java.util.Map;

public class RoleUserFilter implements Filter {
    private final Boolean admin;

    public RoleUserFilter(Boolean admin) {
        this.admin = admin;
    }

    @Override
    public Map<String, Object> getParams() {
        return admin != null ? Map.of("admin", admin) : Map.of();
    }

    @Override
    public List<String> getConditions() {
        return admin != null ? List.of("admin = ?") : List.of();
    }
}
