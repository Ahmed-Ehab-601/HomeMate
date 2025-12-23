package com.homemate.taskmanagement.controller;


import com.homemate.TaskerProfile.DTO.ReviewDTO;
import com.homemate.security.model.AppUserDetails;
import com.homemate.taskmanagement.dto.*;
import com.homemate.taskmanagement.model.Status;
import com.homemate.taskmanagement.service.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@CrossOrigin("*")

public class TaskManagementController {
    private final TaskRequestService taskRequestService;
    private final GetTaskService getTaskService;
    private final TaskStatusService taskStatusService;
    private final TaskRescheduleService taskRescheduleService;
    private final TaskViewService taskViewService;

    public TaskManagementController(TaskRequestService taskRequestService, GetTaskService getTaskService, TaskStatusService taskStatusService, TaskRescheduleService taskRescheduleService, TaskViewService taskViewService) {
        this.taskRequestService = taskRequestService;
        this.getTaskService = getTaskService;
        this.taskStatusService = taskStatusService;
        this.taskRescheduleService = taskRescheduleService;
        this.taskViewService = taskViewService;
    }


    @PostMapping("/user/task/request")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> requestTask(
            @Valid @RequestBody TaskRequestDto requestDto,
            @AuthenticationPrincipal AppUserDetails userDetails
    ) {
        requestDto.setUserID(userDetails.getId());
        Optional<TaskDto> taskDto = taskRequestService.requestTask(requestDto);
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
        Optional<PaginatedResponse> response = getTaskService.getUserTasks(userDetails.getId(),statusDto,page,pageSize);
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

        Optional<PaginatedResponse> response = getTaskService.getTaskerTasks(userDetails.getId(),statusDto,page,pageSize);
        if(response.isEmpty()){
            return new ResponseEntity <> (HttpEntity.EMPTY,HttpStatus.OK);
        }else{
            return new ResponseEntity <> (response.get(),HttpStatus.OK);
        }
    }
    @GetMapping("/tasker/getTasks")
    @PreAuthorize("hasRole('TASKER')")
    public ResponseEntity<?> getTaskerTasksForCalendar(
            @AuthenticationPrincipal AppUserDetails userDetails,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "All") StatusDto status) {
        List<TaskCardDto> tasks = getTaskService.getTaskerTasksForNewView(userDetails.getId(), startDate, endDate, status);
        return new ResponseEntity <> (tasks,HttpStatus.OK);

    }
    @GetMapping("/user/getTasks")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getUserTasksForCalendar(
            @AuthenticationPrincipal AppUserDetails userDetails,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "All") StatusDto status) {

        List<TaskCardDto> tasks = getTaskService.getUserTasksForNewView(userDetails.getId(), startDate, endDate, status);
        return new ResponseEntity <> (tasks,HttpStatus.OK);
    }

    @PreAuthorize("hasRole('TASKER')") @PatchMapping("/tasker/task/{taskID}/accept")
    public ResponseEntity<?> acceptTask(@PathVariable @NotNull Long taskID, @AuthenticationPrincipal AppUserDetails userDetails){
        TaskRequestResponseDto responseDto = taskStatusService.acceptOrReject(taskID,userDetails.getId(), Status.Accepted);
        return new ResponseEntity<>(responseDto,HttpStatus.OK);

    }
    @PreAuthorize("hasRole('TASKER')") @PatchMapping("/tasker/task/{taskID}/reject")
    public ResponseEntity<?> rejectTask(@PathVariable @NotNull Long taskID, @AuthenticationPrincipal AppUserDetails userDetails){
        TaskRequestResponseDto responseDto = taskStatusService.acceptOrReject(taskID,userDetails.getId(), Status.Rejected);
        return new ResponseEntity<>(responseDto,HttpStatus.OK);

    }
    @PatchMapping("/task/{taskId}/reschedule")
    @PreAuthorize("hasAnyRole('USER','TASKER')")
    public ResponseEntity<?> rescheduleTask(
            @PathVariable @Valid Long taskId,
            @Valid @RequestBody RescheduleRequestDto requestDto,
            @AuthenticationPrincipal AppUserDetails user
    ) {
        RescheduleResponseDto response = taskRescheduleService.rescheduleTask(taskId, requestDto, user.getId());
        return ResponseEntity.ok(response);
    }
    @PreAuthorize("hasRole('TASKER')") @PatchMapping("/tasker/task/{taskID}/start")
    public ResponseEntity<?> startTask(@PathVariable @NotNull Long taskID, @AuthenticationPrincipal AppUserDetails userDetails){
        TaskDto taskDto = taskStatusService.updateTaskStatus(taskID,userDetails.getId(), Status.InProgress);
        return new ResponseEntity<>(taskDto,HttpStatus.OK);
    }

    @PreAuthorize("hasRole('TASKER')") @PatchMapping("/tasker/task/{taskID}/suspend")
    public ResponseEntity<?> suspendTask(@PathVariable @NotNull Long taskID, @AuthenticationPrincipal AppUserDetails userDetails){
        TaskDto taskDto = taskStatusService.updateTaskStatus(taskID,userDetails.getId(), Status.Suspended);
        return new ResponseEntity<>(taskDto,HttpStatus.OK);
    }
    @PreAuthorize("hasRole('TASKER')") @PatchMapping("/tasker/task/{taskID}/complete")
    public ResponseEntity<?> completeTask(@PathVariable @NotNull Long taskID, @AuthenticationPrincipal AppUserDetails userDetails){
        TaskDto taskDto = taskStatusService.updateTaskStatus(taskID,userDetails.getId(), Status.Done);
        return new ResponseEntity<>(taskDto,HttpStatus.OK);
    }

    @GetMapping("/task/{taskId}/taskDetails")
    @PreAuthorize("hasAnyRole('USER','TASKER')")
    public ResponseEntity<?> viewTask(
            @PathVariable @Valid Long taskId,
            @AuthenticationPrincipal AppUserDetails user
    ){
        Optional<TaskDto> response = taskViewService.getTaskDetails(taskId,user.getId());
        return ResponseEntity.ok(response);
    }


    @GetMapping("/task/{taskId}/taskReview")
    @PreAuthorize("hasAnyRole('USER','TASKER')")
    public ResponseEntity<?> getTaskReview(
            @PathVariable Long taskId,
            @AuthenticationPrincipal AppUserDetails user
    ){
        Optional<ReviewDTO> reviewDTO = taskViewService.getReviewByTaskId(taskId, user.getId());
        return ResponseEntity.ok(reviewDTO);
    }




}
