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
class TaskerAnalysisDaoTest {

    @Autowired
    private TaskerAnalysisDao dao;

    @Test
    @DisplayName("Should fetch gender counts correctly")
    void fetchGenderCounts_returnsCorrectCounts() {
        GenderCountResponse response = dao.fetchGenderCounts();

        assertThat(response).isNotNull();
        assertThat(response.getMale()).isEqualTo(3L);
        assertThat(response.getFemale()).isEqualTo(2L);
    }

    @Test
    @DisplayName("Should fetch status counts correctly")
    void fetchStatusCounts_aggregatesQueryResults() {
        TaskerStatusCountsResponse response = dao.fetchStatusCounts();

        assertThat(response).isNotNull();
        assertThat(response.getSuspended()).isEqualTo(1L);
        assertThat(response.getActive()).isEqualTo(4L);
    }

    @Test
    @DisplayName("Should fetch age buckets correctly")
    void fetchAgeBuckets_returnsCorrectDistribution() {
        AgeBucketsResponse response = dao.fetchAgeBuckets();

        assertThat(response).isNotNull();
        assertThat(response.getBuckets()).isNotNull();
        Map<String, Long> buckets = response.getBuckets();

        assertThat(buckets).containsKeys("0-10", "10-20", "20-30", "30-40", "40-50", "50-60", "60+");
        
        Long total = buckets.values().stream().mapToLong(Long::longValue).sum();
        assertThat(total).isEqualTo(5L);
    }

    @Test
    @DisplayName("Should fetch rating ranges correctly")
    void fetchRatingRanges_returnsCorrectDistribution() {
        RatingRangesResponse response = dao.fetchRatingRanges();

        assertThat(response).isNotNull();
        assertThat(response.getRanges()).isNotNull();
        Map<String, Long> ranges = response.getRanges();

        assertThat(ranges).containsKeys("0-1", "1-2", "2-3", "3-4", "4-5");
        
        assertThat(ranges.get("3-4")).isEqualTo(1L);
        assertThat(ranges.get("4-5")).isEqualTo(4L);
    }

    @Test
    @DisplayName("Should fetch hour rate ranges correctly")
    void fetchHourRateRanges_returnsCorrectDistribution() {
        HourRateRangesResponse response = dao.fetchHourRateRanges();

        assertThat(response).isNotNull();
        assertThat(response.getRanges()).isNotNull();
        Map<String, Long> ranges = response.getRanges();

        assertThat(ranges).isNotEmpty();
        
        assertThat(ranges.get("20-30")).isEqualTo(4L);
        assertThat(ranges.get("30-40")).isEqualTo(1L);
    }

    @Test
    @DisplayName("Should fetch worked hours ranges correctly")
    void fetchWorkedHoursRanges_returnsCorrectDistribution() {
        WorkedHoursRangesResponse response = dao.fetchWorkedHoursRanges();

        assertThat(response).isNotNull();
        assertThat(response.getRanges()).isNotNull();
        Map<String, Long> ranges = response.getRanges();

        assertThat(ranges).isNotEmpty();
        
        assertThat(ranges.get("10-20")).isEqualTo(2L);
        assertThat(ranges.get("20-30")).isEqualTo(2L);
        assertThat(ranges.get("30-40")).isEqualTo(1L);
    }

    @Test
    @DisplayName("Should fetch new accounts within time ranges")
    void fetchNewAccountsCounts_mapsRanges() {
        TimeRange range1 = new TimeRange(
                LocalDateTime.of(2023, 1, 1, 0, 0),
                LocalDateTime.of(2023, 3, 31, 23, 59)
        );
        TimeRange range2 = new TimeRange(
                LocalDateTime.of(2023, 4, 1, 0, 0),
                LocalDateTime.of(2023, 6, 30, 23, 59)
        );

        NewAccountsRequest request = new NewAccountsRequest(List.of(range1, range2));
        NewAccountsResponse response = dao.fetchNewAccountsCounts(request);

        assertThat(response).isNotNull();
        assertThat(response.getCounts()).hasSize(2);
        assertThat(response.getCounts().get(0)).isEqualTo(3L);
        assertThat(response.getCounts().get(1)).isEqualTo(2L);
    }

    @Test
    @DisplayName("Should fetch service counts correctly")
    void fetchServiceCounts_returnsCorrectCounts() {
        List<Long> serviceIds = List.of(1L, 2L, 3L, 4L);
        ServiceCountRequest request = new ServiceCountRequest(serviceIds);
        ServiceCountResponse response = dao.fetchServiceCounts(request);

        assertThat(response).isNotNull();
        assertThat(response.getCounts()).isNotNull();
        
        assertThat(response.getCounts().get(1L)).isEqualTo(2L);
        assertThat(response.getCounts().get(2L)).isEqualTo(1L);
        assertThat(response.getCounts().get(3L)).isEqualTo(1L);
        assertThat(response.getCounts().get(4L)).isEqualTo(1L);
    }

    @Test
    @DisplayName("Should handle empty service list")
    void fetchServiceCounts_handlesEmptyList() {
        ServiceCountRequest request = new ServiceCountRequest(List.of());
        ServiceCountResponse response = dao.fetchServiceCounts(request);

        assertThat(response).isNotNull();
        assertThat(response.getCounts()).isEmpty();
    }

    @Test
    @DisplayName("Should fetch city counts correctly")
    void fetchCityCounts_returnsCorrectCounts() {
        List<String> cities = List.of("Cairo", "Alexandria", "Giza");
        CityCountRequest request = new CityCountRequest(cities);
        CityCountResponse response = dao.fetchCityCounts(request);

        assertThat(response).isNotNull();
        assertThat(response.getCounts()).isNotNull();
        
        assertThat(response.getCounts().get("Cairo")).isEqualTo(3L);
        assertThat(response.getCounts().get("Alexandria")).isEqualTo(1L);
        assertThat(response.getCounts().get("Giza")).isEqualTo(1L);
    }

    @Test
    @DisplayName("Should handle empty city list")
    void fetchCityCounts_handlesEmptyList() {
        CityCountRequest request = new CityCountRequest(List.of());
        CityCountResponse response = dao.fetchCityCounts(request);

        assertThat(response).isNotNull();
        assertThat(response.getCounts()).isEmpty();
    }

    @Test
    @DisplayName("Should return zero for non-existent service")
    void fetchServiceCounts_returnsZeroForNonExistentService() {
        List<Long> serviceIds = List.of(999L);
        ServiceCountRequest request = new ServiceCountRequest(serviceIds);
        ServiceCountResponse response = dao.fetchServiceCounts(request);

        assertThat(response).isNotNull();
        assertThat(response.getCounts().get(999L)).isEqualTo(0L);
    }

    @Test
    @DisplayName("Should return zero for non-existent city")
    void fetchCityCounts_returnsZeroForNonExistentCity() {
        List<String> cities = List.of("NonExistentCity");
        CityCountRequest request = new CityCountRequest(cities);
        CityCountResponse response = dao.fetchCityCounts(request);

        assertThat(response).isNotNull();
        assertThat(response.getCounts().get("NonExistentCity")).isEqualTo(0L);
    }

    @Test
    @DisplayName("Should verify total taskers count through status")
    void fetchStatusCounts_verifiesTotalCount() {
        TaskerStatusCountsResponse response = dao.fetchStatusCounts();

        assertThat(response).isNotNull();
        long total = response.getActive() + response.getSuspended();
        assertThat(total).isEqualTo(5L);
    }
}
