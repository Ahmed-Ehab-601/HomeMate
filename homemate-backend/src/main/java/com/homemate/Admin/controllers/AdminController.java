package com.homemate.Admin.controllers;

import com.homemate.Admin.domain.dto.*;
import com.homemate.Admin.services.IAdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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
        return ResponseEntity.ok(dto);
    }

    @PatchMapping("/tasker/suspend")
    public ResponseEntity<TaskerDto> suspendTaskerJson(@RequestBody SuspendDto suspendDto) {
        TaskerDto dto = adminService.suspendTasker(suspendDto);
        return ResponseEntity.ok(dto);
    }

    @PatchMapping("/user/reactive/{id}")
    public ResponseEntity<UserDto> reactiveUser(@PathVariable("id") Long id) {
        UserDto dto = adminService.reactiveUser(id);
        return ResponseEntity.ok(dto);
    }

    @PatchMapping("/tasker/reactive/{id}")
    public ResponseEntity<TaskerDto> reactiveTasker(@PathVariable("id") Long id) {
        TaskerDto dto = adminService.reactiveTasker(id);
        return ResponseEntity.ok(dto);
    }

    @PatchMapping("/user/promote/{id}")
    public ResponseEntity<UserDto> promoteUser(@PathVariable("id") Long id) {
        UserDto dto = adminService.promoteUser(id);
        return ResponseEntity.ok(dto);
    }

    @PatchMapping("/user/demote/{id}")
    public ResponseEntity<UserDto> demoteUser(@PathVariable("id") Long id) {
        UserDto dto = adminService.demoteUser(id);
        return ResponseEntity.ok(dto);
    }





}