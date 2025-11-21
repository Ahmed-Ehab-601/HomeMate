package com.homemate.Admin.domain.filters;

import java.util.List;
import java.util.Map;

public interface Filter {

    Map<String, Object> getParams();

    List<String> getConditions();
}
