package com.homemate.taskerdiscovery.service;

import com.homemate.taskerdiscovery.dao.TaskerDao;
import com.homemate.taskerdiscovery.dto.FindTaskerCriteriaDto;
import com.homemate.taskerdiscovery.dto.TaskerCardDto;
import com.homemate.taskerdiscovery.mapper.TaskerCardMapper;
import com.homemate.taskerdiscovery.model.Tasker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class TaskerDiscoveryService {

    @Qualifier("taskerDiscoveryDao")
    @Autowired
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

    public TaskerCardDto getTaskerByID(long taskID){
        Tasker tasker =taskerDao.findTaskerByID(taskID);
        return taskerCardMapper.mapToDto(tasker);
    }
}
