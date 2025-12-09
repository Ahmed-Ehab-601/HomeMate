package com.homemate.taskmanagement.controller;


import com.homemate.security.model.AppUserDetails;
import com.homemate.taskmanagement.dto.*;
import com.homemate.taskmanagement.model.Status;
import com.homemate.taskmanagement.service.TaskManagementService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@CrossOrigin("*")
public class TaskManagementController {
    private final TaskManagementService taskManagementService;

    public TaskManagementController(TaskManagementService taskManagementService) {
        this.taskManagementService = taskManagementService;
    }

    @PostMapping("/user/task/request")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> requestTask(
            @Valid @RequestBody TaskRequestDto requestDto,
            @AuthenticationPrincipal AppUserDetails userDetails
    ) {
        requestDto.setUserID(userDetails.getId());
        Optional<TaskDto> taskDto = taskManagementService.requestTask(requestDto);
        if(taskDto.isPresent()) return new ResponseEntity<>(taskDto.get(),HttpStatus.CREATED);
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @GetMapping("/user/tasks/{page}/{pageSize}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getUserTasks(
            @RequestParam @NotNull(message = "Status required") StatusDto statusDto,
            @PathVariable("page") @Min(0)  int page,
            @PathVariable("pageSize")@Min(1) @Max(100) int pageSize,
            @AuthenticationPrincipal AppUserDetails userDetails
    ){
        Optional<PaginatedResponse> response = taskManagementService.getUserTasks(userDetails.getId(),statusDto,page,pageSize);
        if(response.isEmpty()){
            return new ResponseEntity <> (HttpEntity.EMPTY,HttpStatus.OK);
        }else{
            return new ResponseEntity <> (response.get(),HttpStatus.OK);
        }
    }

    @GetMapping("/tasker/tasks/{page}/{pageSize}")
    @PreAuthorize("hasRole('TASKER')")
    public ResponseEntity<?> getTaskerTasks(
            @RequestParam @NotNull(message = "Status required") StatusDto statusDto,
            @PathVariable("page") @Min(0)  int page,
            @PathVariable("pageSize")@Min(1) @Max(100) int pageSize,
            @AuthenticationPrincipal AppUserDetails userDetails
    ){

        Optional<PaginatedResponse> response = taskManagementService.getTaskerTasks(userDetails.getId(),statusDto,page,pageSize);
        if(response.isEmpty()){
            return new ResponseEntity <> (HttpEntity.EMPTY,HttpStatus.OK);
        }else{
            return new ResponseEntity <> (response.get(),HttpStatus.OK);
        }
    }

    @PreAuthorize("hasRole('TASKER')") @PatchMapping("/tasker/task/{taskID}/accept")
    public ResponseEntity<?> acceptTask(@PathVariable @NotNull Long taskID, @AuthenticationPrincipal AppUserDetails userDetails){
        TaskRequestResponseDto responseDto = taskManagementService.acceptOrReject(taskID,userDetails.getId(), Status.Accepted);
        return new ResponseEntity<>(responseDto,HttpStatus.OK);

    }
    @PreAuthorize("hasRole('TASKER')") @PatchMapping("/tasker/task/{taskID}/reject")
    public ResponseEntity<?> rejectTask(@PathVariable @NotNull Long taskID, @AuthenticationPrincipal AppUserDetails userDetails){
        TaskRequestResponseDto responseDto = taskManagementService.acceptOrReject(taskID,userDetails.getId(), Status.Rejected);
        return new ResponseEntity<>(responseDto,HttpStatus.OK);

    }
    @PatchMapping("/task/{taskId}/reschedule")
    @PreAuthorize("hasAnyRole('USER','TASKER')")
    public ResponseEntity<?> rescheduleTask(
            @PathVariable @Valid Long taskId,
            @Valid @RequestBody RescheduleRequestDto requestDto,
            @AuthenticationPrincipal AppUserDetails user
    ) {
        RescheduleResponseDto response = taskManagementService.rescheduleTask(taskId, requestDto, user.getId());
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('TASKER')") @PatchMapping("/tasker/task/{taskID}/start")
    public ResponseEntity<?> startTask(@PathVariable @NotNull Long taskID, @AuthenticationPrincipal AppUserDetails userDetails){
        TaskDto taskDto = taskManagementService.updateTaskStatus(taskID,userDetails.getId(), Status.InProgress);
        return new ResponseEntity<>(taskDto,HttpStatus.OK);
    }

    @PreAuthorize("hasRole('TASKER')") @PatchMapping("/tasker/task/{taskID}/suspend")
    public ResponseEntity<?> suspendTask(@PathVariable @NotNull Long taskID, @AuthenticationPrincipal AppUserDetails userDetails){
        TaskDto taskDto = taskManagementService.updateTaskStatus(taskID,userDetails.getId(), Status.Suspended);
        return new ResponseEntity<>(taskDto,HttpStatus.OK);
    }
    @PreAuthorize("hasRole('TASKER')") @PatchMapping("/tasker/task/{taskID}/complete")
    public ResponseEntity<?> completeTask(@PathVariable @NotNull Long taskID, @AuthenticationPrincipal AppUserDetails userDetails){
        TaskDto taskDto = taskManagementService.updateTaskStatus(taskID,userDetails.getId(), Status.Done);
        return new ResponseEntity<>(taskDto,HttpStatus.OK);
    }

}
