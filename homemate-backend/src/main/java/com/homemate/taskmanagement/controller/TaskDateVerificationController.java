package com.homemate.taskmanagement.controller;

import com.homemate.security.model.AppUserDetails;
import com.homemate.taskmanagement.service.TaskRequestService;
import org.springframework.http.HttpEntity;
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
    @GetMapping("/get-tasker-tasks/{taskerId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getAllBusyTime(@PathVariable Long taskerId,
                                            @RequestParam LocalDate day){
          try {
             Map<LocalDateTime, Integer> response= taskRequestService.getAllBusyTime(taskerId,day);
             if(response.isEmpty())
             { return new ResponseEntity <> (HttpEntity.EMPTY,HttpStatus.OK);}
             else {
                 return new ResponseEntity<>(response, HttpStatus.OK);
             }
          } catch (RuntimeException e) {
              // Check if the error is due to tasker being unavailable
              if (e.getMessage() != null && e.getMessage().contains("UNAVAILABLE")) {
                  Map<String, String> errorResponse = new java.util.HashMap<>();
                  errorResponse.put("error", "TASKER_UNAVAILABLE");
                  errorResponse.put("message", "This Tasker is UNAVAILABLE Now");
                  return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                          .body(errorResponse);
              }
              return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
          } catch (Exception e) {
              return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
          }

    }
    @PostMapping("/add-estimation/{taskId}")
    @PreAuthorize("hasRole('TASKER')")
    public ResponseEntity<?> addEstimationTime(@PathVariable Long taskId,
                                               @RequestParam int estimation,
                                               @AuthenticationPrincipal AppUserDetails userDetails) {
   try{
        taskRequestService.addEstimation(taskId,estimation,userDetails);
        return ResponseEntity.status(HttpStatus.OK).build();
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
    }
}
