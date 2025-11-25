package com.homemate.taskmanagement.controller;


import com.homemate.taskmanagement.dto.TaskDto;
import com.homemate.taskmanagement.dto.TaskRequestDto;
import com.homemate.taskmanagement.service.TaskManagementService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

}
