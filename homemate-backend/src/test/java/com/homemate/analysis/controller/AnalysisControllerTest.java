// package com.homemate.analysis.controller;

// import com.homemate.analysis.dto.*;
// import com.homemate.analysis.service.IAnalysisService;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.boot.test.mock.mockito.MockBean;
// import org.springframework.http.MediaType;
// import org.springframework.security.test.context.support.WithMockUser;
// import org.springframework.test.web.servlet.MockMvc;

// import java.util.HashMap;
// import java.util.Map;

// import static org.mockito.Mockito.*;
// import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
// import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// @SpringBootTest
// @AutoConfigureMockMvc
// class AnalysisControllerTest {

//     @Autowired
//     private MockMvc mockMvc;

//     @MockBean
//     private IAnalysisService analysisService;

//     private AnalysisResponse mockAnalysisResponse;

//     @BeforeEach
//     void setUp() {
//         // Setup mock User Analysis
//         Map<String, Integer> genderDist = new HashMap<>();
//         genderDist.put("M", 50);
//         genderDist.put("F", 45);

//         Map<String, Integer> ageDist = new HashMap<>();
//         ageDist.put("30", 25);

//         UserAnalysisDto userAnalysis = UserAnalysisDto.builder()
//                 .genderDistribution(genderDist)
//                 .ageDistribution(ageDist)
//                 .suspendedUsersCount(5)
//                 .averageTasksPerUser(2.5)
//                 .usersWithNoCompletedTasksCount(10)
//                 .build();

//         // Setup mock Tasker Analysis
//         Map<String, Integer> taskersPerService = new HashMap<>();
//         taskersPerService.put("Plumbing", 15);

//         Map<String, Integer> availabilityDist = new HashMap<>();
//         availabilityDist.put("available", 35);

//         TaskerAnalysisDto taskerAnalysis = TaskerAnalysisDto.builder()
//                 .taskersPerService(taskersPerService)
//                 .availabilityDistribution(availabilityDist)
//                 .build();

//         // Setup mock Task Analysis
//         Map<String, Integer> statusOverview = new HashMap<>();
//         statusOverview.put("Done", 100);

//         Map<String, Integer> tasksPerService = new HashMap<>();
//         tasksPerService.put("Plumbing", 50);

//         Map<String, Double> avgBillPerService = new HashMap<>();
//         avgBillPerService.put("Plumbing", 75.50);

//         Map<Integer, Integer> peakHours = new HashMap<>();
//         peakHours.put(9, 20);

//         TaskAnalysisDto taskAnalysis = TaskAnalysisDto.builder()
//                 .statusOverview(statusOverview)
//                 .tasksPerService(tasksPerService)
//                 .averageBillPerService(avgBillPerService)
//                 .averageTaskDurationHours(2.5)
//                 .peakCreationHours(peakHours)
//                 .tasksWithReportsCount(10)
//                 .tasksWithDelayedCompletionCount(5)
//                 .build();

//         mockAnalysisResponse = AnalysisResponse.builder()
//                 .userAnalysis(userAnalysis)
//                 .taskerAnalysis(taskerAnalysis)
//                 .taskAnalysis(taskAnalysis)
//                 .build();
//     }

//     @Test
//     @WithMockUser(roles = "ADMIN")
//     void testGetAnalysis_AsAdmin_Success() throws Exception {
//         // Arrange
//         when(analysisService.getAnalysis()).thenReturn(mockAnalysisResponse);

//         // Act & Assert
//         mockMvc.perform(get("/api/analysis")
//                         .contentType(MediaType.APPLICATION_JSON))
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$.userAnalysis").exists())
//                 .andExpect(jsonPath("$.taskerAnalysis").exists())
//                 .andExpect(jsonPath("$.taskAnalysis").exists())
//                 .andExpect(jsonPath("$.userAnalysis.genderDistribution.M").value(50))
//                 .andExpect(jsonPath("$.userAnalysis.genderDistribution.F").value(45))
//                 .andExpect(jsonPath("$.userAnalysis.suspendedUsersCount").value(5))
//                 .andExpect(jsonPath("$.taskerAnalysis.taskersPerService.Plumbing").value(15))
//                 .andExpect(jsonPath("$.taskAnalysis.statusOverview.Done").value(100));

