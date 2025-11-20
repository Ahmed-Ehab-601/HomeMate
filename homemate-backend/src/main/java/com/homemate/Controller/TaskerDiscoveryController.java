package com.homemate.Controller;

import com.homemate.Dto.FindTaskerCriteriaDto;
import com.homemate.Dto.TaskerCardDto;
import com.homemate.Service.TaskerDiscoveryService;
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
