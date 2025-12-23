package com.homemate.analysis.controller;

import com.homemate.analysis.dto.*;
import com.homemate.analysis.service.TaskerAnalysisService;
import com.homemate.security.filter.JwtAuthFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TaskerAnalysisController.class,
    excludeAutoConfiguration = {
        SecurityAutoConfiguration.class,
        SecurityFilterAutoConfiguration.class,
        org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientAutoConfiguration.class,
        org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientWebSecurityAutoConfiguration.class,
        org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration.class
    })
@AutoConfigureMockMvc(addFilters = false)
class TaskerAnalysisControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TaskerAnalysisService taskerAnalysisService;

    @MockBean
    private JwtAuthFilter jwtAuthFilter;

    @Test
    void getRatingRanges_returnsOkWithPayload() throws Exception {
        RatingRangesResponse resp = new RatingRangesResponse(
                java.util.Map.of("0-1", 1L, "1-2", 2L));
        when(taskerAnalysisService.getRatingRanges()).thenReturn(resp);

        mockMvc.perform(get("/api/analysis/tasker/rating-ranges"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ranges['0-1']").value(1))
                .andExpect(jsonPath("$.ranges['1-2']").value(2));
    }

    @Test
    void postServiceCounts_returnsOk() throws Exception {
        ServiceCountRequest req = new ServiceCountRequest(List.of(1L, 2L));
        ServiceCountResponse resp = new ServiceCountResponse(java.util.Map.of(1L, 5L, 2L, 7L));
        when(taskerAnalysisService.getServiceCounts(org.mockito.ArgumentMatchers.any())).thenReturn(resp);

        mockMvc.perform(post("/api/analysis/tasker/service-counts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf())
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.counts['1']").value(5))
                .andExpect(jsonPath("$.counts['2']").value(7));
    }
}
