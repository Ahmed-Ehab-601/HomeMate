// package com.homemate.analysis.dao;

// import com.homemate.analysis.dto.TaskAnalysisDto;
// import com.homemate.analysis.dto.TaskerAnalysisDto;
// import com.homemate.analysis.dto.UserAnalysisDto;
// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.test.context.ActiveProfiles;
// import org.springframework.transaction.annotation.Transactional;

// import java.util.Map;

// import static org.junit.jupiter.api.Assertions.*;

// @SpringBootTest
// @Transactional
// @ActiveProfiles("analysis")
// class AnalysisDaoTest {

//     @Autowired
//     private AnalysisDaoImp analysisDao;

//     // =====================================================
//     // User Analysis Tests
//     // =====================================================

//     @Test
//     void testGetUserAnalysis_ReturnsCompleteDto() {
//         // Act
//         UserAnalysisDto result = analysisDao.getUserAnalysis();

//         // Assert
//         assertNotNull(result);
//         assertNotNull(result.getGenderDistribution());
//         assertNotNull(result.getAgeDistribution());
//         assertNotNull(result.getSuspendedUsersCount());
//         assertNotNull(result.getAverageTasksPerUser());
//         assertNotNull(result.getUsersWithNoCompletedTasksCount());
//     }

//     @Test
//     void testGetUserAnalysis_GenderDistribution() {
//         // Act
//         UserAnalysisDto result = analysisDao.getUserAnalysis();
//         Map<String, Integer> genderDist = result.getGenderDistribution();

//         // Assert - We have 3 males and 3 females in test data
//         assertNotNull(genderDist);
//         assertEquals(3, genderDist.get("M"));
//         assertEquals(3, genderDist.get("F"));
//     }

//     @Test
//     void testGetUserAnalysis_AgeDistribution() {
//         // Act
//         UserAnalysisDto result = analysisDao.getUserAnalysis();
//         Map<String, Integer> ageDist = result.getAgeDistribution();

//         // Assert - Should have age entries based on birthDates in test data
//         assertNotNull(ageDist);
//         assertFalse(ageDist.isEmpty());
//         // All users have birthdates so distribution should be calculated
//         assertTrue(ageDist.values().stream().allMatch(count -> count > 0));
//     }

//     @Test
//     void testGetUserAnalysis_SuspendedUsersCount() {
//         // Act
//         UserAnalysisDto result = analysisDao.getUserAnalysis();

//         // Assert - We have 2 suspended users (userID 3 and 6)
//         assertEquals(2, result.getSuspendedUsersCount());
//     }

//     @Test
//     void testGetUserAnalysis_AverageTasksPerUser() {
//         // Act
//         UserAnalysisDto result = analysisDao.getUserAnalysis();

//         // Assert - 10 tasks / 6 users = ~1.67
//         assertNotNull(result.getAverageTasksPerUser());
//         assertTrue(result.getAverageTasksPerUser() > 0);
//         assertEquals(10.0 / 6.0, result.getAverageTasksPerUser(), 0.01);
//     }

//     @Test
//     void testGetUserAnalysis_UsersWithNoCompletedTasksCount() {
//         // Act
//         UserAnalysisDto result = analysisDao.getUserAnalysis();

//         // Assert - Users 3, 5, 6 have no completed tasks (Done status)
//         // Users 1, 2, 4 have completed tasks
//         assertNotNull(result.getUsersWithNoCompletedTasksCount());
//         assertEquals(3, result.getUsersWithNoCompletedTasksCount());
//     }

//     // =====================================================
//     // Tasker Analysis Tests
//     // =====================================================

//     @Test
//     void testGetTaskerAnalysis_ReturnsCompleteDto() {
//         // Act
//         TaskerAnalysisDto result = analysisDao.getTaskerAnalysis();

//         // Assert
//         assertNotNull(result);
//         assertNotNull(result.getTaskersPerService());
//         assertNotNull(result.getAvailabilityDistribution());
//     }

//     @Test
//     void testGetTaskerAnalysis_TaskersPerService() {
//         // Act
//         TaskerAnalysisDto result = analysisDao.getTaskerAnalysis();
//         Map<String, Integer> taskersPerService = result.getTaskersPerService();

//         // Assert - Based on test data:
//         // Plumbing: 2 taskers (Mike, James)
//         // Electrical: 1 tasker (Sarah)
//         // Cleaning: 1 tasker (Tom)
//         // Painting: 1 tasker (Emma)
//         assertNotNull(taskersPerService);
//         assertEquals(4, taskersPerService.size());
//         assertEquals(2, taskersPerService.get("Plumbing"));
//         assertEquals(1, taskersPerService.get("Electrical"));
//         assertEquals(1, taskersPerService.get("Cleaning"));
//         assertEquals(1, taskersPerService.get("Painting"));
//     }

