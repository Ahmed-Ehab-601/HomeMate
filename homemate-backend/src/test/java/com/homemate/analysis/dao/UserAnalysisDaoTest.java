package com.homemate.analysis.dao;

import com.homemate.analysis.dto.AgeBucketsResponse;
import com.homemate.analysis.dto.GenderCountResponse;
import com.homemate.analysis.dto.NewAccountsRequest;
import com.homemate.analysis.dto.NewAccountsResponse;
import com.homemate.analysis.dto.StatusCountsResponse;
import com.homemate.analysis.dto.TimeRange;
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
class UserAnalysisDaoTest {

    @Autowired
    private UserAnalysisDao dao;

    @Test
    @DisplayName("Should fetch gender counts correctly")
    void fetchGenderCounts_returnsCorrectCounts() {
        GenderCountResponse response = dao.fetchGenderCounts();

        assertThat(response).isNotNull();
        assertThat(response.getMale()).isEqualTo(3L); // John, Bob, Charlie
        assertThat(response.getFemale()).isEqualTo(3L); // Jane, Alice, Diana
    }

    @Test
    @DisplayName("Should fetch status counts correctly")
    void fetchStatusCounts_aggregatesQueryResults() {
        StatusCountsResponse response = dao.fetchStatusCounts();

        assertThat(response).isNotNull();
        assertThat(response.getSuspended()).isEqualTo(2L); // Bob and Diana
        assertThat(response.getAdminActive()).isEqualTo(0L); // No admins in test data
        assertThat(response.getNonAdminActive()).isEqualTo(4L); // John, Jane, Alice, Charlie
    }

    @Test
    @DisplayName("Should fetch age buckets correctly")
    void fetchAgeBuckets_returnsCorrectDistribution() {
        AgeBucketsResponse response = dao.fetchAgeBuckets();

        assertThat(response).isNotNull();
        assertThat(response.getBuckets()).isNotNull();
        Map<String, Long> buckets = response.getBuckets();

        // Verify some age ranges have data (based on birthdates in test data)
        Long total = buckets.values().stream().mapToLong(Long::longValue).sum();
        assertThat(total).isEqualTo(6L); // Total users with birthdate
    }

    @Test
    @DisplayName("Should fetch new accounts within time ranges")
    void fetchNewAccountsCounts_mapsRanges() {
        // Test data has users created from Jan to June 2023
        TimeRange range1 = new TimeRange(
                LocalDateTime.of(2023, 1, 1, 0, 0),
                LocalDateTime.of(2023, 4, 1, 0, 0)
        );
        TimeRange range2 = new TimeRange(
                LocalDateTime.of(2023, 4, 1, 0, 0),
                LocalDateTime.of(2023, 7, 1, 0, 0)
        );

        NewAccountsRequest request = new NewAccountsRequest(List.of(range1, range2));
        NewAccountsResponse response = dao.fetchNewAccountsCounts(request);

        assertThat(response).isNotNull();
        assertThat(response.getCounts()).hasSize(2);
        assertThat(response.getCounts().get(0)).isEqualTo(3L); // John, Jane, Bob
        assertThat(response.getCounts().get(1)).isEqualTo(3L); // Alice, Charlie, Diana
    }

    @Test
    @DisplayName("Should return zero for time ranges with no accounts")
    void fetchNewAccountsCounts_returnsZeroForEmptyRanges() {
        TimeRange emptyRange = new TimeRange(
                LocalDateTime.of(2020, 1, 1, 0, 0),
                LocalDateTime.of(2020, 12, 31, 23, 59)
        );

        NewAccountsRequest request = new NewAccountsRequest(List.of(emptyRange));
        NewAccountsResponse response = dao.fetchNewAccountsCounts(request);

        assertThat(response).isNotNull();
        assertThat(response.getCounts()).hasSize(1);
        assertThat(response.getCounts().get(0)).isEqualTo(0L);
    }

    @Test
    @DisplayName("Should handle empty time range list")
    void fetchNewAccountsCounts_handlesEmptyList() {
        NewAccountsRequest request = new NewAccountsRequest(List.of());
        NewAccountsResponse response = dao.fetchNewAccountsCounts(request);

        assertThat(response).isNotNull();
        assertThat(response.getCounts()).isEmpty();
    }

    @Test
    @DisplayName("Should verify age buckets cover all ranges")
    void fetchAgeBuckets_coversAllRanges() {
        AgeBucketsResponse response = dao.fetchAgeBuckets();

        assertThat(response).isNotNull();
        Map<String, Long> buckets = response.getBuckets();

        // Verify all expected age ranges are present
        assertThat(buckets).containsKeys("0-10", "10-20", "20-30", "30-40", "40-50", "50-60", "60+");
    }

    @Test
    @DisplayName("Should count suspended and active users correctly")
    void fetchStatusCounts_verifiesSuspendedAndActive() {
        StatusCountsResponse response = dao.fetchStatusCounts();

        assertThat(response).isNotNull();
        long totalActive = response.getAdminActive() + response.getNonAdminActive();
        long totalSuspended = response.getSuspended();

        assertThat(totalActive + totalSuspended).isEqualTo(6L); // Total users in test data
    }
}
