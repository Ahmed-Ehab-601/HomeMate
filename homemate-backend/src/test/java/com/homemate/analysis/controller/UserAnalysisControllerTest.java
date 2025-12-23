package com.homemate.analysis.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.homemate.analysis.dto.*;
import com.homemate.analysis.service.UserAnalysisService;
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

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserAnalysisController.class,
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                SecurityFilterAutoConfiguration.class,
                org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientAutoConfiguration.class,
                org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientWebSecurityAutoConfiguration.class,
                org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration.class
        })
@AutoConfigureMockMvc(addFilters = false)
class UserAnalysisControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserAnalysisService userAnalysisService;

    @MockitoBean
    private JwtAuthFilter jwtAuthFilter;

    @Test
    void getGenderCount_returnsOkWithPayload() throws Exception {
        when(userAnalysisService.getGenderCount()).thenReturn(new GenderCountResponse(3, 5));

        mockMvc.perform(get("/api/analysis/user/gender-count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.male").value(3))
                .andExpect(jsonPath("$.female").value(5));
    }

    @Test
    void postNewAccounts_returnsOk() throws Exception {
        NewAccountsRequest req = new NewAccountsRequest(
                List.of(new TimeRange(LocalDateTime.now().minusDays(7), LocalDateTime.now())));
        when(userAnalysisService.getNewAccountsCounts(org.mockito.ArgumentMatchers.any()))
                .thenReturn(new NewAccountsResponse(List.of(10L)));

        mockMvc.perform(post("/api/analysis/user/new-accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf())
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.counts[0]").value(10));
    }
}
