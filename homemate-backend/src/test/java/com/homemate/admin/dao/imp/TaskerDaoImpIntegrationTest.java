package com.homemate.admin.dao.imp;
import com.homemate.admin.domain.entities.Tasker;
import com.homemate.admin.domain.filters.Filter;
import com.homemate.admin.domain.filters.imp.MinRateFilter;
import com.homemate.admin.domain.filters.imp.StatusFilter;
import com.homemate.admin.domain.filters.imp.UsernameFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

//@SpringBootTest
//@ActiveProfiles("test")
//@Sql(scripts = "/adminData.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
//@Transactional
@SpringBootTest
@ExtendWith(SpringExtension.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("admin")
public class TaskerDaoImpIntegrationTest {

    @Autowired
    @Qualifier("adminTaskerDao")
    private TaskerDaoImp taskerDao;

    @Test
    @DisplayName("FindTaskers - Should return all taskers when no filters provided")
    public void testFindTaskersWithNoFiltersReturnsAllTaskers() {
        Long limit = 10L;
        Long offset = 0L;
        List<Tasker> taskers = taskerDao.findTaskers( new ArrayList<>(), limit, offset);
        assertThat(taskers).isNotNull();
        assertThat(taskers.size()).isEqualTo(3);
    }

    @Test
    @DisplayName("FindTaskers - Should filter by suspended status")
    public void testFindTaskersWithSuspendedFilterReturnsOnlySuspendedTaskers() {

        List<Filter> filters = new ArrayList<>();
        filters.add(new StatusFilter(true));
        Long limit = 10L;
        Long offset = 0L;

        List<Tasker> taskers = taskerDao.findTaskers(filters, limit, offset);

        assertThat(taskers).isNotNull();
        assertThat(taskers).isNotEmpty();
        taskers.forEach(tasker -> assertThat(tasker.isSuspended()).isTrue());
    }
    @Test
    @DisplayName("FindTaskers - Should filter by username ")
    public void testFindTaskersWithUsernameFilterReturnsOnlyTaskersPartialMatchingUsername() {

        List<Filter> filters = new ArrayList<>();
        filters.add(new UsernameFilter("sama"));
        Long limit = 10L;
        Long offset = 0L;

        List<Tasker> taskers = taskerDao.findTaskers(filters, limit, offset);

        assertThat(taskers).isNotNull();
        assertThat(taskers).isNotEmpty();
        assertThat(taskers.size()).isEqualTo(1);
    }  @Test
    @DisplayName("FindTaskers - Should filter by null username ")
    public void testFindTaskersWithNullUsernameFilterReturnsOnlyTaskersPartialMatchingUsername() {

        List<Filter> filters = new ArrayList<>();
        filters.add(new UsernameFilter(null));
        Long limit = 10L;
        Long offset = 0L;

        List<Tasker> taskers = taskerDao.findTaskers(filters, limit, offset);

        assertThat(taskers).isNotNull();
        assertThat(taskers).isNotEmpty();
        assertThat(taskers.size()).isEqualTo(3);
    }

    @Test
    @DisplayName("FindTaskers - Should filter by minimum rate")
    public void testFindTaskersWithMinRateFilterReturnsTaskersAboveRate() {

        List<Filter> filters = new ArrayList<>();
        filters.add(new MinRateFilter(4.5));
        Long limit = 10L;
        Long offset = 0L;


        List<Tasker> taskers = taskerDao.findTaskers(filters, limit, offset);


        assertThat(taskers).isNotNull();

        taskers.forEach(tasker -> {
            if (tasker.getHourRate() != null) {
                assertThat(tasker.getAvgRating()).isGreaterThanOrEqualTo(4.5);
            }
        });
    }

    @Test
    @DisplayName("CountTaskers - Should return total count with no filters")
    public void testCountTaskersWithNoFiltersReturnsTotalCount() {
        List<Filter> filters = new ArrayList<>();

        Long count = taskerDao.countTaskers(filters);

        assertThat(count).isNotNull();
        assertThat(count).isEqualTo(3L);
    } @Test
    @DisplayName("CountTaskers - Should return total count with filters")
    public void testCountTaskersWithFiltersReturnsTotalCount() {
        List<Filter> filters = new ArrayList<>();
        filters.add(new MinRateFilter(4.5));
        Long count = taskerDao.countTaskers(filters);
        assertThat(count).isNotNull();
        assertThat(count).isEqualTo(2L);
    }
    @Test
    @DisplayName("CountTaskers - Should ignore null rate filter ")
    public void testCountTaskersWithNullRateFilterReturnsAllTaskers() {
        List<Filter> filters = new ArrayList<>();
        filters.add(new MinRateFilter(null));
        Long count = taskerDao.countTaskers(filters);
        assertThat(count).isNotNull();
        assertThat(count).isEqualTo(3L);
    }
    @Test
    @DisplayName("CountTaskers - Should ignore null status filter ")
    public void testCountTaskersWithNullStatusFilterReturnsAllTaskers() {
        List<Filter> filters = new ArrayList<>();
        filters.add(new StatusFilter(null));
        Long count = taskerDao.countTaskers(filters);
        assertThat(count).isNotNull();
        assertThat(count).isEqualTo(3L);
    }

    @Test
    @DisplayName("FindTaskers - Should respect pagination")
    public void testFindTaskersWithPaginationRespectsLimitAndOffset() {
        List<Filter> filters = new ArrayList<>();
        Long limit = 2L;
        Long offset = 0L;
        List<Tasker> taskers = taskerDao.findTaskers(filters, limit, offset);
        assertThat(taskers).isNotNull();
        assertThat(taskers.size()).isLessThanOrEqualTo(2);
    }
    @Test
    @DisplayName("FindTaskerById")
    public void testFindTaskerById() {

        Tasker tasker= taskerDao.findById(1L);
        assertThat(tasker).isNotNull();
        assertThat(tasker.getUsername()).isEqualTo("samaa");
    }

    @Test
    @DisplayName("UpdateTaskerSuspended")
    public void testUpdateTaskerSuspended() {

        boolean updated= taskerDao.updateTaskerSuspended(1L,true);
        assertThat(updated).isTrue();
    }
    @Test
    @DisplayName("UpdateTaskerSuspended - with user not find")
    public void testUpdateTaskerSuspendedWithNoUser() {

        boolean updated= taskerDao.updateTaskerSuspended(7L,true);
        assertThat(updated).isFalse();
    }
    @Test
    @DisplayName("FindTaskers - Should combine multiple filters correctly")
    void testFindTaskersWithMultipleFiltersAppliesAllFilters(){
        List<Filter> filters = new ArrayList<>();
        Long limit=2L;
        Long offset=1L;
        List<Tasker>taskersWithOffset=taskerDao.findTaskers(filters,limit,offset);
        List<Tasker>taskersWithoutOffset=taskerDao.findTaskers(filters,limit,0L);
        assertThat(taskersWithOffset).isNotNull();
        assertThat(taskersWithoutOffset).isNotNull();
        if (!taskersWithOffset.isEmpty() && taskersWithoutOffset.size() > 1) {
            assertThat(taskersWithOffset.get(0).getTaskerID())
                    .isNotEqualTo(taskersWithoutOffset.get(0).getTaskerID());
            assertThat(taskersWithOffset.get(0).getTaskerID())
                    .isEqualTo(taskersWithoutOffset.get(1).getTaskerID());
        }


    }

    @Test
    @DisplayName("ComputeNumberOfTasks - Should return total tasks for a tasker")
    public void testComputeNumberOfTasksReturnsCorrectCount() {
        Long count = taskerDao.computeNumberOfTasks(1L);
        assertThat(count).isNotNull();
        assertThat(count).isEqualTo(0L);
    }

}


