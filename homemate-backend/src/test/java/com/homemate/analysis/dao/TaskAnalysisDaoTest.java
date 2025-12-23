package com.homemate.analysis.dao;

import com.homemate.analysis.dto.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("analysis")
class TaskAnalysisDaoTest {

    @Autowired
    private TaskAnalysisDao dao;

    @Test
    @DisplayName("Should fetch status counts with all six statuses")
    void fetchStatusCounts_aggregatesSixQueries() {
        TaskStatusCountsResponse response = dao.fetchStatusCounts();

        assertThat(response).isNotNull();
        
        // Based on test data:
        // InReview: 1 (task 5)
        // Accepted: 1 (task 4)
        // InProgress: 1 (task 3)
        // Suspended: 1 (task 9)
        // Done: 5 (tasks 1, 2, 6, 8, 10)
        // Rejected: 1 (task 7)
        assertThat(response.getInReview()).isEqualTo(1L);
        assertThat(response.getAccepted()).isEqualTo(1L);
        assertThat(response.getInProgress()).isEqualTo(1L);
        assertThat(response.getSuspended()).isEqualTo(1L);
        assertThat(response.getDone()).isEqualTo(5L);
        assertThat(response.getRejected()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Should verify total tasks count through status")
    void fetchStatusCounts_verifiesTotalCount() {
        TaskStatusCountsResponse response = dao.fetchStatusCounts();

        assertThat(response).isNotNull();
        long total = response.getInReview() + response.getAccepted() + 
                     response.getInProgress() + response.getSuspended() + 
                     response.getDone() + response.getRejected();
        
        assertThat(total).isEqualTo(10L); // Total tasks in test data
    }

    @Test
    @DisplayName("Should fetch bill ranges correctly")
    void fetchBillRanges_returnsCorrectDistribution() {
        BillRangesResponse response = dao.fetchBillRanges();

        assertThat(response).isNotNull();
        assertThat(response.getRanges()).isNotNull();
        Map<String, Long> ranges = response.getRanges();

        // Bills in test data: 63.75, 90.00, 0, 0, 0, 112.00, 0, 51.00, 0, 38.25
        assertThat(ranges.get("30-40")).isEqualTo(1L); // 38.25
        assertThat(ranges.get("50-60")).isEqualTo(1L); // 51.00
        assertThat(ranges.get("60-70")).isEqualTo(1L); // 63.75
        assertThat(ranges.get("90-100")).isEqualTo(1L); // 90.00
        assertThat(ranges.get("110-120")).isEqualTo(1L); // 112.00
    }

    @Test
    @DisplayName("Should fetch start date ranges correctly")
    void fetchStartDateRanges_returnsCorrectCounts() {
        // All tasks start in January 2024
        TimeRange range1 = new TimeRange(
                LocalDateTime.of(2024, 1, 1, 0, 0),
                LocalDateTime.of(2024, 1, 6, 0, 0)
        );
        TimeRange range2 = new TimeRange(
                LocalDateTime.of(2024, 1, 6, 0, 0),
                LocalDateTime.of(2024, 1, 11, 0, 0)
        );

        TaskDateRangesRequest request = new TaskDateRangesRequest(List.of(range1, range2));
        TaskDateRangesResponse response = dao.fetchStartDateRanges(request);

        assertThat(response).isNotNull();
        assertThat(response.getCounts()).hasSize(2);
        assertThat(response.getCounts().get(0)).isEqualTo(5L); // tasks 1-5
        assertThat(response.getCounts().get(1)).isEqualTo(5L); // tasks 6-10
    }

    @Test
    @DisplayName("Should fetch end date ranges correctly")
    void fetchEndDateRanges_returnsCorrectCounts() {
        // Only completed tasks have end dates
        TimeRange range1 = new TimeRange(
                LocalDateTime.of(2024, 1, 1, 0, 0),
                LocalDateTime.of(2024, 1, 5, 0, 0)
        );
        TimeRange range2 = new TimeRange(
                LocalDateTime.of(2024, 1, 5, 0, 0),
                LocalDateTime.of(2024, 1, 11, 0, 0)
        );

        TaskDateRangesRequest request = new TaskDateRangesRequest(List.of(range1, range2));
        TaskDateRangesResponse response = dao.fetchEndDateRanges(request);

        assertThat(response).isNotNull();
        assertThat(response.getCounts()).hasSize(2);
        
        // Tasks with endDate: 1, 2, 6, 8, 10
        assertThat(response.getCounts().get(0)).isEqualTo(2L); // tasks 1, 2
        assertThat(response.getCounts().get(1)).isEqualTo(3L); // tasks 6, 8, 10
    }

    @Test
    @DisplayName("Should return zero for time ranges with no tasks")
    void fetchStartDateRanges_returnsZeroForEmptyRanges() {
        TimeRange emptyRange = new TimeRange(
                LocalDateTime.of(2020, 1, 1, 0, 0),
                LocalDateTime.of(2020, 12, 31, 23, 59)
        );

        TaskDateRangesRequest request = new TaskDateRangesRequest(List.of(emptyRange));
        TaskDateRangesResponse response = dao.fetchStartDateRanges(request);

        assertThat(response).isNotNull();
        assertThat(response.getCounts()).hasSize(1);
        assertThat(response.getCounts().get(0)).isEqualTo(0L);
    }

    @Test
    @DisplayName("Should handle empty time range list for start dates")
    void fetchStartDateRanges_handlesEmptyList() {
        TaskDateRangesRequest request = new TaskDateRangesRequest(List.of());
        TaskDateRangesResponse response = dao.fetchStartDateRanges(request);

        assertThat(response).isNotNull();
        assertThat(response.getCounts()).isEmpty();
    }

    @Test
    @DisplayName("Should handle empty time range list for end dates")
    void fetchEndDateRanges_handlesEmptyList() {
        TaskDateRangesRequest request = new TaskDateRangesRequest(List.of());
        TaskDateRangesResponse response = dao.fetchEndDateRanges(request);

        assertThat(response).isNotNull();
        assertThat(response.getCounts()).isEmpty();
    }

    @Test
    @DisplayName("Should verify bill ranges cover all expected ranges")
    void fetchBillRanges_coversAllRanges() {
        BillRangesResponse response = dao.fetchBillRanges();

        assertThat(response).isNotNull();
        Map<String, Long> ranges = response.getRanges();

        // Verify some key ranges exist
        assertThat(ranges).containsKeys("0-10", "10-20", "20-30", "30-40");
        assertThat(ranges).containsKey("200+");
    }

    @Test
    @DisplayName("Should count tasks with zero bill correctly")
    void fetchBillRanges_countsZeroBills() {
        BillRangesResponse response = dao.fetchBillRanges();

        assertThat(response).isNotNull();
        
        // Tasks with 0 bill: 3, 4, 5, 7, 9 (5 tasks)
        assertThat(response.getRanges().get("0-10")).isEqualTo(5L);
    }

    @Test
    @DisplayName("Should handle multiple time ranges for start dates")
    void fetchStartDateRanges_handlesMultipleRanges() {
        List<TimeRange> ranges = List.of(
                new TimeRange(LocalDateTime.of(2024, 1, 1, 0, 0), LocalDateTime.of(2024, 1, 3, 0, 0)),
                new TimeRange(LocalDateTime.of(2024, 1, 3, 0, 0), LocalDateTime.of(2024, 1, 6, 0, 0)),
                new TimeRange(LocalDateTime.of(2024, 1, 6, 0, 0), LocalDateTime.of(2024, 1, 11, 0, 0))
        );

        TaskDateRangesRequest request = new TaskDateRangesRequest(ranges);
        TaskDateRangesResponse response = dao.fetchStartDateRanges(request);

        assertThat(response).isNotNull();
        assertThat(response.getCounts()).hasSize(3);
        
        long total = response.getCounts().stream().mapToLong(Long::longValue).sum();
        assertThat(total).isEqualTo(10L); // All tasks
    }

    @Test
    @DisplayName("Should count only done tasks for end dates")
    void fetchEndDateRanges_countsOnlyCompletedTasks() {
        TimeRange fullRange = new TimeRange(
                LocalDateTime.of(2024, 1, 1, 0, 0),
                LocalDateTime.of(2024, 12, 31, 23, 59)
        );

        TaskDateRangesRequest request = new TaskDateRangesRequest(List.of(fullRange));
        TaskDateRangesResponse response = dao.fetchEndDateRanges(request);

        assertThat(response).isNotNull();
        assertThat(response.getCounts()).hasSize(1);
        
        // Only 5 tasks are done with endDate
        assertThat(response.getCounts().get(0)).isEqualTo(5L);
    }
}