//         verify(analysisService, times(1)).getAnalysis();
//     }

//     @Test
//     @WithMockUser(roles = "USER")
//     void testGetAnalysis_AsUser_Forbidden() throws Exception {
//         // Act & Assert - User role should not have access
//         mockMvc.perform(get("/api/analysis")
//                         .contentType(MediaType.APPLICATION_JSON))
//                 .andExpect(status().isForbidden());

//         verify(analysisService, never()).getAnalysis();
//     }

//     @Test
//     @WithMockUser(roles = "TASKER")
//     void testGetAnalysis_AsTasker_Forbidden() throws Exception {
//         // Act & Assert - Tasker role should not have access
//         mockMvc.perform(get("/api/analysis")
//                         .contentType(MediaType.APPLICATION_JSON))
//                 .andExpect(status().isForbidden());

//         verify(analysisService, never()).getAnalysis();
//     }

//     @Test
//     void testGetAnalysis_Unauthenticated_Unauthorized() throws Exception {
//         // Act & Assert - No authentication should fail
//         mockMvc.perform(get("/api/analysis")
//                         .contentType(MediaType.APPLICATION_JSON))
//                 .andExpect(status().isUnauthorized());

//         verify(analysisService, never()).getAnalysis();
//     }

//     @Test
//     @WithMockUser(roles = "ADMIN")
//     void testGetAnalysis_ResponseContainsAllSections() throws Exception {
//         // Arrange
//         when(analysisService.getAnalysis()).thenReturn(mockAnalysisResponse);

//         // Act & Assert
//         mockMvc.perform(get("/api/analysis")
//                         .contentType(MediaType.APPLICATION_JSON))
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$.userAnalysis.genderDistribution").exists())
//                 .andExpect(jsonPath("$.userAnalysis.ageDistribution").exists())
//                 .andExpect(jsonPath("$.userAnalysis.suspendedUsersCount").exists())
//                 .andExpect(jsonPath("$.userAnalysis.averageTasksPerUser").exists())
//                 .andExpect(jsonPath("$.userAnalysis.usersWithNoCompletedTasksCount").exists())
//                 .andExpect(jsonPath("$.taskerAnalysis.taskersPerService").exists())
//                 .andExpect(jsonPath("$.taskerAnalysis.availabilityDistribution").exists())
//                 .andExpect(jsonPath("$.taskAnalysis.statusOverview").exists())
//                 .andExpect(jsonPath("$.taskAnalysis.tasksPerService").exists())
//                 .andExpect(jsonPath("$.taskAnalysis.averageBillPerService").exists())
//                 .andExpect(jsonPath("$.taskAnalysis.averageTaskDurationHours").exists())
//                 .andExpect(jsonPath("$.taskAnalysis.peakCreationHours").exists())
//                 .andExpect(jsonPath("$.taskAnalysis.tasksWithReportsCount").exists());
//     }

//     @Test
//     @WithMockUser(roles = "ADMIN")
//     void testGetAnalysis_NumericValuesCorrect() throws Exception {
//         // Arrange
//         when(analysisService.getAnalysis()).thenReturn(mockAnalysisResponse);

//         // Act & Assert
//         mockMvc.perform(get("/api/analysis")
//                         .contentType(MediaType.APPLICATION_JSON))
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$.userAnalysis.averageTasksPerUser").value(2.5))
//                 .andExpect(jsonPath("$.taskAnalysis.averageTaskDurationHours").value(2.5))
//                 .andExpect(jsonPath("$.taskAnalysis.averageBillPerService.Plumbing").value(75.50))
//                 .andExpect(jsonPath("$.taskAnalysis.tasksWithReportsCount").value(10));
//     }
// }
