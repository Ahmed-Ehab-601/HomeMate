package com.homemate.taskmanagementtests;

import com.homemate.notification.service.impl.EmailServiceImpl;
import com.homemate.taskmanagement.dao.TaskRequestDao;
import com.homemate.taskmanagement.dao.TaskRescheduleDao;
import com.homemate.taskmanagement.dao.TaskStatusDao;
import com.homemate.taskmanagement.dto.RescheduleRequestDto;
import com.homemate.taskmanagement.dto.RescheduleResponseDto;
import com.homemate.taskmanagement.dto.TaskDto;
import com.homemate.taskmanagement.exceptions.BadRescheduleException;
import com.homemate.taskmanagement.exceptions.TaskNotFoundException;
import com.homemate.taskmanagement.model.Status;
import com.homemate.taskmanagement.service.TaskRescheduleService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TaskManagementServiceRescheduleTest {
    @Mock
    private TaskRescheduleDao taskDao;
    @Mock
    private TaskStatusDao taskStatusDao;
    @Mock
    private TaskRequestDao taskRequestDao;
    @Mock
    private SimpMessagingTemplate simpMessagingTemplate;

    @Mock
    private EmailServiceImpl emailServiceImp;

    @InjectMocks
    private TaskRescheduleService taskRescheduleService;

    @Test
    void testValidReschedule() {
        long taskID = 10L;
        long requesterID = 5L;
        long taskerID = 5L;

        LocalDateTime currentDate = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0);
        LocalDateTime futureDate = LocalDateTime.now().plusDays(2).withHour(14).withMinute(0);
        RescheduleRequestDto requestDto = new RescheduleRequestDto();
        requestDto.setNewStartDate(futureDate);

        TaskDto taskDto = new TaskDto();
        taskDto.setTaskID(taskID);
        taskDto.setTaskerID(taskerID);
        taskDto.setUserMail("user@example.com");
        taskDto.setTaskerMail("tasker@example.com");
        taskDto.setStartDate(currentDate);

        when(taskStatusDao.getTaskerID(taskID)).thenReturn(Optional.of(requesterID));
        when(taskDao.getUserID(taskID)).thenReturn(Optional.of(20L));
        when(taskStatusDao.getStatus(taskID)).thenReturn(Optional.of(Status.Accepted));
        when(taskDao.updateTaskStartDate(taskID, futureDate)).thenReturn(true);
        when(taskRequestDao.getTaskDetails(taskID)).thenReturn(Optional.of(taskDto));

        // ✅ Mock getBusytime for the NEW date (futureDate's date)
        when(taskRequestDao.getBusytime(eq(taskerID), eq(futureDate.toLocalDate())))
                .thenReturn(new HashMap<>());

        // Mock getEstimation to return a valid estimation (e.g., 120 minutes)
        when(taskRequestDao.getEstimation(taskID)).thenReturn(120);

        RescheduleResponseDto response = taskRescheduleService.rescheduleTask(taskID, requestDto, requesterID);

        assertEquals(taskID, response.getTaskID());
        assertEquals(futureDate, response.getNewStartDate());

        // Verify email was sent
        verify(emailServiceImp, times(2)).sendEmail(any());
    }

    @Test
    void testReschedulePastDate() {
        long taskID = 10L;
        long requesterID = 5L;

        LocalDateTime pastDate = LocalDateTime.now().minusDays(1);
        RescheduleRequestDto requestDto = new RescheduleRequestDto();
        requestDto.setNewStartDate(pastDate);

        TaskDto taskDto = new TaskDto();
        taskDto.setUserMail("user@example.com");
        taskDto.setTaskerMail("tasker@example.com");
        taskDto.setStartDate(LocalDateTime.now());

        when(taskStatusDao.getTaskerID(taskID)).thenReturn(Optional.of(requesterID));
        when(taskDao.getUserID(taskID)).thenReturn(Optional.of(20L));
        when(taskStatusDao.getStatus(taskID)).thenReturn(Optional.of(Status.Accepted));

        assertThrows(BadRescheduleException.class, () ->
                taskRescheduleService.rescheduleTask(taskID, requestDto, requesterID)
        );

        // Verify no email was sent
        verify(emailServiceImp, never()).sendEmail(any());
    }

    @Test
    void testRequesterNotUserOrTasker() {
        long taskID = 10L;

        RescheduleRequestDto requestDto = new RescheduleRequestDto();
        requestDto.setNewStartDate(LocalDateTime.now().plusDays(1));

        TaskDto taskDto = new TaskDto();
        taskDto.setUserMail("user@example.com");
        taskDto.setTaskerMail("tasker@example.com");
        taskDto.setStartDate(LocalDateTime.now());

        when(taskStatusDao.getTaskerID(taskID)).thenReturn(Optional.of(99L));
        when(taskDao.getUserID(taskID)).thenReturn(Optional.of(100L));

        long unauthorizedID = 50L;

        assertThrows(BadRescheduleException.class, () ->
                taskRescheduleService.rescheduleTask(taskID, requestDto, unauthorizedID)
        );

        // Verify no email was sent
        verify(emailServiceImp, never()).sendEmail(any());
    }

    @Test
    void testTaskIdDoesNotExist() {
        long taskID = 10L;
        long requesterID = 5L;

        RescheduleRequestDto requestDto = new RescheduleRequestDto();
        requestDto.setNewStartDate(LocalDateTime.now().plusDays(1));

        when(taskStatusDao.getTaskerID(taskID)).thenReturn(Optional.empty());

        assertThrows(TaskNotFoundException.class, () ->
                taskRescheduleService.rescheduleTask(taskID, requestDto, requesterID)
        );

        // Verify no email was sent
        verify(emailServiceImp, never()).sendEmail(any());
    }

    @Test
    void testRescheduleSendsEmailSuccessfully() {
        long taskID = 10L;
        long requesterID = 5L;
        long taskerID = 5L;

        LocalDateTime currentDate = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0);
        LocalDateTime futureDate = LocalDateTime.now().plusDays(2).withHour(14).withMinute(0);

        RescheduleRequestDto requestDto = new RescheduleRequestDto();
        requestDto.setNewStartDate(futureDate);

        TaskDto taskDto = new TaskDto();
        taskDto.setTaskID(taskID);
        taskDto.setTaskerID(taskerID);
        taskDto.setUserMail("user@example.com");
        taskDto.setTaskerMail("tasker@example.com");
        taskDto.setStartDate(currentDate);

        when(taskStatusDao.getTaskerID(taskID)).thenReturn(Optional.of(requesterID));
        when(taskDao.getUserID(taskID)).thenReturn(Optional.of(20L));
        when(taskStatusDao.getStatus(taskID)).thenReturn(Optional.of(Status.Accepted));
        when(taskDao.updateTaskStartDate(taskID, futureDate)).thenReturn(true);
        when(taskRequestDao.getTaskDetails(taskID)).thenReturn(Optional.of(taskDto));

        // ✅ Mock getBusytime for the NEW date
        when(taskRequestDao.getBusytime(eq(taskerID), eq(futureDate.toLocalDate())))
                .thenReturn(new HashMap<>());

        when(taskRequestDao.getEstimation(taskID)).thenReturn(120);

        taskRescheduleService.rescheduleTask(taskID, requestDto, requesterID);

        // Assert: email sent twice (to user and tasker)
        verify(emailServiceImp, times(2)).sendEmail(any());
    }

    @Test
    void testRescheduleEmailFails() {
        long taskID = 10L;
        long requesterID = 5L;
        long taskerID = 5L;

        LocalDateTime currentDate = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0);
        LocalDateTime futureDate = LocalDateTime.now().plusDays(2).withHour(14).withMinute(0);

        RescheduleRequestDto requestDto = new RescheduleRequestDto();
        requestDto.setNewStartDate(futureDate);

        TaskDto taskDto = new TaskDto();
        taskDto.setTaskID(taskID);
        taskDto.setTaskerID(taskerID);
        taskDto.setUserMail("user@example.com");
        taskDto.setTaskerMail("tasker@example.com");
        taskDto.setStartDate(currentDate);

        when(taskStatusDao.getTaskerID(taskID)).thenReturn(Optional.of(requesterID));
        when(taskDao.getUserID(taskID)).thenReturn(Optional.of(20L));
        when(taskStatusDao.getStatus(taskID)).thenReturn(Optional.of(Status.Accepted));
        when(taskDao.updateTaskStartDate(taskID, futureDate)).thenReturn(true);
        when(taskRequestDao.getTaskDetails(taskID)).thenReturn(Optional.of(taskDto));

        // ✅ Mock getBusytime for the NEW date
        when(taskRequestDao.getBusytime(eq(taskerID), eq(futureDate.toLocalDate())))
                .thenReturn(new HashMap<>());

        when(taskRequestDao.getEstimation(taskID)).thenReturn(120);

        // Simulate email failure
        doThrow(new RuntimeException("Email failed")).when(emailServiceImp).sendEmail(any());

        // Act & Assert
        assertThrows(RuntimeException.class, () ->
                taskRescheduleService.rescheduleTask(taskID, requestDto, requesterID)
        );
    }

    @Test
    void testRescheduleWithConflictingTask() {
        long taskID = 10L;
        long requesterID = 5L;
        long taskerID = 5L;

        LocalDateTime currentDate = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0);
        LocalDateTime futureDate = LocalDateTime.now().plusDays(2).withHour(14).withMinute(0);

        RescheduleRequestDto requestDto = new RescheduleRequestDto();
        requestDto.setNewStartDate(futureDate);

        TaskDto taskDto = new TaskDto();
        taskDto.setTaskID(taskID);
        taskDto.setTaskerID(taskerID);
        taskDto.setUserMail("user@example.com");
        taskDto.setTaskerMail("tasker@example.com");
        taskDto.setStartDate(currentDate);

        when(taskStatusDao.getTaskerID(taskID)).thenReturn(Optional.of(requesterID));
        when(taskDao.getUserID(taskID)).thenReturn(Optional.of(20L));
        when(taskStatusDao.getStatus(taskID)).thenReturn(Optional.of(Status.Accepted));
        when(taskRequestDao.getTaskDetails(taskID)).thenReturn(Optional.of(taskDto));

        // ✅ Mock getBusytime for the NEW date with a conflicting task
        // Task starts at 14:00, duration 180 mins (ends at 17:00)
        // Conflicting task starts at 15:00, duration 120 mins (ends at 17:00)
        // These overlap!
        Map<LocalDateTime, Integer> busyTimes = new HashMap<>();
        LocalDateTime conflictingTaskStart = futureDate.plusHours(1); // 15:00
        busyTimes.put(conflictingTaskStart, 120);
        when(taskRequestDao.getBusytime(eq(taskerID), eq(futureDate.toLocalDate())))
                .thenReturn(busyTimes);

        when(taskRequestDao.getEstimation(taskID)).thenReturn(180);

        // Act & Assert - Expect BadRescheduleException due to conflict
        assertThrows(BadRescheduleException.class, () ->
                taskRescheduleService.rescheduleTask(taskID, requestDto, requesterID)
        );

        // Verify no email was sent
        verify(emailServiceImp, never()).sendEmail(any());
    }

    @Test
    void testRescheduleInvalidStatus() {
        long taskID = 10L;
        long requesterID = 5L;
        LocalDateTime futureDate = LocalDateTime.now().plusDays(2);

        RescheduleRequestDto requestDto = new RescheduleRequestDto();
        requestDto.setNewStartDate(futureDate);

        TaskDto taskDto = new TaskDto();
        taskDto.setStartDate(LocalDateTime.now());

        when(taskStatusDao.getTaskerID(taskID)).thenReturn(Optional.of(requesterID));
        when(taskDao.getUserID(taskID)).thenReturn(Optional.of(20L));
        // Task is in InProgress status (cannot reschedule)
        when(taskStatusDao.getStatus(taskID)).thenReturn(Optional.of(Status.InProgress));

        // Act & Assert
        assertThrows(BadRescheduleException.class, () ->
                taskRescheduleService.rescheduleTask(taskID, requestDto, requesterID)
        );

        // Verify no update and no email was sent
        verify(taskDao, never()).updateTaskStartDate(anyLong(), any(LocalDateTime.class));
        verify(emailServiceImp, never()).sendEmail(any());
    }

    @Test
    void testRescheduleTaskSkipsOwnSlot() {
        // Test that the task being rescheduled doesn't conflict with its own old slot
        long taskID = 10L;
        long requesterID = 5L;
        long taskerID = 5L;

        LocalDateTime currentDate = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0);
        LocalDateTime futureDate = LocalDateTime.now().plusDays(1).withHour(14).withMinute(0); // Same day!

        RescheduleRequestDto requestDto = new RescheduleRequestDto();
        requestDto.setNewStartDate(futureDate);

        TaskDto taskDto = new TaskDto();
        taskDto.setTaskID(taskID);
        taskDto.setTaskerID(taskerID);
        taskDto.setUserMail("user@example.com");
        taskDto.setTaskerMail("tasker@example.com");
        taskDto.setStartDate(currentDate);

        when(taskStatusDao.getTaskerID(taskID)).thenReturn(Optional.of(requesterID));
        when(taskDao.getUserID(taskID)).thenReturn(Optional.of(20L));
        when(taskStatusDao.getStatus(taskID)).thenReturn(Optional.of(Status.Accepted));
        when(taskDao.updateTaskStartDate(taskID, futureDate)).thenReturn(true);
        when(taskRequestDao.getTaskDetails(taskID)).thenReturn(Optional.of(taskDto));

        // ✅ Busy times include the task's own old slot (should be skipped)
        Map<LocalDateTime, Integer> busyTimes = new HashMap<>();
        busyTimes.put(currentDate, 120); // Old slot at 10:00
        when(taskRequestDao.getBusytime(eq(taskerID), eq(futureDate.toLocalDate())))
                .thenReturn(busyTimes);

        when(taskRequestDao.getEstimation(taskID)).thenReturn(120);

        // Should succeed because it skips its own old slot
        RescheduleResponseDto response = taskRescheduleService.rescheduleTask(taskID, requestDto, requesterID);

        assertEquals(taskID, response.getTaskID());
        assertEquals(futureDate, response.getNewStartDate());

        verify(emailServiceImp, times(2)).sendEmail(any());
    }

    @Test
    void testRescheduleNoConflictDifferentDay() {
        // Test rescheduling to a completely different day with no conflicts
        long taskID = 10L;
        long requesterID = 5L;
        long taskerID = 5L;

        LocalDateTime currentDate = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0);
        LocalDateTime futureDate = LocalDateTime.now().plusDays(5).withHour(14).withMinute(0);

        RescheduleRequestDto requestDto = new RescheduleRequestDto();
        requestDto.setNewStartDate(futureDate);

        TaskDto taskDto = new TaskDto();
        taskDto.setTaskID(taskID);
        taskDto.setTaskerID(taskerID);
        taskDto.setUserMail("user@example.com");
        taskDto.setTaskerMail("tasker@example.com");
        taskDto.setStartDate(currentDate);

        when(taskStatusDao.getTaskerID(taskID)).thenReturn(Optional.of(requesterID));
        when(taskDao.getUserID(taskID)).thenReturn(Optional.of(20L));
        when(taskStatusDao.getStatus(taskID)).thenReturn(Optional.of(Status.Accepted));
        when(taskDao.updateTaskStartDate(taskID, futureDate)).thenReturn(true);
        when(taskRequestDao.getTaskDetails(taskID)).thenReturn(Optional.of(taskDto));

        // No busy times on the new date
        when(taskRequestDao.getBusytime(eq(taskerID), eq(futureDate.toLocalDate())))
                .thenReturn(new HashMap<>());

        when(taskRequestDao.getEstimation(taskID)).thenReturn(120);

        RescheduleResponseDto response = taskRescheduleService.rescheduleTask(taskID, requestDto, requesterID);

        assertEquals(taskID, response.getTaskID());
        assertEquals(futureDate, response.getNewStartDate());

        verify(emailServiceImp, times(2)).sendEmail(any());
    }
}