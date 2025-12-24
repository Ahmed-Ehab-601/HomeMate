package com.homemate.analysis.service;

import com.homemate.analysis.dao.ReportReviewAnalysisDao;
import com.homemate.analysis.dto.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportReviewAnalysisServiceTest {

    @Mock
    private ReportReviewAnalysisDao dao;

    @InjectMocks
    private ReportReviewAnalysisService service;

    @Test
    void getReportsPerService() {
        ReportsPerServiceResponse mockResponse = new ReportsPerServiceResponse(Map.of("Plumbing", 10L));
        when(dao.fetchReportsPerService()).thenReturn(mockResponse);

        ReportsPerServiceResponse result = service.getReportsPerService();
        assertThat(result).isEqualTo(mockResponse);
    }

    @Test
    void getReviewsPerService() {
        ReviewsPerServiceResponse mockResponse = new ReviewsPerServiceResponse(Map.of("Plumbing", 20L));
        when(dao.fetchReviewsPerService()).thenReturn(mockResponse);

        ReviewsPerServiceResponse result = service.getReviewsPerService();
        assertThat(result).isEqualTo(mockResponse);
    }

    @Test
    void getReportStatusCounts() {
        ReportStatusCountResponse mockResponse = new ReportStatusCountResponse(5L, 3L);
        when(dao.fetchReportStatusCounts()).thenReturn(mockResponse);

        ReportStatusCountResponse result = service.getReportStatusCounts();
        assertThat(result).isEqualTo(mockResponse);
    }

    @Test
    void getAvgRatingPerService() {
        AvgRatingPerServiceResponse mockResponse = new AvgRatingPerServiceResponse(Map.of("Plumbing", 4.5));
        when(dao.fetchAvgRatingPerService()).thenReturn(mockResponse);

        AvgRatingPerServiceResponse result = service.getAvgRatingPerService();
        assertThat(result).isEqualTo(mockResponse);
    }
}
