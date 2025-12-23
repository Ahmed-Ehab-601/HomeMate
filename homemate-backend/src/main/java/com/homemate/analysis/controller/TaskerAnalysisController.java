package com.homemate.analysis.controller;

import com.homemate.analysis.dto.*;
import com.homemate.analysis.service.TaskerAnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/analysis/tasker")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class TaskerAnalysisController {

    private final TaskerAnalysisService taskerAnalysisService;

    @GetMapping("/gender-count")
    public ResponseEntity<GenderCountResponse> getGenderCount() {
        return ResponseEntity.ok(taskerAnalysisService.getGenderCount());
    }

    @GetMapping("/age-buckets")
    public ResponseEntity<AgeBucketsResponse> getAgeBuckets() {
        return ResponseEntity.ok(taskerAnalysisService.getAgeBuckets());
    }

    @PostMapping("/new-accounts")
    public ResponseEntity<NewAccountsResponse> getNewAccounts(@RequestBody NewAccountsRequest request) {
        return ResponseEntity.ok(taskerAnalysisService.getNewAccountsCounts(request));
    }

    @GetMapping("/rating-ranges")
    public ResponseEntity<RatingRangesResponse> getRatingRanges() {
        return ResponseEntity.ok(taskerAnalysisService.getRatingRanges());
    }

    @GetMapping("/hour-rate-ranges")
    public ResponseEntity<HourRateRangesResponse> getHourRateRanges() {
        return ResponseEntity.ok(taskerAnalysisService.getHourRateRanges());
    }

    @GetMapping("/worked-hours-ranges")
    public ResponseEntity<WorkedHoursRangesResponse> getWorkedHoursRanges() {
        return ResponseEntity.ok(taskerAnalysisService.getWorkedHoursRanges());
    }

    @PostMapping("/service-counts")
    public ResponseEntity<ServiceCountResponse> getServiceCounts(@RequestBody ServiceCountRequest request) {
        return ResponseEntity.ok(taskerAnalysisService.getServiceCounts(request));
    }

    @PostMapping("/city-counts")
    public ResponseEntity<CityCountResponse> getCityCounts(@RequestBody CityCountRequest request) {
        return ResponseEntity.ok(taskerAnalysisService.getCityCounts(request));
    }

    @GetMapping("/status-counts")
    public ResponseEntity<TaskerStatusCountsResponse> getStatusCounts() {
        return ResponseEntity.ok(taskerAnalysisService.getStatusCounts());
    }
}
