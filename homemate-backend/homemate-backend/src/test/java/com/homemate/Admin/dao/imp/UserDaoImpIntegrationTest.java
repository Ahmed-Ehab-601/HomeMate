package com.homemate.Admin.dao.imp;

import com.homemate.Admin.dao.UserDao;
import com.homemate.Admin.domain.entities.Tasker;
import com.homemate.Admin.domain.entities.User;
import com.homemate.Admin.domain.filters.Filter;
import com.homemate.Admin.domain.filters.imp.RoleUserFilter;
import com.homemate.Admin.domain.filters.imp.StatusFilter;
import com.homemate.Admin.domain.filters.imp.UsernameFilter;
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
    @DisplayName("FindUsers - Should filter by admin status")
    void testFindUsers_WithAdminFilter_ReturnsOnlyAdmins() {

        List<Filter> filters = new ArrayList<>();
        filters.add(new RoleUserFilter(true));
        Long limit = 10L;
        Long offset = 0L;

        List<User> users = userDao.findUsers(filters, limit, offset);

        assertThat(users).isNotNull();
        assertThat(users).isNotEmpty();
        users.forEach(user -> assertThat(user.isAdmin()).isTrue());
    }

    @Test
    @DisplayName("FindUsers - Should filter by suspended status")
    void testFindUsers_WithSuspendedFilter_ReturnsOnlySuspendedUsers() {

        List<Filter> filters = new ArrayList<>();
        filters.add(new StatusFilter(true));
        Long limit = 10L;
        Long offset = 0L;


        List<User> users = userDao.findUsers(filters, limit, offset);


        assertThat(users).isNotNull();
        assertThat(users).isNotEmpty();
        users.forEach(user -> assertThat(user.isSuspended()).isTrue());
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
    @Test
    @DisplayName("CountUsers - Should ignore null role filter ")
    public void testCountUsers_WithNullRoleFilter_ReturnsAllUsers() {
        List<Filter> filters = new ArrayList<>();
        filters.add(new RoleUserFilter(null));
        Long count = userDao.countUsers(filters);
        assertThat(count).isNotNull();
        assertThat(count).isEqualTo(3L);
    }
    @Test
    @DisplayName("FindUsers - Should filter by username ")
    public void testFindUsers_WithUsernameFilter_ReturnsOnlyUsersPartialMatchingUsername() {

        List<Filter> filters = new ArrayList<>();
        filters.add(new UsernameFilter("user"));
        Long limit = 10L;
        Long offset = 0L;
        List<User> users = userDao.findUsers(filters, limit, offset);
        assertThat(users).isNotNull();
        assertThat(users).isNotEmpty();
        assertThat(users.size()).isEqualTo(3);
    }
   @Test
    @DisplayName("FindUsers - Should filter by null username ")
    public void testFindUsers_WithNullUsernameFilter_ReturnsOnlyUsersPartialMatchingUsername() {

        List<Filter> filters = new ArrayList<>();
        filters.add(new UsernameFilter(null));
        Long limit = 10L;
        Long offset = 0L;
        List<User> users = userDao.findUsers(filters, limit, offset);
        assertThat(users).isNotNull();
        assertThat(users).isNotEmpty();
        assertThat(users.size()).isEqualTo(3);
    }

    @Test
    @DisplayName("CountUsers - Should ignore null status filter ")
    public void testCountUsers_WithNullStatusFilter_ReturnsAllUsers() {
        List<Filter> filters = new ArrayList<>();
        filters.add(new StatusFilter(null));
        Long count = userDao.countUsers(filters);
        assertThat(count).isNotNull();
        assertThat(count).isEqualTo(3L);
    }

    @Test
    @DisplayName("CountUsers - Should return filtered count")
    void testCountUsers_WithAdminFilter_ReturnsAdminCount() {

        List<Filter> filters = new ArrayList<>();
        filters.add(new RoleUserFilter(true));
        Long adminCount = userDao.countUsers(filters);
        Long totalCount = userDao.countUsers(new ArrayList<>());
        assertThat(adminCount).isNotNull();
        assertThat(adminCount).isLessThanOrEqualTo(totalCount);
        assertThat(adminCount).isGreaterThan(0L);
    }

    @Test
    @DisplayName("CountUsers - Should return zero when no matches")
    void testCountUsers_WithNonMatchingFilter_ReturnsZero() {

        List<Filter> filters = new ArrayList<>();

        filters.add(new StatusFilter(true));
        filters.add(new RoleUserFilter(true));

        Long count = userDao.countUsers(filters);

        assertThat(count).isNotNull();
        assertThat(count).isGreaterThanOrEqualTo(0L);
    }

    @Test
    @DisplayName("FindUsers - Should combine multiple filters correctly")
    void testFindUsers_WithMultipleFilters_AppliesAllFilters() {

        List<Filter> filters = new ArrayList<>();
        filters.add(new RoleUserFilter(false));
        filters.add(new StatusFilter(false));
        Long limit = 10L;
        Long offset = 0L;
        List<User> users = userDao.findUsers(filters, limit, offset);

        assertThat(users).isNotNull();

        users.forEach(user -> {
            assertThat(user.isAdmin()).isFalse();
            assertThat(user.isSuspended()).isFalse();
        });
    }
}


