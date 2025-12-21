package com.homemate.analysis.controller;

import com.homemate.analysis.dto.AnalysisResponse;
import com.homemate.analysis.service.IAnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/analysis")
@RequiredArgsConstructor
public class AnalysisController {

    private final IAnalysisService analysisService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AnalysisResponse> getAnalysis() {
        AnalysisResponse response = analysisService.getAnalysis();
        return ResponseEntity.ok(response);
    }
}
