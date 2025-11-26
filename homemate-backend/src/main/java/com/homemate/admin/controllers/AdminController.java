package com.homemate.admin.controllers;

import com.homemate.admin.domain.dto.*;
import com.homemate.admin.domain.dto.*;
import com.homemate.admin.services.IAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {
    private final IAdminService adminService;

    @GetMapping("/users")
    public ResponseEntity<PageResponse<UserDto>> getUsers(
           @ModelAttribute PageRequest pageRequest,
            @ModelAttribute UserFilterDto userFilterDto) {

        PageResponse<UserDto> response = adminService.getUsers(userFilterDto, pageRequest);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/taskers")
    public ResponseEntity<PageResponse<TaskerDto>> getTaskers(
             @ModelAttribute PageRequest pageRequest,
            @ModelAttribute TaskerFilterDto taskerFilterDto) {

        PageResponse<TaskerDto> response = adminService.getTaskers(taskerFilterDto, pageRequest);
        return ResponseEntity.ok(response);
    }
    
    @PatchMapping("/user/suspend")
    public ResponseEntity<UserDto> suspendUserJson(@RequestBody SuspendDto suspendDto) {
        UserDto dto = adminService.suspendUser(suspendDto);
        if(dto == null){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(dto);
    }

    @PatchMapping("/tasker/suspend")
    public ResponseEntity<TaskerDto> suspendTaskerJson(@RequestBody SuspendDto suspendDto) {
        TaskerDto dto = adminService.suspendTasker(suspendDto);
        if(dto == null){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(dto);
    }

    @PatchMapping("/user/reactive/{id}")
    public ResponseEntity<UserDto> reactiveUser(@PathVariable("id") Long id) {
        UserDto dto = adminService.reactiveUser(id);
         if(dto == null){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(dto);
    }

    @PatchMapping("/tasker/reactive/{id}")
    public ResponseEntity<TaskerDto> reactiveTasker(@PathVariable("id") Long id) {
        TaskerDto dto = adminService.reactiveTasker(id);
         if(dto == null){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(dto);
    }

    @PatchMapping("/user/promote/{id}")
    public ResponseEntity<UserDto> promoteUser(@PathVariable("id") Long id) {
        UserDto dto = adminService.promoteUser(id);
         if(dto == null){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(dto);
    }

    @PatchMapping("/user/demote/{id}")
    public ResponseEntity<UserDto> demoteUser(@PathVariable("id") Long id) {
        UserDto dto = adminService.demoteUser(id);
        if(dto == null){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(dto);
    }





}