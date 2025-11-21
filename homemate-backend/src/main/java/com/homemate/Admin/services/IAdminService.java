package com.homemate.Admin.services;

import com.homemate.Admin.domain.dto.*;
import com.homemate.Admin.domain.filters.Filter;

import java.util.List;

public interface IAdminService {

    // User Management
    PageResponse<UserDto> getUsers(UserFilterDto filterDTO, PageRequest pageRequest);

//    Boolean suspendUser(SuspendDto suspendDto);

    Boolean reactiveUser(Long userId, String userType);

    Boolean promoteUser(Long userId);
    public Boolean demoteUser(Long userId);


    PageResponse<TaskerDto> getTaskers(TaskerFilterDto filterDTO, PageRequest pageRequest);

}