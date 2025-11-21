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
import static com.homemate.Admin.TestDataUtil.createMockTaskerPageResponse;
import static com.homemate.Admin.TestDataUtil.createMockUserPageResponse;
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


        mockMvc.perform(get("/admin/users")
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

        mockMvc.perform(get("/admin/users")
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
        // Given
        PageResponse<UserDto> mockResponse = createMockUserPageResponse();
        when(adminService.getUsers(any(UserFilterDto.class), any(PageRequest.class)))
                .thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(get("/admin/users")
                        .param("page", "1")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").exists());
    }

    @Test
    @DisplayName("GET /admin/taskers - Should return 200 with tasker list")
    void testGetTaskers_Returns200WithTaskerList() throws Exception {
        // Given
        PageResponse<TaskerDto> mockResponse = createMockTaskerPageResponse();
        when(adminService.getTaskers(any(TaskerFilterDto.class), any(PageRequest.class)))
                .thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(get("/admin/taskers")
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

        mockMvc.perform(get("/admin/taskers")
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

        mockMvc.perform(get("/admin/taskers")
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

        mockMvc.perform(get("/admin/users")
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

        mockMvc.perform(get("/admin/taskers")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items.length()").value(0))
                .andExpect(jsonPath("$.totalItems").value(0L));
    }



}


