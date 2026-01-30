package com.homemate.analysis.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.homemate.analysis.dto.*;
import com.homemate.analysis.service.TaskAnalysisService;
import com.homemate.security.filter.JwtAuthFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TaskAnalysisController.class,
    excludeAutoConfiguration = {
        SecurityAutoConfiguration.class,
        SecurityFilterAutoConfiguration.class,
        org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientAutoConfiguration.class,
        org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientWebSecurityAutoConfiguration.class,
        org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration.class
    })
@AutoConfigureMockMvc(addFilters = false)
class TaskAnalysisControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TaskAnalysisService taskAnalysisService;

    @MockitoBean
    private JwtAuthFilter jwtAuthFilter;

    @Test
    void getBillRanges_returnsOk() throws Exception {
        BillRangesResponse resp = new BillRangesResponse(Map.of("0-10", 2L, "10-20", 3L));
        when(taskAnalysisService.getBillRanges()).thenReturn(resp);

        mockMvc.perform(get("/api/analysis/task/bill-ranges"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ranges['0-10']").value(2))
                .andExpect(jsonPath("$.ranges['10-20']").value(3));
    }

    @Test
    void postStartDateRanges_returnsOk() throws Exception {
        TaskDateRangesRequest req = new TaskDateRangesRequest(
                List.of(new TimeRange(LocalDateTime.now().minusDays(1), LocalDateTime.now())));
        TaskDateRangesResponse resp = new TaskDateRangesResponse(List.of(4L));
        when(taskAnalysisService.getStartDateRanges(org.mockito.ArgumentMatchers.any())).thenReturn(resp);

        mockMvc.perform(post("/api/analysis/task/start-date-ranges")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.counts[0]").value(4));
    }
}
