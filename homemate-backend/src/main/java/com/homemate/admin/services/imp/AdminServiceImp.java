package com.homemate.admin.services.imp;

import com.homemate.admin.dao.TaskerDao;
import com.homemate.admin.dao.UserDao;
import com.homemate.admin.domain.dto.*;
import com.homemate.admin.domain.dto.*;
import com.homemate.admin.domain.entities.Tasker;
import com.homemate.admin.domain.entities.User;
import com.homemate.admin.domain.filters.Filter;
import com.homemate.admin.domain.filters.imp.MinRateFilter;
import com.homemate.admin.domain.filters.imp.RoleUserFilter;
import com.homemate.admin.domain.filters.imp.StatusFilter;
import com.homemate.admin.domain.filters.imp.UsernameFilter;
import com.homemate.admin.mappers.imp.TaskerMapper;
import com.homemate.admin.mappers.imp.UserMapper;
import com.homemate.admin.services.IAdminService;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class AdminServiceImp implements IAdminService {
    private TaskerDao taskerDao;
    private UserDao userDao;
    private UserMapper userMapper;
    private TaskerMapper taskerMapper;

    public AdminServiceImp(TaskerDao taskerDao, UserDao userDao, UserMapper userMapper , TaskerMapper taskerMapper) {
        this.taskerDao = taskerDao;
        this.userDao = userDao;
        this.taskerMapper=taskerMapper;
        this.userMapper=userMapper;

    }

    @Override
    public PageResponse<UserDto> getUsers(UserFilterDto filterDTO, PageRequest pageRequest) {
        List<Filter> filters = buildUserFilters(filterDTO);

        Long limit = pageRequest.getSize();
        Long offset = pageRequest.getPage() * pageRequest.getSize();

        List<User> users = userDao.findUsers(filters, limit, offset);
        Long totalElements = userDao.countUsers(filters);

        List<UserDto> userDtos = users.stream()
                .map(userMapper::mapTO)
                .toList();

        Long totalPages = (totalElements + pageRequest.getSize() - 1) / pageRequest.getSize();

        PageResponse<UserDto> res = PageResponse.<UserDto>builder()
                .items(userDtos)
                .totalPages(totalPages)
                .totalItems(totalElements)
                .build();

        return res;
    }

    @Override
    public UserDto suspendUser(SuspendDto suspendDto) {
        if (suspendDto == null || suspendDto.getUserId() == null) return null;
        Long id = suspendDto.getUserId();
        User user = userDao.findById(id);
        if (user == null) return null;
        boolean updated = userDao.updateUserSuspended(id, true);
        if (!updated) return null;
        User refreshed = userDao.findById(id);
        return refreshed != null ? userMapper.mapTO(refreshed) : null;
    }

    @Override
    public TaskerDto suspendTasker(SuspendDto suspendDto) {
        if (suspendDto == null || suspendDto.getUserId() == null) return null;
        Long id = suspendDto.getUserId();
        Tasker tasker = taskerDao.findById(id);
        if (tasker == null) return null;
        boolean updated = taskerDao.updateTaskerSuspended(id, true);
        if (!updated) return null;
        Tasker refreshed = taskerDao.findById(id);
        return refreshed != null ? taskerMapper.mapTO(refreshed) : null;
    }

    @Override
    public UserDto reactiveUser(Long userID) {
        if (userID == null) return null;
        User user = userDao.findById(userID);
        if (user == null) return null;
        boolean updated = userDao.updateUserSuspended(userID, false);
        if (!updated) return null;
        User refreshed = userDao.findById(userID);
        return refreshed != null ? userMapper.mapTO(refreshed) : null;
    }

    @Override
    public TaskerDto reactiveTasker(Long userID) {
        if (userID == null) return null;
        Tasker tasker = taskerDao.findById(userID);
        if (tasker == null) return null;
        boolean updated = taskerDao.updateTaskerSuspended(userID, false);
        if (!updated) return null;
        Tasker refreshed = taskerDao.findById(userID);
        return refreshed != null ? taskerMapper.mapTO(refreshed) : null;
    }

    @Override
    public UserDto promoteUser(Long userId) {
        if (userId == null) return null;
        User user = userDao.findById(userId);
        if (user == null) return null;
        boolean updated = userDao.updateUserAdmin(userId, true);
        if (!updated) return null;
        User refreshed = userDao.findById(userId);
        return refreshed != null ? userMapper.mapTO(refreshed) : null;
    }

    @Override
    public UserDto demoteUser(Long userId) {
        if (userId == null) return null;
        User user = userDao.findById(userId);
        if (user == null) return null;
        boolean updated = userDao.updateUserAdmin(userId, false);
        if (!updated) return null;
        User refreshed = userDao.findById(userId);
        return refreshed != null ? userMapper.mapTO(refreshed) : null;
    }




    private List<Filter> buildTaskerFilters(TaskerFilterDto taskerFilterDto) {
        List<Filter> filters = new ArrayList<>();

        if (taskerFilterDto.getSuspended() != null) {
            filters.add(new StatusFilter(taskerFilterDto.getSuspended()));
        }

        if (taskerFilterDto.getMinRate() != null) {
            filters.add(new MinRateFilter(taskerFilterDto.getMinRate()));
        }
        if (taskerFilterDto.getUsername() != null) {
            filters.add(new UsernameFilter(taskerFilterDto.getUsername()));
        }

        return filters;
    }
    private List<Filter> buildUserFilters(UserFilterDto userFilterDto) {
        List<Filter> filters = new ArrayList<>();

        if (userFilterDto.getAdmin() != null) {
            filters.add(new RoleUserFilter(userFilterDto.getAdmin()));
        }

        if (userFilterDto.getSuspended() != null) {
            filters.add(new StatusFilter(userFilterDto.getSuspended()));
        }
        if (userFilterDto.getUsername() != null) {
            filters.add(new UsernameFilter(userFilterDto.getUsername()));
        }

        return filters;
    }

    @Override
    public PageResponse<TaskerDto> getTaskers(TaskerFilterDto filterDTO, PageRequest pageRequest) {

        List<Filter> filters = buildTaskerFilters(filterDTO);
        Long limit = pageRequest.getSize();
        Long offset = pageRequest.getPage() * pageRequest.getSize();

        List<Tasker> taskers = taskerDao.findTaskers(filters, limit, offset);
        Long totalElements = taskerDao.countTaskers(filters);

        List<TaskerDto> taskerDtos = taskers.stream()
                .map(taskerMapper::mapTO)
                .toList();
        for (TaskerDto taskerDto : taskerDtos) {
            Long numberOfTasks = taskerDao.computeNumberOfTasks(taskerDto.getTaskerID());
            taskerDto.setNumberOfTasks(numberOfTasks);
        }

        Long totalPages = (totalElements + pageRequest.getSize() - 1) / pageRequest.getSize();

        PageResponse<TaskerDto> res = PageResponse.<TaskerDto>builder()
                .items(taskerDtos)
                .totalPages(totalPages)
                .totalItems(totalElements)
                .build();

        return res;
    }


}
