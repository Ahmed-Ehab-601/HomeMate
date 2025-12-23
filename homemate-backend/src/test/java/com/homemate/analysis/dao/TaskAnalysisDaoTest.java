package com.homemate.analysis.dao;

import com.homemate.analysis.dto.*;
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
    void fetchStatusCounts_aggregatesSixQueries() {
        TaskStatusCountsResponse response = dao.fetchStatusCounts();

        assertThat(response).isNotNull();
        assertThat(response.getInReview()).isEqualTo(1L);
        assertThat(response.getAccepted()).isEqualTo(1L);
        assertThat(response.getInProgress()).isEqualTo(1L);
        assertThat(response.getSuspended()).isEqualTo(1L);
        assertThat(response.getDone()).isEqualTo(5L);
        assertThat(response.getRejected()).isEqualTo(1L);
    }

    @Test
    void fetchStatusCounts_verifiesTotalCount() {
        TaskStatusCountsResponse response = dao.fetchStatusCounts();

        assertThat(response).isNotNull();
        long total = response.getInReview() + response.getAccepted() + 
                     response.getInProgress() + response.getSuspended() + 
                     response.getDone() + response.getRejected();
        
        assertThat(total).isEqualTo(10L);
    }

    @Test
    void fetchBillRanges_returnsCorrectDistribution() {
        BillRangesResponse response = dao.fetchBillRanges();

        assertThat(response).isNotNull();
        assertThat(response.getRanges()).isNotNull();
        Map<String, Long> ranges = response.getRanges();

        assertThat(ranges.get("30-40")).isEqualTo(1L);
        assertThat(ranges.get("50-60")).isEqualTo(1L);
        assertThat(ranges.get("60-70")).isEqualTo(1L);
        assertThat(ranges.get("90-100")).isEqualTo(1L);
        assertThat(ranges.get("110-120")).isEqualTo(1L);
    }

    @Test
    void fetchStartDateRanges_returnsCorrectCounts() {
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
    void fetchEndDateRanges_returnsCorrectCounts() {
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
        
        assertThat(response.getCounts().get(0)).isEqualTo(2L);
        assertThat(response.getCounts().get(1)).isEqualTo(3L);
    }

    @Test
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
    void fetchStartDateRanges_handlesEmptyList() {
        TaskDateRangesRequest request = new TaskDateRangesRequest(List.of());
        TaskDateRangesResponse response = dao.fetchStartDateRanges(request);

        assertThat(response).isNotNull();
        assertThat(response.getCounts()).isEmpty();
    }

    @Test
    void fetchEndDateRanges_handlesEmptyList() {
        TaskDateRangesRequest request = new TaskDateRangesRequest(List.of());
        TaskDateRangesResponse response = dao.fetchEndDateRanges(request);

        assertThat(response).isNotNull();
        assertThat(response.getCounts()).isEmpty();
    }

    @Test
    void fetchBillRanges_coversAllRanges() {
        BillRangesResponse response = dao.fetchBillRanges();

        assertThat(response).isNotNull();
        Map<String, Long> ranges = response.getRanges();

        assertThat(ranges).containsKeys("0-10", "10-20", "20-30", "30-40");
        assertThat(ranges).containsKey("200+");
    }

    @Test
    void fetchBillRanges_countsZeroBills() {
        BillRangesResponse response = dao.fetchBillRanges();

        assertThat(response).isNotNull();
        
        assertThat(response.getRanges().get("0-10")).isEqualTo(5L);
    }

    @Test
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
        assertThat(total).isEqualTo(10L);
    }

    @Test
    void fetchEndDateRanges_countsOnlyCompletedTasks() {
        TimeRange fullRange = new TimeRange(
                LocalDateTime.of(2024, 1, 1, 0, 0),
                LocalDateTime.of(2024, 12, 31, 23, 59)
        );

        TaskDateRangesRequest request = new TaskDateRangesRequest(List.of(fullRange));
        TaskDateRangesResponse response = dao.fetchEndDateRanges(request);

        assertThat(response).isNotNull();
        assertThat(response.getCounts()).hasSize(1);
        
        assertThat(response.getCounts().get(0)).isEqualTo(5L);
    }
}
