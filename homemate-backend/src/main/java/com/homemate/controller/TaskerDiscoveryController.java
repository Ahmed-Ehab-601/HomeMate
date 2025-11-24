package com.homemate.controller;

import com.homemate.dto.FindTaskerCriteriaDto;
import com.homemate.dto.TaskerCardDto;
import com.homemate.service.TaskerDiscoveryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/taskers")
public class TaskerDiscoveryController {

    private final TaskerDiscoveryService taskerDiscoveryService;

    @Autowired
    public TaskerDiscoveryController(TaskerDiscoveryService taskerDiscoveryService) {
        this.taskerDiscoveryService = taskerDiscoveryService;
    }

    @PostMapping("/search")
    public ResponseEntity<List<TaskerCardDto>> findTaskers(
            @RequestBody FindTaskerCriteriaDto criteria,
            @RequestParam int page,
            @RequestParam int size) {

        List<TaskerCardDto> results = taskerDiscoveryService.getTaskerCardsByFilters(criteria, page, size);
        return ResponseEntity.ok(results);
    }
}
