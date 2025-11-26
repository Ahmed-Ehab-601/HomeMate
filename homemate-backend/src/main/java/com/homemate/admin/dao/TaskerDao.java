package com.homemate.admin.dao;

import com.homemate.admin.domain.entities.Tasker;
import com.homemate.admin.domain.filters.Filter;

import java.util.List;

public interface TaskerDao {
    List<Tasker> findTaskers(List<Filter> filters,Long limit , Long offest);
    Long countTaskers(List<Filter> filters);
    Tasker findById(Long taskerId);
    boolean updateTaskerSuspended(Long taskerId, boolean suspended);
    Long computeNumberOfTasks(Long taskerId);


}
