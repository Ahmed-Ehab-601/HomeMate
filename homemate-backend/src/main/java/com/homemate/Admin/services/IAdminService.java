package com.homemate.Admin.services;

import com.homemate.Admin.domain.dto.*;
import com.homemate.Admin.domain.filters.Filter;

import java.util.List;

public interface IAdminService {


    PageResponse<UserDto> getUsers(UserFilterDto filterDTO, PageRequest pageRequest);

    UserDto suspendUser(SuspendDto suspendDto);
    TaskerDto suspendTasker(SuspendDto suspendDto);

    UserDto reactiveUser(Long userID);
    TaskerDto reactiveTasker(Long userID);

    UserDto promoteUser(Long userId);
    UserDto demoteUser(Long userId);


    PageResponse<TaskerDto> getTaskers(TaskerFilterDto filterDTO, PageRequest pageRequest);

}