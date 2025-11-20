package com.homemate.Service;

import com.homemate.Dao.TaskerDao;
import com.homemate.Dto.FindTaskerCriteriaDto;
import com.homemate.Dto.TaskerCardDto;
import com.homemate.Dto.TaskerCardMapper;
import com.homemate.Model.Tasker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class TaskerDiscoveryService {

    private final TaskerDao taskerDao;
    private final TaskerCardMapper taskerCardMapper;

    @Autowired
    public TaskerDiscoveryService(TaskerDao taskerDao, TaskerCardMapper taskerCardMapper) {
        this.taskerDao = taskerDao;
        this.taskerCardMapper = taskerCardMapper;
    }

    public List<TaskerCardDto> getTaskerCardsByFilters(FindTaskerCriteriaDto filter, int page, int pageSize) {

        if (filter == null){
            throw new IllegalArgumentException("Filter cannot be null");
        }

        if (filter.getServiceID() == null) {
            throw new IllegalArgumentException("Service ID is required");
        }

        if (filter.getMinRating() != null && filter.getMaxRating() != null &&
                filter.getMinRating() > filter.getMaxRating()) {
            throw new IllegalArgumentException("minRating cannot be greater than maxRating");
        }

        if (filter.getMinHourRate() != null && filter.getMaxHourRate() != null &&
                filter.getMinHourRate() > filter.getMaxHourRate()) {
            throw new IllegalArgumentException("minHourRate cannot be greater than maxHourRate");
        }

        List<Tasker> taskers = taskerDao.findTaskersWithFilters(filter, page, pageSize);
        List<TaskerCardDto> dtos = new ArrayList<>();
        for (Tasker tasker : taskers) {
            dtos.add(taskerCardMapper.mapToDto(tasker));
        }
        return dtos;
    }
}
