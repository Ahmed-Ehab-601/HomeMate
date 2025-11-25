package com.homemate.dao;

import com.homemate.dto.FindTaskerCriteriaDto;
import com.homemate.model.Tasker;
import java.util.List;

public interface TaskerDao {
    List<Tasker> findTaskersWithFilters(FindTaskerCriteriaDto filter, int page, int pageSize);
}
