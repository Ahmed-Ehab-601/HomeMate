package com.homemate.analysis.controller;

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

    @GetMapping("/placeholder")
    public ResponseEntity<String> placeholder() {
        return ResponseEntity.ok("OK");
    }
}
