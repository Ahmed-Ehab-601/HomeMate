package com.homemate.analysis.controller;

import com.homemate.analysis.dto.*;
import com.homemate.analysis.service.UserAnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/analysis/user")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UserAnalysisController {

    private final UserAnalysisService userAnalysisService;

    @GetMapping("/gender-count")
    public ResponseEntity<GenderCountResponse> getGenderCount() {
        return ResponseEntity.ok(userAnalysisService.getGenderCount());
    }

    @GetMapping("/status-counts")
    public ResponseEntity<StatusCountsResponse> getStatusCounts() {
        return ResponseEntity.ok(userAnalysisService.getStatusCounts());
    }

    @GetMapping("/age-buckets")
    public ResponseEntity<AgeBucketsResponse> getAgeBuckets() {
        return ResponseEntity.ok(userAnalysisService.getAgeBuckets());
    }

    @PostMapping("/new-accounts")
    public ResponseEntity<NewAccountsResponse> getNewAccounts(@RequestBody NewAccountsRequest request) {
        return ResponseEntity.ok(userAnalysisService.getNewAccountsCounts(request));
    }
}
