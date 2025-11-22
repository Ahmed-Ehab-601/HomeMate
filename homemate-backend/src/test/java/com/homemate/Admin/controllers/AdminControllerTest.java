package com.homemate.Admin.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.homemate.Admin.domain.dto.*;
import com.homemate.Admin.services.IAdminService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;

import static com.homemate.Admin.TestDataUtil.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc(addFilters = false)

@ExtendWith(MockitoExtension.class)
@WebMvcTest(AdminController.class)
@DisplayName("AdminController Integration Tests")
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IAdminService adminService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testGetUsers_Returns200WithUserList() throws Exception {

        PageResponse<UserDto> mockResponse = createMockUserPageResponse();
        when(adminService.getUsers(any(UserFilterDto.class), any(PageRequest.class)))
                .thenReturn(mockResponse);


        mockMvc.perform(get("/api/admin/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items.length()").value(2))
                .andExpect(jsonPath("$.totalItems").value(2L))
                .andExpect(jsonPath("$.totalPages").value(1L));
    }

    @Test
    @DisplayName("GET /admin/users - Should accept filter parameters")
    void testGetUsers_WithFilters_AppliesFilters() throws Exception {
        PageResponse<UserDto> mockResponse = createMockUserPageResponse();
        when(adminService.getUsers(any(UserFilterDto.class), any(PageRequest.class)))
                .thenReturn(mockResponse);

        mockMvc.perform(get("/api/admin/users")
                        .param("admin", "true")
                        .param("suspended", "false")
                        .param("username","adm")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray());
    }

    @Test
    @DisplayName("GET /admin/users - Should handle pagination parameters")
    void testGetUsers_WithPagination_AppliesPagination() throws Exception {

        PageResponse<UserDto> mockResponse = createMockUserPageResponse();
        when(adminService.getUsers(any(UserFilterDto.class), any(PageRequest.class)))
                .thenReturn(mockResponse);


        mockMvc.perform(get("/api/admin/users")
                        .param("page", "1")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").exists());
    }
    @Test
    @DisplayName("GET api/admin/user")
    void testSuspendUser() throws Exception {

        UserDto userDto=UserDto.builder()
                .suspended(true)
                .build();
        SuspendDto suspendDto=SuspendDto.builder()
                .userId(1L)
                .reason("I do not like his personality")
                .build();

        when(adminService.suspendUser(any(SuspendDto.class)))
                .thenReturn(userDto);

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch("/api/admin/user/suspend")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(suspendDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.suspended").value(true));
    }
    @Test
    @DisplayName("Patch api/admin/tasker")
    void testSuspendTasker() throws Exception {
        TaskerDto taskerDto=TaskerDto.builder()
                .suspended(true)
                .build();
        SuspendDto suspendDto=SuspendDto.builder()
                .userId(1L)
                .reason("I do not like his personality")
                .build();

        when(adminService.suspendTasker(any(SuspendDto.class)))
                .thenReturn(taskerDto);

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch("/api/admin/tasker/suspend")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(suspendDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.suspended").value(true));
    }

    @Test
    @DisplayName("PATCH /api/admin/user/promote - promote user to admin")
    void testPromoteUser() throws Exception {
        UserDto promoted = UserDto.builder()
                .userId(1L)
                .admin(true)
                .build();

        when(adminService.promoteUser(1L)).thenReturn(promoted);

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch("/api/admin/user/promote/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.admin").value(true));
    }

    @Test
    @DisplayName("PATCH /api/admin/user/demote - demote user from admin")
    void testDemoteUser() throws Exception {
        UserDto demoted = UserDto.builder()
                .userId(2L)
                .admin(false)
                .build();

        when(adminService.demoteUser(2L)).thenReturn(demoted);

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch("/api/admin/user/demote/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.admin").value(false));
    }

    @Test
    @DisplayName("PATCH /api/admin/user/reactive/{id} - reactivate user")
    void testReactiveUser() throws Exception {
        UserDto active = UserDto.builder()
                .userId(3L)
                .suspended(false)
                .build();

        when(adminService.reactiveUser(3L)).thenReturn(active);

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch("/api/admin/user/reactive/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.suspended").value(false));
    }

    @Test
    @DisplayName("PATCH /api/admin/tasker/reactive/{id} - reactivate tasker")
    void testReactiveTasker() throws Exception {
        TaskerDto active = TaskerDto.builder()
                .taskerID(4L)
                .suspended(false)
                .build();

        when(adminService.reactiveTasker(4L)).thenReturn(active);

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch("/api/admin/tasker/reactive/4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.suspended").value(false));
    }

    @Test
    @DisplayName("GET /admin/taskers - Should return 200 with tasker list")
    void testGetTaskers_Returns200WithTaskerList() throws Exception {
        PageResponse<TaskerDto> mockResponse = createMockTaskerPageResponse();
        when(adminService.getTaskers(any(TaskerFilterDto.class), any(PageRequest.class)))
                .thenReturn(mockResponse);

        mockMvc.perform(get("/api/admin/taskers")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items.length()").value(2))
                .andExpect(jsonPath("$.totalItems").value(2L))
                .andExpect(jsonPath("$.totalPages").value(1L));
    }

    @Test
    @DisplayName("GET /admin/taskers - Should accept filter parameters")
    void testGetTaskers_WithFilters_AppliesFilters() throws Exception {
        PageResponse<TaskerDto> mockResponse = createMockTaskerPageResponse();
        when(adminService.getTaskers(any(TaskerFilterDto.class), any(PageRequest.class)))
                .thenReturn(mockResponse);

        mockMvc.perform(get("/api/admin/taskers")
                        .param("suspended", "false")
                        .param("minRate", "4.6")
                        .param("username","tasker")
                )

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray());
    }
    @Test
    @DisplayName("GET /admin/taskers - Should handle pagination parameters")
    void testGetTaskers_WithPagination_AppliesPagination() throws Exception {

        PageResponse<TaskerDto> mockResponse = createMockTaskerPageResponse();
        when(adminService.getTaskers(any(TaskerFilterDto.class), any(PageRequest.class)))
                .thenReturn(mockResponse);

        mockMvc.perform(get("/api/admin/taskers")
                        .param("page", "1")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").exists());
    }
    @Test
    @DisplayName("GET /admin/users - Should return empty list when no users")
    void testGetUsers_WhenNoUsers_ReturnsEmptyList() throws Exception {

        PageResponse<UserDto> emptyResponse = PageResponse.<UserDto>builder()
                .items(new ArrayList<>())
                .totalItems(0L)
                .totalPages(0L)
                .build();
        when(adminService.getUsers(any(UserFilterDto.class), any(PageRequest.class)))
                .thenReturn(emptyResponse);

        mockMvc.perform(get("/api/admin/users")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items.length()").value(0))
                .andExpect(jsonPath("$.totalItems").value(0L));
    }

    @Test
    @DisplayName("GET /admin/taskers - Should return empty list when no users")
    void testGetTaskers_WhenNoUsers_ReturnsEmptyList() throws Exception {

        PageResponse<TaskerDto> emptyResponse = PageResponse.<TaskerDto>builder()
                .items(new ArrayList<>())
                .totalItems(0L)
                .totalPages(0L)
                .build();
        when(adminService.getTaskers(any(TaskerFilterDto.class), any(PageRequest.class)))
                .thenReturn(emptyResponse);

        mockMvc.perform(get("/api/admin/taskers")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items.length()").value(0))
                .andExpect(jsonPath("$.totalItems").value(0L));
    }



}


