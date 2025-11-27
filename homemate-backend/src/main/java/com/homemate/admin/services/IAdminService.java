package com.homemate.admin.services;

import com.homemate.admin.domain.dto.*;
import com.homemate.admin.domain.dto.*;

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