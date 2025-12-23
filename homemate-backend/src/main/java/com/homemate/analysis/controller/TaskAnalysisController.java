package com.homemate.analysis.controller;

import com.homemate.analysis.dto.*;
import com.homemate.analysis.service.TaskAnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/analysis/task")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class TaskAnalysisController {

    private final TaskAnalysisService taskAnalysisService;

    @PostMapping("/start-date-ranges")
    public ResponseEntity<TaskDateRangesResponse> getStartDateRanges(@RequestBody TaskDateRangesRequest request) {
        return ResponseEntity.ok(taskAnalysisService.getStartDateRanges(request));
    }

    @PostMapping("/end-date-ranges")
    public ResponseEntity<TaskDateRangesResponse> getEndDateRanges(@RequestBody TaskDateRangesRequest request) {
        return ResponseEntity.ok(taskAnalysisService.getEndDateRanges(request));
    }

    @GetMapping("/bill-ranges")
    public ResponseEntity<BillRangesResponse> getBillRanges() {
        return ResponseEntity.ok(taskAnalysisService.getBillRanges());
    }

    @GetMapping("/status-counts")
    public ResponseEntity<TaskStatusCountsResponse> getStatusCounts() {
        return ResponseEntity.ok(taskAnalysisService.getStatusCounts());
    }
}
