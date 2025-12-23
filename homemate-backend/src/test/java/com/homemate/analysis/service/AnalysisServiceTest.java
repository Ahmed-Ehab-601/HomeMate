// package com.homemate.analysis.service;

// import com.homemate.analysis.dao.AnalysisDao;
// import com.homemate.analysis.dto.*;

// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.junit.jupiter.MockitoExtension;

// import java.util.HashMap;
// import java.util.Map;

// import static org.junit.jupiter.api.Assertions.*;
// import static org.mockito.Mockito.*;

// @ExtendWith(MockitoExtension.class)
// class AnalysisServiceTest {

//     @Mock
//     private AnalysisDao analysisDao;

//     @InjectMocks
//     private AnalysisServiceImp analysisService;

//     private UserAnalysisDto mockUserAnalysis;
//     private TaskerAnalysisDto mockTaskerAnalysis;
//     private TaskAnalysisDto mockTaskAnalysis;

//     @BeforeEach
//     void setUp() {
//         // Setup mock User Analysis
//         Map<String, Integer> genderDist = new HashMap<>();
//         genderDist.put("M", 50);
//         genderDist.put("F", 45);
//         genderDist.put("Unknown", 5);

//         Map<String, Integer> ageDist = new HashMap<>();
//         ageDist.put("25", 10);
//         ageDist.put("30", 25);
//         ageDist.put("35", 20);

//         mockUserAnalysis = UserAnalysisDto.builder()
//                 .genderDistribution(genderDist)
//                 .ageDistribution(ageDist)
//                 .suspendedUsersCount(5)
//                 .averageTasksPerUser(2.5)
//                 .usersWithNoCompletedTasksCount(10)
//                 .build();

//         // Setup mock Tasker Analysis
//         Map<String, Integer> taskersPerService = new HashMap<>();
//         taskersPerService.put("Plumbing", 15);
//         taskersPerService.put("Electrical", 10);
//         taskersPerService.put("Cleaning", 20);

//         Map<String, Integer> availabilityDist = new HashMap<>();
//         availabilityDist.put("available", 35);
//         availabilityDist.put("unavailable", 10);

//         mockTaskerAnalysis = TaskerAnalysisDto.builder()
//                 .taskersPerService(taskersPerService)
//                 .availabilityDistribution(availabilityDist)
//                 .build();

//         // Setup mock Task Analysis
//         Map<String, Integer> statusOverview = new HashMap<>();
//         statusOverview.put("Done", 100);
//         statusOverview.put("InProgress", 25);
//         statusOverview.put("Accepted", 15);
//         statusOverview.put("InReview", 10);

//         Map<String, Integer> tasksPerService = new HashMap<>();
//         tasksPerService.put("Plumbing", 50);
//         tasksPerService.put("Electrical", 40);
//         tasksPerService.put("Cleaning", 60);

//         Map<String, Double> avgBillPerService = new HashMap<>();
//         avgBillPerService.put("Plumbing", 75.50);
//         avgBillPerService.put("Electrical", 120.00);
//         avgBillPerService.put("Cleaning", 50.00);

//         Map<Integer, Integer> peakHours = new HashMap<>();
//         peakHours.put(9, 20);
//         peakHours.put(10, 30);
//         peakHours.put(14, 25);

//         mockTaskAnalysis = TaskAnalysisDto.builder()
//                 .statusOverview(statusOverview)
//                 .tasksPerService(tasksPerService)
//                 .averageBillPerService(avgBillPerService)
//                 .averageTaskDurationHours(2.5)
//                 .peakCreationHours(peakHours)
//                 .tasksWithReportsCount(10)
//                 .tasksWithDelayedCompletionCount(5)
//                 .build();
//     }

//     @Test
//     void testGetAnalysis_Success() {
//         // Arrange
//         when(analysisDao.getUserAnalysis()).thenReturn(mockUserAnalysis);
//         when(analysisDao.getTaskerAnalysis()).thenReturn(mockTaskerAnalysis);
//         when(analysisDao.getTaskAnalysis()).thenReturn(mockTaskAnalysis);

