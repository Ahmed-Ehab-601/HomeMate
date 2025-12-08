package com.homemate.reports.controller;

import com.homemate.reports.dto.ShortReport;
import com.homemate.reports.service.IReportService;
import com.homemate.util.PaginatedResponse;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final IReportService reportService;

    @GetMapping("/short-reports")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaginatedResponse<ShortReport>> getAllShortReports(
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "20") int pageSize) {

        PaginatedResponse<ShortReport> response = reportService.getAllShortReports(pageNumber, pageSize);
        return ResponseEntity.ok(response);
    }
}
