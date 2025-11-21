package com.homemate.Admin.domain.filters.imp;

import com.homemate.Admin.domain.filters.Filter;

import java.util.List;
import java.util.Map;

public class UsernameFilter implements Filter {
    private final String username;

    public UsernameFilter(String username) {
        this.username = username;
    }

    @Override
    public Map<String, Object> getParams() {
        return username != null && !username.isEmpty()
                ? Map.of("username", "%" + username + "%")
                : Map.of();
    }

    @Override
    public List<String> getConditions() {
        return username != null && !username.isEmpty()
                ? List.of("username LIKE ?")
                : List.of();
    }
}