//         // Act
//         AnalysisResponse result = analysisService.getAnalysis();

//         // Assert
//         assertNotNull(result);
//         assertNotNull(result.getUserAnalysis());
//         assertNotNull(result.getTaskerAnalysis());
//         assertNotNull(result.getTaskAnalysis());
        
//         verify(analysisDao, times(1)).getUserAnalysis();
//         verify(analysisDao, times(1)).getTaskerAnalysis();
//         verify(analysisDao, times(1)).getTaskAnalysis();
//     }

//     @Test
//     void testGetAnalysis_UserAnalysisContent() {
//         // Arrange
//         when(analysisDao.getUserAnalysis()).thenReturn(mockUserAnalysis);
//         when(analysisDao.getTaskerAnalysis()).thenReturn(mockTaskerAnalysis);
//         when(analysisDao.getTaskAnalysis()).thenReturn(mockTaskAnalysis);

//         // Act
//         AnalysisResponse result = analysisService.getAnalysis();
//         UserAnalysisDto userAnalysis = result.getUserAnalysis();

//         // Assert
//         assertEquals(50, userAnalysis.getGenderDistribution().get("M"));
//         assertEquals(45, userAnalysis.getGenderDistribution().get("F"));
//         assertEquals(5, userAnalysis.getSuspendedUsersCount());
//         assertEquals(2.5, userAnalysis.getAverageTasksPerUser());
//         assertEquals(10, userAnalysis.getUsersWithNoCompletedTasksCount());
//     }

//     @Test
//     void testGetAnalysis_TaskerAnalysisContent() {
//         // Arrange
//         when(analysisDao.getUserAnalysis()).thenReturn(mockUserAnalysis);
//         when(analysisDao.getTaskerAnalysis()).thenReturn(mockTaskerAnalysis);
//         when(analysisDao.getTaskAnalysis()).thenReturn(mockTaskAnalysis);

//         // Act
//         AnalysisResponse result = analysisService.getAnalysis();
//         TaskerAnalysisDto taskerAnalysis = result.getTaskerAnalysis();

//         // Assert
//         assertEquals(15, taskerAnalysis.getTaskersPerService().get("Plumbing"));
//         assertEquals(10, taskerAnalysis.getTaskersPerService().get("Electrical"));
//         assertEquals(20, taskerAnalysis.getTaskersPerService().get("Cleaning"));
//         assertEquals(35, taskerAnalysis.getAvailabilityDistribution().get("available"));
//         assertEquals(10, taskerAnalysis.getAvailabilityDistribution().get("unavailable"));
//     }

//     @Test
//     void testGetAnalysis_TaskAnalysisContent() {
//         // Arrange
//         when(analysisDao.getUserAnalysis()).thenReturn(mockUserAnalysis);
//         when(analysisDao.getTaskerAnalysis()).thenReturn(mockTaskerAnalysis);
//         when(analysisDao.getTaskAnalysis()).thenReturn(mockTaskAnalysis);

//         // Act
//         AnalysisResponse result = analysisService.getAnalysis();
//         TaskAnalysisDto taskAnalysis = result.getTaskAnalysis();

//         // Assert
//         assertEquals(100, taskAnalysis.getStatusOverview().get("Done"));
//         assertEquals(25, taskAnalysis.getStatusOverview().get("InProgress"));
//         assertEquals(50, taskAnalysis.getTasksPerService().get("Plumbing"));
//         assertEquals(75.50, taskAnalysis.getAverageBillPerService().get("Plumbing"));
//         assertEquals(2.5, taskAnalysis.getAverageTaskDurationHours());
//         assertEquals(10, taskAnalysis.getTasksWithReportsCount());
//     }

//     @Test
//     void testGetAnalysis_DaoCalledOnce() {
//         // Arrange
//         when(analysisDao.getUserAnalysis()).thenReturn(mockUserAnalysis);
//         when(analysisDao.getTaskerAnalysis()).thenReturn(mockTaskerAnalysis);
//         when(analysisDao.getTaskAnalysis()).thenReturn(mockTaskAnalysis);

