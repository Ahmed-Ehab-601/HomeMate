package com.homemate.Admin.dao;

import com.homemate.Admin.domain.entities.User;
import com.homemate.Admin.domain.filters.Filter;

import java.util.List;

public interface UserDao {
    List<User> findUsers(List<Filter> filters, Long limit , Long offest);
    Long countUsers(List<Filter> filters);
}
