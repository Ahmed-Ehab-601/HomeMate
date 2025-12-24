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

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("analysis")
class ReportReviewAnalysisDaoTest {

    @Autowired
    private ReportReviewAnalysisDao dao;

    @Test
    @DisplayName("Should fetch reports count per service correctly")
    void fetchReportsPerService() {
        ReportsPerServiceResponse response = dao.fetchReportsPerService();
        assertThat(response).isNotNull();
        Map<String, Long> counts = response.getCounts();
        
        // Based on analysisData.sql:
        // Report 1 -> Task 1 -> Service 1 (Plumbing)
        // Report 2 -> Task 2 -> Service 2 (Electrical)
        // Report 3 -> Task 5 -> Service 3 (Cleaning)
        
        assertThat(counts.get("Plumbing")).isEqualTo(1L);
        assertThat(counts.get("Electrical")).isEqualTo(1L);
        assertThat(counts.get("Cleaning")).isEqualTo(1L);
    }

    @Test
    @DisplayName("Should fetch reviews count per service correctly")
    void fetchReviewsPerService() {
        ReviewsPerServiceResponse response = dao.fetchReviewsPerService();
        assertThat(response).isNotNull();
        Map<String, Long> counts = response.getCounts();

        // Reviews:
        // Rev 1 -> Task 1 (Plumbing)
        // Rev 2 -> Task 2 (Electrical)
        // Rev 3 -> Task 6 (Painting)
        // Rev 4 -> Task 8 (Plumbing)
        // Rev 5 -> Task 10 (Plumbing)

        assertThat(counts.get("Plumbing")).isEqualTo(3L);
        assertThat(counts.get("Electrical")).isEqualTo(1L);
        assertThat(counts.get("Painting")).isEqualTo(1L);
    }
    
    @Test
    @DisplayName("Should fetch report status counts correctly")
    void fetchReportStatusCounts() {
        ReportStatusCountResponse response = dao.fetchReportStatusCounts();
        
        // Report 1: pending
        // Report 2: pending
        // Report 3: done
        
        assertThat(response.getPending()).isEqualTo(2L);
        assertThat(response.getDone()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Should fetch avg rating per service correctly")
    void fetchAvgRatingPerService() {
        AvgRatingPerServiceResponse response = dao.fetchAvgRatingPerService();
        Map<String, Double> ratings = response.getRatings();

        // Plumbing: Res 1 (4.5), Rev 4 (4.5), Rev 5 (3.5) -> Avg (12.5 / 3) = 4.166
        // Electrical: Rev 2 (5.0) -> 5.0
        // Painting: Rev 3 (4.0) -> 4.0
        
        assertThat(ratings.get("Electrical")).isEqualTo(5.0);
        assertThat(ratings.get("Painting")).isEqualTo(4.0);
        assertThat(ratings.get("Plumbing")).isBetween(4.1, 4.2);
    }
}
