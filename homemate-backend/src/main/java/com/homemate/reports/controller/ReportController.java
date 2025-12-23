package com.homemate.reports.controller;

import com.homemate.reports.dto.ShortReport;
import com.homemate.reports.dto.SubmitReport;
import com.homemate.reports.dto.DetailedReport;
import com.homemate.reports.dto.ReportFilterDto;
import com.homemate.reports.service.IReportService;
import com.homemate.security.model.AppUserDetails;
import com.homemate.util.PaginatedResponse;
import com.homemate.reports.dto.ReportResponseRequest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

    @PostMapping
    @PreAuthorize("hasAnyRole('USER','TASKER')")
    public ResponseEntity<String> submitReport(
            @RequestBody SubmitReport submitReport,
            @AuthenticationPrincipal AppUserDetails userDetails) {

        boolean ok = reportService.submitReport(submitReport, userDetails);
        if (ok) return ResponseEntity.ok("Report Submitted");
        return ResponseEntity.badRequest().body("Invalid report data or task not accessible");
    }
  
    @GetMapping("/{reportID}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DetailedReport> getDetailedReport(@PathVariable int reportID) {
        var detailedReport = reportService.getDetailedReportById(reportID);
        return detailedReport
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PatchMapping("/{reportID}/complete")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DetailedReport> completeReport(@PathVariable int reportID) {
        var completedReport = reportService.completeReport(reportID);
        return completedReport
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PostMapping("/{reportID}/response")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> respondToReport(
            @PathVariable int reportID,
            @RequestBody @jakarta.validation.Valid ReportResponseRequest request) {
        
        try {
            reportService.respondToReport(reportID, request.getMessage());
            return ResponseEntity.ok("Response sent successfully");

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());

        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred");

        }
    }
}
