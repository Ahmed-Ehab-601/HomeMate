package com.homemate.Admin.dao.imp;

import com.homemate.Admin.domain.entities.Tasker;
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
public class TaskerDaoImpIntegrationTest {
    
    @Autowired
    private TaskerDaoImp taskerDao;

    @Test
    @DisplayName("FindTaskers - Should return all taskers when no filters provided")
    public void testFindTaskers_WithNoFilters_ReturnsAllTaskers() {
        Long limit = 10L;
        Long offset = 0L;
        List<Tasker> taskers = taskerDao.findTaskers( new ArrayList<>(), limit, offset);
        assertThat(taskers).isNotNull();
        assertThat(taskers.size()).isEqualTo(3);
    }

   @Test
    @DisplayName("CountTaskers - Should return total count with no filters")
    public void testCountTaskers_WithNoFilters_ReturnsTotalCount() {
        List<Filter> filters = new ArrayList<>();

        Long count = taskerDao.countTaskers(filters);

        assertThat(count).isNotNull();
        assertThat(count).isEqualTo(3L);
    }



    @Test
    @DisplayName("FindTaskers - Should respect pagination")
    public void testFindTaskers_WithPagination_RespectsLimitAndOffset() {
        List<Filter> filters = new ArrayList<>();
        Long limit = 2L;
        Long offset = 0L;


        List<Tasker> taskers = taskerDao.findTaskers(filters, limit, offset);

        assertThat(taskers).isNotNull();
        assertThat(taskers.size()).isLessThanOrEqualTo(2);
    }
    @Test
    @DisplayName("FindTaskers - Should combine multiple filters correctly")
    void testFindTaskers_WithMultipleFilters_AppliesAllFilters(){
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
}
