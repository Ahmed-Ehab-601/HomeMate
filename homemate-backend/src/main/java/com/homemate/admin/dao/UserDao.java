package com.homemate.admin.dao;

import com.homemate.admin.domain.entities.User;
import com.homemate.admin.domain.filters.Filter;

import java.util.List;

public interface UserDao {
    List<User> findUsers(List<Filter> filters, Long limit , Long offest);
    Long countUsers(List<Filter> filters);
    User findById(Long userId);
    boolean updateUserSuspended(Long userId, boolean suspended);
    boolean updateUserAdmin(Long userId, boolean isAdmin);
}
