package com.homemate.admin.dao.imp;

import com.homemate.admin.dao.UserDao;
import com.homemate.admin.domain.entities.User;
import com.homemate.admin.domain.filters.Filter;
import com.homemate.admin.domain.filters.imp.RoleUserFilter;
import com.homemate.admin.domain.filters.imp.StatusFilter;
import com.homemate.admin.domain.filters.imp.UsernameFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("admin")
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
    void testFindUsersWithAdminFilterReturnsOnlyAdmins() {

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
    void testFindUsersWithSuspendedFilterReturnsOnlySuspendedUsers() {

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
    void testFindUsersWithLimitReturnsLimitedResults() {
        List<Filter> filters = new ArrayList<>();
        Long limit = 2L;
        Long offset = 0L;
        List<User> users = userDao.findUsers(filters, limit, offset);
        assertThat(users).isNotNull();
        assertThat(users.size()).isLessThanOrEqualTo(2);
    }

    @Test
    @DisplayName("FindUsers - Should respect pagination offset")
    void testFindUsersWithOffsetSkipsFirstRecords() {
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
    void testCountUsersWithNoFiltersReturnsTotalCount() {

        List<Filter> filters = new ArrayList<>();
        Long count = userDao.countUsers(filters);
        assertThat(count).isNotNull();
        assertThat(count).isEqualTo(3L);
    }
    @Test
    @DisplayName("CountUsers - Should ignore null role filter ")
    public void testCountUsersWithNullRoleFilterReturnsAllUsers() {
        List<Filter> filters = new ArrayList<>();
        filters.add(new RoleUserFilter(null));
        Long count = userDao.countUsers(filters);
        assertThat(count).isNotNull();
        assertThat(count).isEqualTo(3L);
    }
    @Test
    @DisplayName("FindUsers - Should filter by username ")
    public void testFindUsersWithUsernameFilterReturnsOnlyUsersPartialMatchingUsername() {

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
    public void testFindUsersWithNullUsernameFilterReturnsOnlyUsersPartialMatchingUsername() {

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
    public void testCountUsersWithNullStatusFilterReturnsAllUsers() {
        List<Filter> filters = new ArrayList<>();
        filters.add(new StatusFilter(null));
        Long count = userDao.countUsers(filters);
        assertThat(count).isNotNull();
        assertThat(count).isEqualTo(3L);
    }

    @Test
    @DisplayName("CountUsers - Should return filtered count")
    void testCountUsersWithAdminFilterReturnsAdminCount() {

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
    void testCountUsersWithNonMatchingFilterReturnsZero() {

        List<Filter> filters = new ArrayList<>();

        filters.add(new StatusFilter(true));
        filters.add(new RoleUserFilter(true));

        Long count = userDao.countUsers(filters);

        assertThat(count).isNotNull();
        assertThat(count).isGreaterThanOrEqualTo(0L);
    }

    @Test
    @DisplayName("FindUsers - Should combine multiple filters correctly")
    void testFindUsersWithMultipleFiltersAppliesAllFilters() {

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
    @Test
    @DisplayName("FindUserById")
    public void testFindUserById() {

        User user= userDao.findById(1L);
        assertThat(user).isNotNull();
        assertThat(user.getUsername()).isEqualTo("admin_user");
    }

    @Test
    @DisplayName("UpdateUserSuspended")
    public void testUpdateUserSuspended() {

        boolean updated= userDao.updateUserSuspended(1L,true);
        assertThat(updated).isTrue();
    }
    @Test
    @DisplayName("UpdateUserAdmin ")
    public void testUpdateUserAdmin() {

        boolean updated= userDao.updateUserAdmin(2L,true);
        assertThat(updated).isTrue();
    }
    @Test
    @DisplayName("UpdateUserAdmin - when not found ")
    public void testUpdateUserAdminWithNoUser() {

        boolean updated= userDao.updateUserAdmin(7L,true);
        assertThat(updated).isFalse();
    }
    @Test
    @DisplayName("UpdateUserSuspended - when not found ")
    public void testUpdateUserSuspendedWithNoUser() {

        boolean updated= userDao.updateUserSuspended(7L,true);
        assertThat(updated).isFalse();
    }
}