//         // Act
//         analysisService.getAnalysis();

//         // Assert - Each DAO method called exactly once
//         verify(analysisDao, times(1)).getUserAnalysis();
//         verify(analysisDao, times(1)).getTaskerAnalysis();
//         verify(analysisDao, times(1)).getTaskAnalysis();
//     }

//     @Test
//     void testGetAnalysis_WithEmptyData() {
//         // Arrange - Empty DTOs
//         UserAnalysisDto emptyUserAnalysis = UserAnalysisDto.builder()
//                 .genderDistribution(new HashMap<>())
//                 .ageDistribution(new HashMap<>())
//                 .suspendedUsersCount(0)
//                 .averageTasksPerUser(0.0)
//                 .usersWithNoCompletedTasksCount(0)
//                 .build();

//         TaskerAnalysisDto emptyTaskerAnalysis = TaskerAnalysisDto.builder()
//                 .taskersPerService(new HashMap<>())
//                 .availabilityDistribution(new HashMap<>())
//                 .build();

//         TaskAnalysisDto emptyTaskAnalysis = TaskAnalysisDto.builder()
//                 .statusOverview(new HashMap<>())
//                 .tasksPerService(new HashMap<>())
//                 .averageBillPerService(new HashMap<>())
//                 .averageTaskDurationHours(0.0)
//                 .peakCreationHours(new HashMap<>())
//                 .tasksWithReportsCount(0)
//                 .tasksWithDelayedCompletionCount(0)
//                 .build();

//         when(analysisDao.getUserAnalysis()).thenReturn(emptyUserAnalysis);
//         when(analysisDao.getTaskerAnalysis()).thenReturn(emptyTaskerAnalysis);
//         when(analysisDao.getTaskAnalysis()).thenReturn(emptyTaskAnalysis);

//         // Act
//         AnalysisResponse result = analysisService.getAnalysis();

//         // Assert - Should return valid response with empty data
//         assertNotNull(result);
//         assertTrue(result.getUserAnalysis().getGenderDistribution().isEmpty());
//         assertTrue(result.getTaskerAnalysis().getTaskersPerService().isEmpty());
//         assertTrue(result.getTaskAnalysis().getStatusOverview().isEmpty());
//         assertEquals(0, result.getTaskAnalysis().getTasksWithReportsCount());
//     }

//     @Test
//     void testGetAnalysis_GenderDistributionUnknownHandling() {
//         // Arrange
//         when(analysisDao.getUserAnalysis()).thenReturn(mockUserAnalysis);
//         when(analysisDao.getTaskerAnalysis()).thenReturn(mockTaskerAnalysis);
//         when(analysisDao.getTaskAnalysis()).thenReturn(mockTaskAnalysis);

//         // Act
//         AnalysisResponse result = analysisService.getAnalysis();
//         UserAnalysisDto userAnalysis = result.getUserAnalysis();

//         // Assert - Should include unknown gender
//         assertTrue(userAnalysis.getGenderDistribution().containsKey("Unknown"));
//         assertEquals(5, userAnalysis.getGenderDistribution().get("Unknown"));
//     }

//     @Test
//     void testGetAnalysis_PeakHoursContent() {
//         // Arrange
//         when(analysisDao.getUserAnalysis()).thenReturn(mockUserAnalysis);
//         when(analysisDao.getTaskerAnalysis()).thenReturn(mockTaskerAnalysis);
//         when(analysisDao.getTaskAnalysis()).thenReturn(mockTaskAnalysis);

//         // Act
//         AnalysisResponse result = analysisService.getAnalysis();
//         Map<Integer, Integer> peakHours = result.getTaskAnalysis().getPeakCreationHours();

//         // Assert
//         assertNotNull(peakHours);
//         assertEquals(3, peakHours.size());
//         assertEquals(20, peakHours.get(9));
//         assertEquals(30, peakHours.get(10));
//         assertEquals(25, peakHours.get(14));
//     }
// }
