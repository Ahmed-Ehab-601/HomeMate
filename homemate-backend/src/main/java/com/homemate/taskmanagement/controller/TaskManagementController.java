package com.homemate.taskmanagement.controller;


import com.homemate.taskmanagement.dto.*;
import com.homemate.taskmanagement.service.TaskManagementService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<?> requestTask(@Valid @RequestBody TaskRequestDto requestDto) {
        Optional<TaskDto> taskDto = taskManagementService.requestTask(requestDto);
        if(taskDto.isPresent()) return new ResponseEntity<>(taskDto.get(),HttpStatus.CREATED);
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

    }
    @GetMapping("/user/tasks/{page}/{pageSize}")
    public ResponseEntity<?> getUserTasks(@RequestParam @NotNull(message = "userID required") Long userID,
                                          @RequestParam @NotNull(message = "Status required") StatusDto statusDto,
                                          @PathVariable("page") @Min(0)  int page,
                                          @PathVariable("pageSize")@Min(1) @Max(100) int pageSize){

        Optional<PaginatedResponse> response = taskManagementService.getUserTasks(userID,statusDto,page,pageSize);
        if(response.isEmpty()){
            return new ResponseEntity <> (HttpEntity.EMPTY,HttpStatus.OK);
        }else{
            return new ResponseEntity <> (response.get(),HttpStatus.OK);
        }
    }


}
