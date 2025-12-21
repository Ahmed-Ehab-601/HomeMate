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

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
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

        LocalDateTime currentDate = LocalDateTime.now().plusDays(1);
        LocalDateTime futureDate = LocalDateTime.now().plusDays(2);
        RescheduleRequestDto requestDto = new RescheduleRequestDto();
        requestDto.setNewStartDate(futureDate);

        TaskDto taskDto = new TaskDto();
        taskDto.setTaskID(taskID);
        taskDto.setTaskerID(taskerID);
        taskDto.setUserMail("user@example.com");
        taskDto.setTaskerMail("tasker@example.com");
        taskDto.setStartDate(currentDate); // Set the current start date

        when(taskStatusDao.getTaskerID(taskID)).thenReturn(Optional.of(requesterID));
        when(taskDao.getUserID(taskID)).thenReturn(Optional.of(20L));
        when(taskStatusDao.getStatus(taskID)).thenReturn(Optional.of(Status.Accepted));
        when(taskDao.updateTaskStartDate(taskID, futureDate)).thenReturn(true);
        when(taskRequestDao.getTaskDetails(taskID)).thenReturn(Optional.of(taskDto));

        // Mock getBusytime to return empty map (no conflicts)
        when(taskRequestDao.getBusytime(eq(taskerID), any())).thenReturn(new HashMap<>());

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
        taskDto.setStartDate(LocalDateTime.now()); // Set start date

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
        taskDto.setStartDate(LocalDateTime.now()); // Set start date

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
        // Arrange
        long taskID = 10L;
        long requesterID = 5L;
        long taskerID = 5L;

        LocalDateTime currentDate = LocalDateTime.now().plusDays(1);
        LocalDateTime futureDate = LocalDateTime.now().plusDays(2);

        RescheduleRequestDto requestDto = new RescheduleRequestDto();
        requestDto.setNewStartDate(futureDate);

        TaskDto taskDto = new TaskDto();
        taskDto.setTaskID(taskID);
        taskDto.setTaskerID(taskerID);
        taskDto.setUserMail("user@example.com");
        taskDto.setTaskerMail("tasker@example.com");
        taskDto.setStartDate(currentDate); // Set the current start date

        when(taskStatusDao.getTaskerID(taskID)).thenReturn(Optional.of(requesterID));
        when(taskDao.getUserID(taskID)).thenReturn(Optional.of(20L));
        when(taskStatusDao.getStatus(taskID)).thenReturn(Optional.of(Status.Accepted));
        when(taskDao.updateTaskStartDate(taskID, futureDate)).thenReturn(true);
        when(taskRequestDao.getTaskDetails(taskID)).thenReturn(Optional.of(taskDto));

        // Mock getBusytime to return empty map (no conflicts)
        when(taskRequestDao.getBusytime(eq(taskerID), any())).thenReturn(new HashMap<>());

        // Mock getEstimation to return a valid estimation
        when(taskRequestDao.getEstimation(taskID)).thenReturn(120);

        // Act
        taskRescheduleService.rescheduleTask(taskID, requestDto, requesterID);

        // Assert: email sent twice (to user and tasker)
        verify(emailServiceImp, times(2)).sendEmail(any());
    }

    @Test
    void testRescheduleEmailFails() {
        // Arrange
        long taskID = 10L;
        long requesterID = 5L;
        long taskerID = 5L;

        LocalDateTime currentDate = LocalDateTime.now().plusDays(1);
        LocalDateTime futureDate = LocalDateTime.now().plusDays(2);

        RescheduleRequestDto requestDto = new RescheduleRequestDto();
        requestDto.setNewStartDate(futureDate);

        TaskDto taskDto = new TaskDto();
        taskDto.setTaskID(taskID);
        taskDto.setTaskerID(taskerID);
        taskDto.setUserMail("user@example.com");
        taskDto.setTaskerMail("tasker@example.com");
        taskDto.setStartDate(currentDate); // Set the current start date

        when(taskStatusDao.getTaskerID(taskID)).thenReturn(Optional.of(requesterID));
        when(taskDao.getUserID(taskID)).thenReturn(Optional.of(20L));
        when(taskStatusDao.getStatus(taskID)).thenReturn(Optional.of(Status.Accepted));
        when(taskDao.updateTaskStartDate(taskID, futureDate)).thenReturn(true);
        when(taskRequestDao.getTaskDetails(taskID)).thenReturn(Optional.of(taskDto));

        // Mock getBusytime to return empty map (no conflicts)
        when(taskRequestDao.getBusytime(eq(taskerID), any())).thenReturn(new HashMap<>());

        // Mock getEstimation to return a valid estimation
        when(taskRequestDao.getEstimation(taskID)).thenReturn(120);

        // Simulate email failure
        doThrow(new RuntimeException("Email failed")).when(emailServiceImp).sendEmail(any());

        // Act & Assert
        assertThrows(RuntimeException.class, () ->
                taskRescheduleService.rescheduleTask(taskID, requestDto, requesterID)
        );
    }



    @Test
    void testRescheduleInvalidEstimation() {
        // Arrange
        long taskID = 10L;
        long requesterID = 5L;
        long taskerID = 5L;

        LocalDateTime currentDate = LocalDateTime.now().plusDays(1).withHour(19).withMinute(0);
        LocalDateTime futureDate = LocalDateTime.now().plusDays(2);

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

        // Mock getBusytime to return empty map
        when(taskRequestDao.getBusytime(eq(taskerID), any())).thenReturn(new HashMap<>());

        // Mock getEstimation to return estimation that extends beyond work hours
        when(taskRequestDao.getEstimation(taskID)).thenReturn(180);

        // Act & Assert - Expect IllegalStateException instead of BadRescheduleException
        assertThrows(IllegalStateException.class, () ->
                taskRescheduleService.rescheduleTask(taskID, requestDto, requesterID)
        );

        // Verify no email was sent
        verify(emailServiceImp, never()).sendEmail(any());
    }

    @Test
    void testRescheduleWithConflictingTask() {
        // Arrange
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

        // Mock getBusytime to return a conflicting task
        Map<LocalDateTime, Integer> busyTimes = new HashMap<>();
        LocalDateTime conflictingTaskStart = futureDate.plusHours(1);
        busyTimes.put(conflictingTaskStart, 120);
        when(taskRequestDao.getBusytime(eq(taskerID), any())).thenReturn(busyTimes);

        when(taskRequestDao.getEstimation(taskID)).thenReturn(180);

        // Act & Assert - Expect IllegalStateException instead of BadRescheduleException
        assertThrows(IllegalStateException.class, () ->
                taskRescheduleService.rescheduleTask(taskID, requestDto, requesterID)
        );

        // Verify no email was sent
        verify(emailServiceImp, never()).sendEmail(any());
    }
    @Test
    void testRescheduleInvalidStatus() {
        // Arrange
        long taskID = 10L;
        long requesterID = 5L;
        LocalDateTime futureDate = LocalDateTime.now().plusDays(2);

        RescheduleRequestDto requestDto = new RescheduleRequestDto();
        requestDto.setNewStartDate(futureDate);

        TaskDto taskDto = new TaskDto();
        taskDto.setStartDate(LocalDateTime.now()); // Set start date

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
}