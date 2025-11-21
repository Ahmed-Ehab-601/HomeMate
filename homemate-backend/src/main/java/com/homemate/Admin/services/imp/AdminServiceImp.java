package com.homemate.Admin.services.imp;

import com.homemate.Admin.dao.TaskerDao;
import com.homemate.Admin.dao.UserDao;
import com.homemate.Admin.domain.dto.*;
import com.homemate.Admin.domain.entities.Tasker;
import com.homemate.Admin.domain.entities.User;
import com.homemate.Admin.domain.filters.Filter;
import com.homemate.Admin.mappers.imp.TaskerMapper;
import com.homemate.Admin.mappers.imp.UserMapper;
import com.homemate.Admin.services.IAdminService;
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

//    @Override
//    public Boolean suspendUser(SuspendDto suspendDto) {
//        return null;
//    }

    @Override
    public Boolean reactiveUser(Long userId, String userType) {
        return null;
    }

    @Override
    public Boolean promoteUser(Long userId) {
        return null;
    }
    @Override
    public Boolean demoteUser(Long userId) {
        return null;
    }




    private List<Filter> buildTaskerFilters(TaskerFilterDto taskerFilterDto) {
        List<Filter> filters = new ArrayList<>();


        return filters;
    }
    private List<Filter> buildUserFilters(UserFilterDto userFilterDto) {
        List<Filter> filters = new ArrayList<>();

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

        Long totalPages = (totalElements + pageRequest.getSize() - 1) / pageRequest.getSize();

        PageResponse<TaskerDto> res = PageResponse.<TaskerDto>builder()
                .items(taskerDtos)
                .totalPages(totalPages)
                .totalItems(totalElements)
                .build();

        return res;
    }


}
