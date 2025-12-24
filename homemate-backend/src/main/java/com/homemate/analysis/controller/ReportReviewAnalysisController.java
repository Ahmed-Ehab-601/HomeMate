package com.homemate.analysis.controller;

import com.homemate.analysis.dto.*;
import com.homemate.analysis.service.ReportReviewAnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/analysis/report-review")
@RequiredArgsConstructor
public class ReportReviewAnalysisController {

    private final ReportReviewAnalysisService service;

    @GetMapping("/reports-per-service")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ReportsPerServiceResponse> getReportsPerService() {
        return ResponseEntity.ok(service.getReportsPerService());
    }

    @GetMapping("/reviews-per-service")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ReviewsPerServiceResponse> getReviewsPerService() {
        return ResponseEntity.ok(service.getReviewsPerService());
    }

    @GetMapping("/report-status-counts")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ReportStatusCountResponse> getReportStatusCounts() {
        return ResponseEntity.ok(service.getReportStatusCounts());
    }

    @GetMapping("/avg-rating-per-service")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AvgRatingPerServiceResponse> getAvgRatingPerService() {
        return ResponseEntity.ok(service.getAvgRatingPerService());
    }
}
