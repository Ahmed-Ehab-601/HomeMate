package com.homemate.taskmanagementtests;

import com.homemate.TaskerProfile.models.TaskerAvailability;
import com.homemate.security.model.AppUserDetails;
import com.homemate.taskmanagement.dao.TaskRequestDao;
import com.homemate.taskmanagement.dto.TaskDto;
import com.homemate.taskmanagement.service.TaskRequestService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskRequestServiceEstimationAndBusyTimeTest {

    @Mock
    private TaskRequestDao taskRequestDao;

    @Mock
    private com.homemate.taskmanagement.mappers.TaskMapper taskMapper;

    @Mock
    private com.homemate.notification.service.EmailService emailService;

    @InjectMocks
    private TaskRequestService taskRequestService;

    private AppUserDetails taskerUserDetails;
    private AppUserDetails otherTaskerUserDetails;
    private TaskDto taskDto;
    private Long taskId;
    private Long taskerId;

    @BeforeEach
    void setUp() {
        taskId = 100L;
        taskerId = 2L;

        taskerUserDetails = new AppUserDetails(taskerId, "tasker1", "tasker1@example.com", "ROLE_TASKER");
        otherTaskerUserDetails = new AppUserDetails(999L, "tasker2", "tasker2@example.com", "ROLE_TASKER");

        taskDto = TaskDto.builder()
                .taskID(taskId)
                .taskerID(taskerId)
                .startDate(LocalDateTime.of(2025, 11, 20, 10, 0))
                .status(com.homemate.taskmanagement.model.Status.InReview)
                .description("Test task")
                .userName("John Doe")
                .taskerName("Jane Smith")
                .serviceName("Plumbing")
                .build();
    }

    // ========== addEstimation Tests ==========

    @Test
    void testAddEstimation_Success_WhenTaskerOwnsTask() {
        // Arrange
        int estimationMinutes = 120; // 2 hours in minutes

        when(taskRequestDao.getTaskDetails(taskId)).thenReturn(Optional.of(taskDto));
        doNothing().when(taskRequestDao).add(taskId, estimationMinutes);

        // Act
        assertThatCode(() -> taskRequestService.addEstimation(taskId, estimationMinutes, taskerUserDetails))
                .doesNotThrowAnyException();

        // Assert
        verify(taskRequestDao).getTaskDetails(taskId);
        verify(taskRequestDao).add(taskId, estimationMinutes);
    }

    @Test
    void testAddEstimation_ThrowsRuntimeException_WhenTaskNotFound() {
        // Arrange
        int estimationMinutes = 120;

        when(taskRequestDao.getTaskDetails(taskId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> taskRequestService.addEstimation(taskId, estimationMinutes, taskerUserDetails))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("This Tasker has no Access to that task");

        verify(taskRequestDao).getTaskDetails(taskId);
        verify(taskRequestDao, never()).add(anyLong(), anyInt());
    }

    @Test
    void testAddEstimation_ThrowsRuntimeException_WhenTaskerDoesNotOwnTask() {
        // Arrange
        int estimationMinutes = 120;

        when(taskRequestDao.getTaskDetails(taskId)).thenReturn(Optional.of(taskDto));

        // Act & Assert
        assertThatThrownBy(() -> taskRequestService.addEstimation(taskId, estimationMinutes, otherTaskerUserDetails))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("This Tasker has no Access to that task");

        verify(taskRequestDao).getTaskDetails(taskId);
        verify(taskRequestDao, never()).add(anyLong(), anyInt());
    }

    @Test
    void testAddEstimation_WithDifferentEstimationValues() {
        // Arrange
        int[] estimations = {30, 60, 120, 240, 480, 1440}; // Various minutes: 0.5h, 1h, 2h, 4h, 8h, 24h

        when(taskRequestDao.getTaskDetails(taskId)).thenReturn(Optional.of(taskDto));

        // Act & Assert
        for (int estimation : estimations) {
            assertThatCode(() -> taskRequestService.addEstimation(taskId, estimation, taskerUserDetails))
                    .doesNotThrowAnyException();
            verify(taskRequestDao).add(taskId, estimation);
        }

        verify(taskRequestDao, times(estimations.length)).getTaskDetails(taskId);
        verify(taskRequestDao, times(estimations.length)).add(eq(taskId), anyInt());
    }

    // ========== getAllBusyTime Tests ==========

    @Test
    void testGetAllBusyTime_Success_WhenTaskerIsAvailable() {
        // Arrange
        Long taskerId = 2L;
        LocalDate day = LocalDate.of(2025, 11, 20);
        Map<LocalDateTime, Integer> expectedBusyTimes = new HashMap<>();
        expectedBusyTimes.put(LocalDateTime.of(2025, 11, 20, 10, 0), 120); // 2 hours in minutes
        expectedBusyTimes.put(LocalDateTime.of(2025, 11, 20, 14, 0), 60);  // 1 hour in minutes

        when(taskRequestDao.CheckAvailability(taskerId)).thenReturn(TaskerAvailability.AVAILABLE);
        when(taskRequestDao.getBusytime(taskerId, day)).thenReturn(expectedBusyTimes);

        // Act
        Map<LocalDateTime, Integer> result = taskRequestService.getAllBusyTime(taskerId, day);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result).containsEntry(LocalDateTime.of(2025, 11, 20, 10, 0), 120);
        assertThat(result).containsEntry(LocalDateTime.of(2025, 11, 20, 14, 0), 60);

        verify(taskRequestDao).CheckAvailability(taskerId);
        verify(taskRequestDao).getBusytime(taskerId, day);
    }

    @Test
    void testGetAllBusyTime_ReturnsEmptyMap_WhenNoTasksExist() {
        // Arrange
        Long taskerId = 2L;
        LocalDate day = LocalDate.of(2025, 11, 20);
        Map<LocalDateTime, Integer> emptyBusyTimes = new HashMap<>();

        when(taskRequestDao.CheckAvailability(taskerId)).thenReturn(TaskerAvailability.AVAILABLE);
        when(taskRequestDao.getBusytime(taskerId, day)).thenReturn(emptyBusyTimes);

        // Act
        Map<LocalDateTime, Integer> result = taskRequestService.getAllBusyTime(taskerId, day);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();

        verify(taskRequestDao).CheckAvailability(taskerId);
        verify(taskRequestDao).getBusytime(taskerId, day);
    }

    @Test
    void testGetAllBusyTime_ThrowsRuntimeException_WhenTaskerIsUnavailable() {
        // Arrange
        Long taskerId = 2L;
        LocalDate day = LocalDate.of(2025, 11, 20);

        when(taskRequestDao.CheckAvailability(taskerId)).thenReturn(TaskerAvailability.UNAVAILABLE);

        // Act & Assert
        assertThatThrownBy(() -> taskRequestService.getAllBusyTime(taskerId, day))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("This Tasker is UNAVAILABLE Now");

        verify(taskRequestDao).CheckAvailability(taskerId);
        verify(taskRequestDao, never()).getBusytime(anyLong(), any(LocalDate.class));
    }

    @Test
    void testGetAllBusyTime_WithMultipleTasksOnSameDay() {
        // Arrange
        Long taskerId = 2L;
        LocalDate day = LocalDate.of(2025, 11, 20);
        Map<LocalDateTime, Integer> expectedBusyTimes = new HashMap<>();
        expectedBusyTimes.put(LocalDateTime.of(2025, 11, 20, 8, 0), 60);   // 8:00-9:00
        expectedBusyTimes.put(LocalDateTime.of(2025, 11, 20, 10, 0), 120); // 10:00-12:00
        expectedBusyTimes.put(LocalDateTime.of(2025, 11, 20, 14, 0), 90);  // 14:00-15:30
        expectedBusyTimes.put(LocalDateTime.of(2025, 11, 20, 16, 0), 30);  // 16:00-16:30

        when(taskRequestDao.CheckAvailability(taskerId)).thenReturn(TaskerAvailability.AVAILABLE);
        when(taskRequestDao.getBusytime(taskerId, day)).thenReturn(expectedBusyTimes);

        // Act
        Map<LocalDateTime, Integer> result = taskRequestService.getAllBusyTime(taskerId, day);

        // Assert
        assertThat(result).hasSize(4);
        assertThat(result).containsAllEntriesOf(expectedBusyTimes);

        verify(taskRequestDao).CheckAvailability(taskerId);
        verify(taskRequestDao).getBusytime(taskerId, day);
    }

    @Test
    void testGetAllBusyTime_WithDifferentDays() {
        // Arrange
        Long taskerId = 2L;
        LocalDate day1 = LocalDate.of(2025, 11, 20);
        LocalDate day2 = LocalDate.of(2025, 11, 21);
        
        Map<LocalDateTime, Integer> busyTimesDay1 = new HashMap<>();
        busyTimesDay1.put(LocalDateTime.of(2025, 11, 20, 10, 0), 120);
        
        Map<LocalDateTime, Integer> busyTimesDay2 = new HashMap<>();
        busyTimesDay2.put(LocalDateTime.of(2025, 11, 21, 14, 0), 60);

        when(taskRequestDao.CheckAvailability(taskerId)).thenReturn(TaskerAvailability.AVAILABLE);
        when(taskRequestDao.getBusytime(taskerId, day1)).thenReturn(busyTimesDay1);
        when(taskRequestDao.getBusytime(taskerId, day2)).thenReturn(busyTimesDay2);

        // Act
        Map<LocalDateTime, Integer> result1 = taskRequestService.getAllBusyTime(taskerId, day1);
        Map<LocalDateTime, Integer> result2 = taskRequestService.getAllBusyTime(taskerId, day2);

        // Assert
        assertThat(result1).hasSize(1);
        assertThat(result1).containsEntry(LocalDateTime.of(2025, 11, 20, 10, 0), 120);
        
        assertThat(result2).hasSize(1);
        assertThat(result2).containsEntry(LocalDateTime.of(2025, 11, 21, 14, 0), 60);

        verify(taskRequestDao, times(2)).CheckAvailability(taskerId);
        verify(taskRequestDao).getBusytime(taskerId, day1);
        verify(taskRequestDao).getBusytime(taskerId, day2);
    }

    @Test
    void testGetAllBusyTime_WithNullAvailability_ThrowsException() {
        // Arrange
        Long taskerId = 2L;
        LocalDate day = LocalDate.of(2025, 11, 20);

        when(taskRequestDao.CheckAvailability(taskerId))
        .thenReturn(TaskerAvailability.UNAVAILABLE);
       
        assertThatThrownBy(() -> taskRequestService.getAllBusyTime(taskerId, day))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("This Tasker is UNAVAILABLE Now");
        verify(taskRequestDao).CheckAvailability(taskerId);
        verify(taskRequestDao, never()).getBusytime(anyLong(), any(LocalDate.class));
    }
}

