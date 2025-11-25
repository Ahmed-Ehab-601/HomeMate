package com.homemate.taskerdiscovery.controller;

import com.homemate.taskerdiscovery.dto.FindTaskerCriteriaDto;
import com.homemate.taskerdiscovery.dto.TaskerCardDto;
import com.homemate.taskerdiscovery.service.TaskerDiscoveryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/taskers")
@CrossOrigin("*")
public class TaskerDiscoveryController {

    private final TaskerDiscoveryService taskerDiscoveryService;

    @Autowired
    public TaskerDiscoveryController(TaskerDiscoveryService taskerDiscoveryService) {
        this.taskerDiscoveryService = taskerDiscoveryService;
    }

    @PostMapping("/search")
    public ResponseEntity<List<TaskerCardDto>> findTaskers(
            @RequestBody @Valid FindTaskerCriteriaDto criteria,
            @RequestParam int page,
            @RequestParam int size) {

        List<TaskerCardDto> results = taskerDiscoveryService.getTaskerCardsByFilters(criteria, page, size);
        return ResponseEntity.ok(results);
    }
}
