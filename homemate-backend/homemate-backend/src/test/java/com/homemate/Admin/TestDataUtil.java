package com.homemate.Admin;

import com.homemate.Admin.domain.dto.PageResponse;
import com.homemate.Admin.domain.dto.TaskerDto;
import com.homemate.Admin.domain.dto.UserDto;
import com.homemate.Admin.domain.entities.Tasker;
import com.homemate.Admin.domain.entities.User;

import java.util.ArrayList;
import java.util.List;


public class TestDataUtil {
public static List<User> createMockUsers(int count) {
    List<User> users = new ArrayList<>();
    for (int i = 1; i <= count; i++) {
        User user = new User();
        user.setUserId((long) i);
        user.setUsername("user" + i);
        user.setEmail("user" + i + "@test.com");
        user.setAdmin(i % 2 == 0);
        user.setSuspended(false);
        users.add(user);
    }
    return users;
}

        public static  List<UserDto> createMockUserDtos(int count) {
            List<UserDto> dtos = new ArrayList<>();
            for (int i = 1; i <= count; i++) {
                UserDto dto = UserDto.builder()
                        .userId((long) i)
                        .username("user" + i)
                        .email("user" + i + "@test.com")
                        .admin(i % 2 == 0)
                        .suspended(false)
                        .build();
                dtos.add(dto);
            }
            return dtos;
        }

        public static List<Tasker> createMockTaskers(int count) {
            List<Tasker> taskers = new ArrayList<>();
            for (int i = 1; i <= count; i++) {
                Tasker tasker = new Tasker();
                tasker.setTaskerID((long) i);
                tasker.setUsername("tasker" + i);
                tasker.setEmail("tasker" + i + "@test.com");
                tasker.setSuspended(false);
                tasker.setHourRate(20.0 + i);
                taskers.add(tasker);
            }
            return taskers;
        }

        public static List<TaskerDto> createMockTaskerDtos(int count) {
            List<TaskerDto> dtos = new ArrayList<>();
            for (int i = 1; i <= count; i++) {
                TaskerDto dto = TaskerDto.builder()
                        .taskerID((long) i)
                        .username("tasker" + i)
                        .email("tasker" + i + "@test.com")
                        .suspended(false)
                        .hourRate(20.0 + i)
                        .build();
                dtos.add(dto);
            }
            return dtos;
        }
    public static PageResponse<UserDto> createMockUserPageResponse() {
        List<UserDto> users = new ArrayList<>();
        users.add(UserDto.builder()
                .userId(1L)
                .username("user1")
                .email("user1@test.com")
                .admin(false)
                .suspended(false)
                .build());
        users.add(UserDto.builder()
                .userId(2L)
                .username("user2")
                .email("user2@test.com")
                .admin(true)
                .suspended(false)
                .build());

        return PageResponse.<UserDto>builder()
                .items(users)
                .totalItems(2L)
                .totalPages(1L)
                .build();
    }

    public  static  PageResponse<TaskerDto> createMockTaskerPageResponse() {
        List<TaskerDto> taskers = new ArrayList<>();
        taskers.add(TaskerDto.builder()
                .taskerID(1L)
                .username("tasker1")
                .email("tasker1@test.com")
                .gender(TaskerDto.Gender.F)
                .hourRate(25.50)
                .build());
        taskers.add(TaskerDto.builder()
                .taskerID(2L)
                .username("tasker2")
                .email("tasker2@test.com")
                .gender(TaskerDto.Gender.M)
                .hourRate(30.00)
                .build());

        return PageResponse.<TaskerDto>builder()
                .items(taskers)
                .totalItems(2L)
                .totalPages(1L)
                .build();
    }
}
