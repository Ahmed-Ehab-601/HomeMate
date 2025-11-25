package com.homemate.service;

import com.homemate.dao.TaskerDao;
import com.homemate.dto.FindTaskerCriteriaDto;
import com.homemate.dto.TaskerCardDto;
import com.homemate.mapper.TaskerCardMapper;
import com.homemate.model.Tasker;
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


        if (filter.getMinRating() != null && filter.getMaxRating() != null) {
            Double minRating = filter.getMinRating();
            Double maxRating = filter.getMaxRating();
            if (minRating > maxRating) {
                throw new IllegalArgumentException("minRating cannot be greater than maxRating");
            }
        }

        if (filter.getMinHourRate() != null && filter.getMaxHourRate() != null) {

            Double minHourRate = filter.getMinHourRate();
            Double maxHourRate = filter.getMaxHourRate();

            if (minHourRate > maxHourRate) {
                throw new IllegalArgumentException("minHourRate cannot be greater than maxHourRate");
            }
        }


        List<Tasker> taskers = taskerDao.findTaskersWithFilters(filter, page, pageSize);
        List<TaskerCardDto> dtos = new ArrayList<>();
        for (Tasker tasker : taskers) {
            dtos.add(taskerCardMapper.mapToDto(tasker));
        }
        return dtos;
    }
}
