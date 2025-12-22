package com.homemate.taskmanagementtests;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import com.homemate.TaskerProfile.models.TaskerAvailability;
import com.homemate.security.model.AppUserDetails;
import com.homemate.taskmanagement.dao.TaskRequestDao;
import com.homemate.taskmanagement.dto.TaskDto;
import com.homemate.taskmanagement.dto.TaskTimeDto;
import com.homemate.taskmanagement.service.TaskRequestService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TaskRequestServiceEstimationAndBusyTimeTest {

    @Mock
    private TaskRequestDao taskRequestDao;

    @Mock
    private AppUserDetails userDetails;

    @InjectMocks
    private TaskRequestService taskRequestService;

    private TaskDto taskDto;

    @BeforeEach
    void setUp() {
        taskDto = new TaskDto();
        taskDto.setTaskerID(10L);
        taskDto.setStartDate(LocalDateTime.of(2025, 1, 1, 10, 0));
    }

    /* ==================== addEstimation ==================== */

    @Test
    void addEstimation_success() {
        when(userDetails.getId()).thenReturn(10L);
        when(taskRequestDao.getTaskDetails(1L)).thenReturn(Optional.of(taskDto));
        when(taskRequestDao.getBusytime(anyLong(), any()))
                .thenReturn(Collections.emptyList());

        taskRequestService.addEstimation(1L, 30, userDetails);

        verify(taskRequestDao).addEstimation(1L, 30);
    }

    @Test
    void addEstimation_invalidEstimationOverlap() {
        List<TaskTimeDto> busyList = new ArrayList<>();
        busyList.add(TaskTimeDto.builder()
                .taskID(2L)
                .startDate(LocalDateTime.of(2025, 1, 1, 10, 15))
                .estimation(30)
                .build());

        when(taskRequestDao.getTaskDetails(1L)).thenReturn(Optional.of(taskDto));
        when(taskRequestDao.getBusytime(anyLong(), any())).thenReturn(busyList);

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> taskRequestService.addEstimation(1L, 30, userDetails)
        );

        assertEquals("Invalid estimation: This time slot conflicts with another scheduled task", ex.getMessage());
    }

    @Test
    void addEstimation_estimationLessThanZero() {
        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> taskRequestService.addEstimation(1L, -5, userDetails)
        );

        assertEquals("estimation need to be > 0", ex.getMessage());
    }

    @Test
    void addEstimation_noAccessToTask() {
        when(userDetails.getId()).thenReturn(99L);
        when(taskRequestDao.getTaskDetails(1L)).thenReturn(Optional.of(taskDto));

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> taskRequestService.addEstimation(1L, 30, userDetails)
        );

        assertEquals("This Tasker has no Access to that task", ex.getMessage());
    }

    /* ==================== checkValidEstimation ==================== */

    @Test
    void checkValidEstimation_noConflict_returnsTrue() {
        when(taskRequestDao.getTaskDetails(1L)).thenReturn(Optional.of(taskDto));
        when(taskRequestDao.getBusytime(anyLong(), any()))
                .thenReturn(Collections.emptyList());

        boolean result = taskRequestService.checkValidEstimation(30, 1L);

        assertTrue(result);
    }

    @Test
    void checkValidEstimation_withConflict_returnsFalse() {
        List<TaskTimeDto> busyList = new ArrayList<>();
        busyList.add(TaskTimeDto.builder()
                .taskID(2L)
                .startDate(LocalDateTime.of(2025, 1, 1, 10, 10))
                .estimation(30)
                .build());

        when(taskRequestDao.getTaskDetails(1L)).thenReturn(Optional.of(taskDto));
        when(taskRequestDao.getBusytime(anyLong(), any())).thenReturn(busyList);

        boolean result = taskRequestService.checkValidEstimation(30, 1L);

        assertFalse(result);
    }

    @Test
    void checkValidEstimation_taskNotFound_returnsFalse() {
        when(taskRequestDao.getTaskDetails(1L)).thenReturn(Optional.empty());

        boolean result = taskRequestService.checkValidEstimation(30, 1L);

        assertFalse(result);
    }

    @Test
    void checkValidEstimation_skipsSameTask_returnsTrue() {
        // Task with ID 1 at 10:00 with 30 min estimation
        taskDto.setTaskID(1L);

        List<TaskTimeDto> busyList = new ArrayList<>();
        // Same task should be skipped
        busyList.add(TaskTimeDto.builder()
                .taskID(1L)
                .startDate(LocalDateTime.of(2025, 1, 1, 10, 0))
                .estimation(30)
                .build());

        when(taskRequestDao.getTaskDetails(1L)).thenReturn(Optional.of(taskDto));
        when(taskRequestDao.getBusytime(anyLong(), any())).thenReturn(busyList);

        boolean result = taskRequestService.checkValidEstimation(30, 1L);

        assertTrue(result);
    }

    /* ==================== getTaskDetails ==================== */

    @Test
    void getTaskDetails_returnsEstimation() {
        when(taskRequestDao.getEstimation(1L)).thenReturn(45);

        int estimation = taskRequestService.getTaskDetails(1L);

        assertEquals(45, estimation);
    }

    /* ==================== getAllBusyTime ==================== */

    @Test
    void getAllBusyTime_taskerUnavailable_throwsException() {
        when(taskRequestDao.CheckAvailability(5L))
                .thenReturn(TaskerAvailability.UNAVAILABLE);

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> taskRequestService.getAllBusyTime(5L, LocalDate.now())
        );

        assertEquals("UNAVAILABLE: This Tasker is UNAVAILABLE Now", ex.getMessage());
        verify(taskRequestDao, never()).getBusytime(anyLong(), any());
    }

    @Test
    void getAllBusyTime_taskerAvailable_returnsBusyTime() {
        List<TaskTimeDto> busyList = new ArrayList<>();
        busyList.add(TaskTimeDto.builder()
                .taskID(1L)
                .startDate(LocalDateTime.of(2025, 1, 1, 10, 0))
                .estimation(30)
                .build());

        when(taskRequestDao.CheckAvailability(5L))
                .thenReturn(TaskerAvailability.AVAILABLE);
        when(taskRequestDao.getBusytime(5L, LocalDate.of(2025, 1, 1)))
                .thenReturn(busyList);

        List<TaskTimeDto> result =
                taskRequestService.getAllBusyTime(5L, LocalDate.of(2025, 1, 1));

        assertEquals(1, result.size());
        assertEquals(30, result.get(0).getEstimation());
        assertEquals(LocalDateTime.of(2025, 1, 1, 10, 0), result.get(0).getStartDate());
    }

    @Test
    void testGetAllBusyTime_WithDifferentDays() {
        Long taskerId = 2L;
        LocalDate day1 = LocalDate.of(2025, 11, 20);
        LocalDate day2 = LocalDate.of(2025, 11, 21);

        List<TaskTimeDto> busyTimesDay1 = new ArrayList<>();
        busyTimesDay1.add(TaskTimeDto.builder()
                .taskID(1L)
                .startDate(LocalDateTime.of(2025, 11, 20, 10, 0))
                .estimation(120)
                .build());

        List<TaskTimeDto> busyTimesDay2 = new ArrayList<>();
        busyTimesDay2.add(TaskTimeDto.builder()
                .taskID(2L)
                .startDate(LocalDateTime.of(2025, 11, 21, 14, 0))
                .estimation(60)
                .build());

        when(taskRequestDao.CheckAvailability(taskerId)).thenReturn(TaskerAvailability.AVAILABLE);
        when(taskRequestDao.getBusytime(taskerId, day1)).thenReturn(busyTimesDay1);
        when(taskRequestDao.getBusytime(taskerId, day2)).thenReturn(busyTimesDay2);

        List<TaskTimeDto> result1 = taskRequestService.getAllBusyTime(taskerId, day1);
        List<TaskTimeDto> result2 = taskRequestService.getAllBusyTime(taskerId, day2);

        assertThat(result1).hasSize(1);
        assertThat(result1.get(0).getStartDate()).isEqualTo(LocalDateTime.of(2025, 11, 20, 10, 0));
        assertThat(result1.get(0).getEstimation()).isEqualTo(120);

        assertThat(result2).hasSize(1);
        assertThat(result2.get(0).getStartDate()).isEqualTo(LocalDateTime.of(2025, 11, 21, 14, 0));
        assertThat(result2.get(0).getEstimation()).isEqualTo(60);

        verify(taskRequestDao, times(2)).CheckAvailability(taskerId);
        verify(taskRequestDao).getBusytime(taskerId, day1);
        verify(taskRequestDao).getBusytime(taskerId, day2);
    }

    @Test
    void testGetAllBusyTime_WithNullAvailability_ThrowsException() {
        Long taskerId = 2L;
        LocalDate day = LocalDate.of(2025, 11, 20);

        when(taskRequestDao.CheckAvailability(taskerId))
                .thenReturn(TaskerAvailability.UNAVAILABLE);

        assertThatThrownBy(() -> taskRequestService.getAllBusyTime(taskerId, day))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("UNAVAILABLE: This Tasker is UNAVAILABLE Now");
        verify(taskRequestDao).CheckAvailability(taskerId);
        verify(taskRequestDao, never()).getBusytime(anyLong(), any(LocalDate.class));
    }
}