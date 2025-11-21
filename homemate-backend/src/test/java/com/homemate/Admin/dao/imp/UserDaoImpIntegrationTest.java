package com.homemate.Admin.dao.imp;

import com.homemate.Admin.dao.UserDao;
import com.homemate.Admin.domain.entities.Tasker;
import com.homemate.Admin.domain.entities.User;
import com.homemate.Admin.domain.filters.Filter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Sql(scripts = "/test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Transactional
class UserDaoImpIntegrationTest {

    @Autowired
    private UserDao userDao;

    @Test
    void testFindUsers_WithNoFilters_ReturnsAllUsers() {

        Long limit = 10L;
        Long offset = 0L;
        List<User> users = userDao.findUsers(new ArrayList<>(), limit, offset);
        assertThat(users).isNotNull();
        assertThat(users.size()).isEqualTo(3);
    }


    @Test
    @DisplayName("FindUsers - Should respect pagination limit")
    void testFindUsers_WithLimit_ReturnsLimitedResults() {
        List<Filter> filters = new ArrayList<>();
        Long limit = 2L;
        Long offset = 0L;
        List<User> users = userDao.findUsers(filters, limit, offset);
        assertThat(users).isNotNull();
        assertThat(users.size()).isLessThanOrEqualTo(2);
    }

    @Test
    @DisplayName("FindUsers - Should respect pagination offset")
    void testFindUsers_WithOffset_SkipsFirstRecords() {
        List<Filter> filters = new ArrayList<>();
        Long limit = 2L;
        Long offset = 1L;
        List<User> userWithOffset = userDao.findUsers(filters, limit, offset);
        List<User> userWithoutOffset = userDao.findUsers(filters, limit, 0L);
        assertThat(userWithOffset).isNotNull();
        assertThat(userWithoutOffset).isNotNull();

        if (!userWithOffset.isEmpty() && userWithoutOffset.size() > 1) {
            assertThat(userWithOffset.get(0).getUserId())
                    .isNotEqualTo(userWithoutOffset.get(0).getUserId());
        }
    }

    @Test
    @DisplayName("CountUsers - Should return total count when no filters")
    void testCountUsers_WithNoFilters_ReturnsTotalCount() {

        List<Filter> filters = new ArrayList<>();
        Long count = userDao.countUsers(filters);
        assertThat(count).isNotNull();
        assertThat(count).isEqualTo(3L);
    }

}


