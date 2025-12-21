package com.homemate.taskmanagementtests;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

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
                .thenReturn(Collections.emptyMap());

        taskRequestService.addEstimation(1L, 30, userDetails);

        verify(taskRequestDao).add(1L, 30);
    }

    @Test
    void addEstimation_invalidEstimationOverlap() {
        Map<LocalDateTime, Integer> busy = new HashMap<>();
        busy.put(LocalDateTime.of(2025, 1, 1, 10, 15), 30);

        when(taskRequestDao.getTaskDetails(1L)).thenReturn(Optional.of(taskDto));
        when(taskRequestDao.getBusytime(anyLong(), any())).thenReturn(busy);

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> taskRequestService.addEstimation(1L, 30, userDetails)
        );

        assertEquals("invalid estimation", ex.getMessage());
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
                .thenReturn(Collections.emptyMap());

        boolean result = taskRequestService.checkValidEstimation(30, 1L);

        assertTrue(result);
    }

    @Test
    void checkValidEstimation_withConflict_returnsFalse() {
        Map<LocalDateTime, Integer> busy = new HashMap<>();
        busy.put(LocalDateTime.of(2025, 1, 1, 10, 10), 30);

        when(taskRequestDao.getTaskDetails(1L)).thenReturn(Optional.of(taskDto));
        when(taskRequestDao.getBusytime(anyLong(), any())).thenReturn(busy);

        boolean result = taskRequestService.checkValidEstimation(30, 1L);

        assertFalse(result);
    }

    @Test
    void checkValidEstimation_taskNotFound_returnsFalse() {
        when(taskRequestDao.getTaskDetails(1L)).thenReturn(Optional.empty());

        boolean result = taskRequestService.checkValidEstimation(30, 1L);

        assertFalse(result);
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

        assertEquals("This Tasker is UNAVAILABLE Now", ex.getMessage());
        verify(taskRequestDao, never()).getBusytime(anyLong(), any());
    }

    @Test
    void getAllBusyTime_taskerAvailable_returnsBusyTime() {
        Map<LocalDateTime, Integer> busyTime = new HashMap<>();
        busyTime.put(LocalDateTime.of(2025, 1, 1, 10, 0), 30);

        when(taskRequestDao.CheckAvailability(5L))
                .thenReturn(TaskerAvailability.AVAILABLE);
        when(taskRequestDao.getBusytime(5L, LocalDate.of(2025, 1, 1)))
                .thenReturn(busyTime);

        Map<LocalDateTime, Integer> result =
                taskRequestService.getAllBusyTime(5L, LocalDate.of(2025, 1, 1));

        assertEquals(1, result.size());
        assertEquals(30, result.get(LocalDateTime.of(2025, 1, 1, 10, 0)));
    }

}