//     @Test
//     void testGetTaskerAnalysis_AvailabilityDistribution() {
//         // Act
//         TaskerAnalysisDto result = analysisDao.getTaskerAnalysis();
//         Map<String, Integer> availabilityDist = result.getAvailabilityDistribution();

//         // Assert - 4 available, 1 unavailable
//         assertNotNull(availabilityDist);
//         assertEquals(4, availabilityDist.get("available"));
//         assertEquals(1, availabilityDist.get("unavailable"));
//     }

//     // =====================================================
//     // Task Analysis Tests
//     // =====================================================

//     @Test
//     void testGetTaskAnalysis_ReturnsCompleteDto() {
//         // Act
//         TaskAnalysisDto result = analysisDao.getTaskAnalysis();

//         // Assert
//         assertNotNull(result);
//         assertNotNull(result.getStatusOverview());
//         assertNotNull(result.getTasksPerService());
//         assertNotNull(result.getAverageBillPerService());
//         assertNotNull(result.getAverageTaskDurationHours());
//         assertNotNull(result.getPeakCreationHours());
//         assertNotNull(result.getTasksWithReportsCount());
//     }

//     @Test
//     void testGetTaskAnalysis_StatusOverview() {
//         // Act
//         TaskAnalysisDto result = analysisDao.getTaskAnalysis();
//         Map<String, Integer> statusOverview = result.getStatusOverview();

//         // Assert - Based on test data:
//         // Done: 5, InProgress: 1, Accepted: 1, InReview: 1, Rejected: 1, Suspended: 1
//         assertNotNull(statusOverview);
//         assertEquals(5, statusOverview.get("Done"));
//         assertEquals(1, statusOverview.get("InProgress"));
//         assertEquals(1, statusOverview.get("Accepted"));
//         assertEquals(1, statusOverview.get("InReview"));
//         assertEquals(1, statusOverview.get("Rejected"));
//         assertEquals(1, statusOverview.get("Suspended"));
//     }

//     @Test
//     void testGetTaskAnalysis_TasksPerService() {
//         // Act
//         TaskAnalysisDto result = analysisDao.getTaskAnalysis();
//         Map<String, Integer> tasksPerService = result.getTasksPerService();

//         // Assert - Based on test data:
//         // Plumbing: 5 tasks, Electrical: 3 tasks, Cleaning: 1 task
//         assertNotNull(tasksPerService);
//         assertEquals(5, tasksPerService.get("Plumbing"));
//         assertEquals(3, tasksPerService.get("Electrical"));
//         assertEquals(1, tasksPerService.get("Cleaning"));
//     }

//     @Test
//     void testGetTaskAnalysis_AverageBillPerService() {
//         // Act
//         TaskAnalysisDto result = analysisDao.getTaskAnalysis();
//         Map<String, Double> avgBillPerService = result.getAverageBillPerService();

//         // Assert - Should have entries for services with tasks
//         assertNotNull(avgBillPerService);
//         assertTrue(avgBillPerService.containsKey("Plumbing"));
//         assertTrue(avgBillPerService.containsKey("Electrical"));
//         // Average values should be positive for completed tasks
//         assertTrue(avgBillPerService.get("Plumbing") > 0);
//     }

//     @Test
//     void testGetTaskAnalysis_AverageTaskDuration() {
//         // Act
//         TaskAnalysisDto result = analysisDao.getTaskAnalysis();

//         // Assert - Should have calculated duration for tasks with endDate
//         assertNotNull(result.getAverageTaskDurationHours());
//         // Tasks with both start and end dates have durations between 1.5 to 4 hours
//         assertTrue(result.getAverageTaskDurationHours() >= 0);
//     }

//     @Test
//     void testGetTaskAnalysis_PeakCreationHours() {
//         // Act
//         TaskAnalysisDto result = analysisDao.getTaskAnalysis();
//         Map<Integer, Integer> peakHours = result.getPeakCreationHours();

//         // Assert - Should have entries for hours when tasks were created
//         assertNotNull(peakHours);
//         assertFalse(peakHours.isEmpty());
//         // All values should be positive
//         assertTrue(peakHours.values().stream().allMatch(count -> count > 0));
//     }

//     @Test
//     void testGetTaskAnalysis_TasksWithReportsCount() {
//         // Act
//         TaskAnalysisDto result = analysisDao.getTaskAnalysis();

//         // Assert - We have 3 reports for 3 different tasks in test data
//         assertEquals(3, result.getTasksWithReportsCount());
//     }

//     @Test
//     void testGetTaskAnalysis_TasksWithDelayedCompletionCount() {
//         // Act
//         TaskAnalysisDto result = analysisDao.getTaskAnalysis();

//         // Assert - Currently always returns 0 (not implemented)
//         assertEquals(0, result.getTasksWithDelayedCompletionCount());
//     }
// }
