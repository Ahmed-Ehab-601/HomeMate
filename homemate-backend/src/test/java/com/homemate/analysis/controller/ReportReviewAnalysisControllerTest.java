package com.homemate.analysis.controller;

import com.homemate.analysis.dto.*;
import com.homemate.analysis.service.ReportReviewAnalysisService;
import com.homemate.security.filter.JwtAuthFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ReportReviewAnalysisController.class,
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                SecurityFilterAutoConfiguration.class,
                org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientAutoConfiguration.class,
                org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientWebSecurityAutoConfiguration.class,
                org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration.class
        })
@AutoConfigureMockMvc(addFilters = false)
class ReportReviewAnalysisControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReportReviewAnalysisService service;

    @MockitoBean
    private JwtAuthFilter jwtAuthFilter;

    @Test
    void getReportsPerService() throws Exception {
        ReportsPerServiceResponse mockResponse = new ReportsPerServiceResponse(Map.of("Plumbing", 10L));
        when(service.getReportsPerService()).thenReturn(mockResponse);

        mockMvc.perform(get("/api/analysis/report-review/reports-per-service"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.counts.Plumbing").value(10));
    }

    @Test
    void getReviewsPerService() throws Exception {
        ReviewsPerServiceResponse mockResponse = new ReviewsPerServiceResponse(Map.of("Plumbing", 20L));
        when(service.getReviewsPerService()).thenReturn(mockResponse);

        mockMvc.perform(get("/api/analysis/report-review/reviews-per-service"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.counts.Plumbing").value(20));
    }

    @Test
    void getReportStatusCounts() throws Exception {
        ReportStatusCountResponse mockResponse = new ReportStatusCountResponse(5L, 3L);
        when(service.getReportStatusCounts()).thenReturn(mockResponse);

        mockMvc.perform(get("/api/analysis/report-review/report-status-counts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pending").value(5))
                .andExpect(jsonPath("$.done").value(3));
    }

    @Test
    void getAvgRatingPerService() throws Exception {
        AvgRatingPerServiceResponse mockResponse = new AvgRatingPerServiceResponse(Map.of("Plumbing", 4.5));
        when(service.getAvgRatingPerService()).thenReturn(mockResponse);

        mockMvc.perform(get("/api/analysis/report-review/avg-rating-per-service"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ratings.Plumbing").value(4.5));
    }
}
