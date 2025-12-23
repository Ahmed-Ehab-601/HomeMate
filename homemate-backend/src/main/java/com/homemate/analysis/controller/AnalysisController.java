/*
package com.homemate.analysis.controller;

import com.homemate.analysis.dto.AnalysisResponse;
import com.homemate.analysis.service.AnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

// Deprecated: Consolidated into User/Tasker/Task analysis controllers.
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/analysis")
@RequiredArgsConstructor
public class AnalysisController {

    private final AnalysisService analysisService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AnalysisResponse> getAnalysis() {
        AnalysisResponse response = analysisService.getAnalysis();
        return ResponseEntity.ok(response);
    }
}
*/
