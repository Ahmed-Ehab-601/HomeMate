package com.homemate;

import com.homemate.dao.TaskerDao;
import com.homemate.dto.FindTaskerCriteriaDto;
import com.homemate.model.Tasker;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
@ExtendWith(SpringExtension.class)
@ActiveProfiles("tasker")
public class TaskerDaoIntegrationTest {

    @Autowired
    private TaskerDao taskerDao;

    @Test
    void findTaskers_noFilters_returnsAll() {
        FindTaskerCriteriaDto criteria = new FindTaskerCriteriaDto();
        List<Tasker> taskers = taskerDao.findTaskersWithFilters(criteria, 1, 20);

        assertThat(taskers).isNotNull();
        assertThat(taskers.size()).isGreaterThan(0);
    }



    @Test
    void findTaskers_filterByAvailability_returnsCorrectTaskers() {
        FindTaskerCriteriaDto criteria = new FindTaskerCriteriaDto();
        criteria.setAvailability("available");

        List<Tasker> taskers = taskerDao.findTaskersWithFilters(criteria, 1, 10);

        assertThat(taskers).isNotNull();
        assertThat(taskers).allMatch(t -> t.getAvailability().equalsIgnoreCase("available"));
    }


    @Test
    void findTaskers_filterByRatingAndHourRate_returnsCorrectTaskers() {

        FindTaskerCriteriaDto criteria = new FindTaskerCriteriaDto();
        criteria.setMinRating(4.7);
        criteria.setMaxRating(4.9);
        criteria.setMinHourRate(30.0);
        criteria.setMaxHourRate(50.0);

        List<Tasker> taskers = taskerDao.findTaskersWithFilters(criteria, 1, 10);

        assertThat(taskers).isNotNull();

        for (Tasker tasker : taskers) {

            double rating = tasker.getRating();
            double hourRate = tasker.getHourRate();

            assertThat(rating).isGreaterThanOrEqualTo(4.7);
            assertThat(rating).isLessThanOrEqualTo(4.9);
            assertThat(hourRate).isGreaterThanOrEqualTo(30.0);
            assertThat(hourRate).isLessThanOrEqualTo(50.0);
        }
    }


    @Test
    void findTaskers_sortByRatingDescending_returnsSorted() {
        FindTaskerCriteriaDto criteria = new FindTaskerCriteriaDto();
        criteria.setSortBy("rating");
        criteria.setSortOrder("DESC");

        List<Tasker> taskers = taskerDao.findTaskersWithFilters(criteria, 1, 10);

        assertThat(taskers).isNotNull();
        for (int i = 1; i < taskers.size(); i++) {
            assertThat(taskers.get(i - 1).getRating()).isGreaterThanOrEqualTo(taskers.get(i).getRating());

        }
    }

    @Test
    void findTaskers_pagination_worksCorrectly() {
        FindTaskerCriteriaDto criteria = new FindTaskerCriteriaDto();
        List<Tasker> page1 = taskerDao.findTaskersWithFilters(criteria, 1, 5);
        List<Tasker> page2 = taskerDao.findTaskersWithFilters(criteria, 2, 5);

        assertThat(page1).isNotNull();
        assertThat(page2).isNotNull();
        assertThat(page1).doesNotContainAnyElementsOf(page2); // pages should not overlap
    }

    @Test
    void findTaskers_pagination_returnsCorrectPages() {
        FindTaskerCriteriaDto criteria = new FindTaskerCriteriaDto();
        criteria.setServiceID(2);

        int pageSize = 3;
        List<Tasker> page1 = taskerDao.findTaskersWithFilters(criteria, 1, pageSize);
        assertThat(page1).isNotNull();
        assertThat(page1.size()).isLessThanOrEqualTo(pageSize);

        List<Tasker> page2 = taskerDao.findTaskersWithFilters(criteria, 2, pageSize);
        assertThat(page2).isNotNull();
        assertThat(page2.size()).isLessThanOrEqualTo(pageSize);

        List<Tasker> page3 = taskerDao.findTaskersWithFilters(criteria, 3, pageSize);
        assertThat(page3).isNotNull();
        assertThat(page3.size()).isLessThanOrEqualTo(pageSize);


        for (Tasker t : page2) {
            assertThat(page1).doesNotContain(t);
        }
        for (Tasker t : page3) {
            assertThat(page1).doesNotContain(t);
            assertThat(page2).doesNotContain(t);
        }

        int totalFetched = page1.size() + page2.size() + page3.size();
        List<Tasker> all = taskerDao.findTaskersWithFilters(criteria, 1, 100); // fetch all
        assertThat(totalFetched).isEqualTo(all.size());
    }

}
