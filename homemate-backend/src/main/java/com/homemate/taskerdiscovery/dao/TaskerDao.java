package com.homemate.taskerdiscovery.dao;

import com.homemate.taskerdiscovery.dto.FindTaskerCriteriaDto;
import com.homemate.taskerdiscovery.model.Tasker;
import java.util.List;

public interface TaskerDao {
    List<Tasker> findTaskersWithFilters(FindTaskerCriteriaDto filter, int page, int pageSize);
}
