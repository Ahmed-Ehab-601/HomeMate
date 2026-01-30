package com.homemate.taskmanagement.controller;

import com.homemate.security.model.AppUserDetails;
import com.homemate.taskmanagement.dto.TaskTimeDto;
import com.homemate.taskmanagement.service.TaskRequestService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/task")
public class TaskDateVerificationController {
    private final TaskRequestService taskRequestService;

    public TaskDateVerificationController(TaskRequestService taskRequestService) {
        this.taskRequestService = taskRequestService;
    }

    @GetMapping("/get-tasker-tasks/{taskerId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getAllBusyTime(
            @PathVariable Long taskerId,
            @RequestParam LocalDate day) {
        List<TaskTimeDto> response = taskRequestService.getAllBusyTime(taskerId, day);
        return ResponseEntity.ok(response);
    }
    @PostMapping("/add-estimation/{taskId}")
    @PreAuthorize("hasRole('TASKER')")
    public ResponseEntity<Void> addEstimationTime(
            @PathVariable Long taskId,
            @RequestParam int estimation,
            @AuthenticationPrincipal AppUserDetails userDetails) {
        taskRequestService.addEstimation(taskId, estimation, userDetails);
        return ResponseEntity.ok().build();
    }


    @GetMapping("/get-tasker-tasks-tasker-only/{taskerId}")
    @PreAuthorize("hasRole('TASKER')")
    public ResponseEntity<?> getAllBusyTaskerOnly(
            @PathVariable Long taskerId,
            @RequestParam LocalDate day) {
        List<TaskTimeDto> response = taskRequestService.getAllBusyTime(taskerId, day);
        return ResponseEntity.ok(response);
    }
}
