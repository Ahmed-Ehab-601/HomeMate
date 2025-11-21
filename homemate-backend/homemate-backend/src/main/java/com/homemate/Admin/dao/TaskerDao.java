package com.homemate.Admin.dao;

import com.homemate.Admin.domain.entities.Tasker;
import com.homemate.Admin.domain.entities.User;
import com.homemate.Admin.domain.filters.Filter;

import java.util.List;

public interface TaskerDao {
    List<Tasker> findTaskers(List<Filter> filters,Long limit , Long offest);
    Long countTaskers(List<Filter> filters);


}
