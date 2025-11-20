package com.homemate.Dao;

import com.homemate.Dto.FindTaskerCriteriaDto;
import com.homemate.Model.Tasker;
import java.util.List;

public interface TaskerDao {
    List<Tasker> findTaskersWithFilters(FindTaskerCriteriaDto filter, int page, int pageSize);
}
