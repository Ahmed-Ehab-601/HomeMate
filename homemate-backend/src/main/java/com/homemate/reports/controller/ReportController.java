package com.homemate.reports.controller;

import com.homemate.reports.dto.ShortReport;
import com.homemate.reports.dto.ReportFilterDto;
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
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String header,
            @RequestParam(required = false) String body,
            @RequestParam(required = false) Integer taskID,
            @RequestParam(required = false) Boolean reporter,
            @RequestParam(required = false) String adminStatus) {

        ReportFilterDto filterDto = ReportFilterDto.builder()
                .header(header)
                .body(body)
                .taskID(taskID)
                .reporter(reporter)
                .adminStatus(adminStatus)
                .build();

        PaginatedResponse<ShortReport> response = reportService.getAllShortReports(pageNumber, pageSize, filterDto);
        return ResponseEntity.ok(response);
    }
}
