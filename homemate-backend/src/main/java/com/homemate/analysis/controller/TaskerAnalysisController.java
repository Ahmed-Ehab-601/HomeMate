package com.homemate.analysis.controller;

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

    @GetMapping("/placeholder")
    public ResponseEntity<String> placeholder() {
        return ResponseEntity.ok("OK");
    }
}
