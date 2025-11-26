package com.homemate.admin.dao.imp;

import com.homemate.admin.domain.filters.Filter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class Helper {



    public  static String buildWhere(List<Filter> filters) {
        if (filters == null || filters.isEmpty()) {
            return "";
        }

        List<String> conditions = filters.stream()
                .flatMap(filter -> filter.getConditions().stream())
                .filter(cond -> cond != null && !cond.trim().isEmpty())
                .collect(Collectors.toList());

        if (conditions.isEmpty()) {
            return "";
        }

        return " WHERE " + String.join(" AND ", conditions);
    }
    public static List<Object> buildParams(List<Filter> filters) {
        List<Object> params = new ArrayList<>();

        if (filters != null) {
            for (Filter filter : filters) {
                params.addAll(filter.getParams().values());
            }
        }

        return params;
    }
}
