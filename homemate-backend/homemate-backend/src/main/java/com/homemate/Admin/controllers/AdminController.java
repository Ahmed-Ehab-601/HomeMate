package com.homemate.Admin.controllers;

import com.homemate.Admin.domain.dto.*;
import com.homemate.Admin.services.IAdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
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




}