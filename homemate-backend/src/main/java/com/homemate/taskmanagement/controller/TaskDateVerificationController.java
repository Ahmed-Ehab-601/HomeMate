package com.homemate.taskmanagement.controller;

import com.homemate.security.model.AppUserDetails;
import com.homemate.taskmanagement.service.TaskRequestService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/task")
public class TaskDateVerificationController {
    private final TaskRequestService taskRequestService;

    public TaskDateVerificationController(TaskRequestService taskRequestService) {
        this.taskRequestService = taskRequestService;
    }

    /**
     * Get all busy time slots for a tasker on a specific day (accessible by USER role)
     * @param taskerId the ID of the tasker
     * @param day the date to check availability
     * @return Map of LocalDateTime to Integer (duration in minutes)
     */
    @GetMapping("/get-tasker-tasks/{taskerId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Map<LocalDateTime, Integer>> getAllBusyTime(
            @PathVariable Long taskerId,
            @RequestParam LocalDate day) {
        Map<LocalDateTime, Integer> response = taskRequestService.getAllBusyTime(taskerId, day);
        return ResponseEntity.ok(response);
    }

    /**
     * Add estimation time for a task (TASKER role only)
     * @param taskId the ID of the task
     * @param estimation the estimated duration in minutes
     * @param userDetails the authenticated tasker details
     * @return success response
     */
    @PostMapping("/add-estimation/{taskId}")
    @PreAuthorize("hasRole('TASKER')")
    public ResponseEntity<Void> addEstimationTime(
            @PathVariable Long taskId,
            @RequestParam int estimation,
            @AuthenticationPrincipal AppUserDetails userDetails) {
        taskRequestService.addEstimation(taskId, estimation, userDetails);
        return ResponseEntity.ok().build();
    }

    /**
     * Get all busy time slots for a tasker on a specific day (accessible by TASKER role only)
     * @param taskerId the ID of the tasker
     * @param day the date to check availability
     * @return Map of LocalDateTime to Integer (duration in minutes)
     */
    @GetMapping("/get-tasker-tasks-tasker-only/{taskerId}")
    @PreAuthorize("hasRole('TASKER')")
    public ResponseEntity<Map<LocalDateTime, Integer>> getAllBusyTaskerOnly(
            @PathVariable Long taskerId,
            @RequestParam LocalDate day) {
        Map<LocalDateTime, Integer> response = taskRequestService.getAllBusyTime(taskerId, day);
        return ResponseEntity.ok(response);
    }

    /**
     * Get estimation details for a specific task (TASKER role only)
     * @param taskId the ID of the task
     * @return the estimation time in minutes
     */
    @GetMapping("/get-taskDetails-estimation/{taskId}")
    @PreAuthorize("hasRole('TASKER')")
    public ResponseEntity<Integer> getEstimation(@PathVariable Long taskId) {
        int response = taskRequestService.getTaskDetails(taskId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get estimation details for a specific task (USER role only)
     * @param taskId the ID of the task
     * @return the estimation time in minutes
     */
    @GetMapping("/get-taskDetails-estimation-user/{taskId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Integer> getEstimationUser(@PathVariable Long taskId) {
        int response = taskRequestService.getTaskDetails(taskId);
        return ResponseEntity.ok(response);
    }
}