package com.homemate.taskmanagementtests;

import com.homemate.notification.service.impl.EmailServiceImpl;
import com.homemate.taskmanagement.dao.TaskRequestDao;
import com.homemate.taskmanagement.dao.TaskRescheduleDao;
import com.homemate.taskmanagement.dao.TaskStatusDao;
import com.homemate.taskmanagement.dto.RescheduleRequestDto;
import com.homemate.taskmanagement.dto.RescheduleResponseDto;
import com.homemate.taskmanagement.dto.TaskDto;
import com.homemate.taskmanagement.dto.TaskTimeDto;
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
import java.util.ArrayList;
import java.util.List;
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

        // ✅ Mock getBusytime to return List<TaskTimeDto> (empty list = no conflicts)
        when(taskRequestDao.getBusytime(eq(taskerID), eq(futureDate.toLocalDate())))
                .thenReturn(new ArrayList<>());

        when(taskRequestDao.getEstimation(taskID)).thenReturn(120);

        RescheduleResponseDto response = taskRescheduleService.rescheduleTask(taskID, requestDto, requesterID);

        assertEquals(taskID, response.getTaskID());
        assertEquals(futureDate, response.getNewStartDate());

        verify(emailServiceImp, times(2)).sendEmail(any());
    }

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

        // ✅ Mock getBusytime to return List<TaskTimeDto>
        when(taskRequestDao.getBusytime(eq(taskerID), eq(futureDate.toLocalDate())))
                .thenReturn(new ArrayList<>());

        when(taskRequestDao.getEstimation(taskID)).thenReturn(120);

        taskRescheduleService.rescheduleTask(taskID, requestDto, requesterID);

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

        // ✅ Mock getBusytime to return List<TaskTimeDto>
        when(taskRequestDao.getBusytime(eq(taskerID), eq(futureDate.toLocalDate())))
                .thenReturn(new ArrayList<>());

        when(taskRequestDao.getEstimation(taskID)).thenReturn(120);

        doThrow(new RuntimeException("Email failed")).when(emailServiceImp).sendEmail(any());

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

        // ✅ Create proper TaskTimeDto for conflicting task
        // Task being rescheduled: starts at 14:00, duration 180 mins (ends at 17:00)
        // Conflicting task: starts at 15:00, duration 120 mins (ends at 17:00)
        LocalDateTime conflictingTaskStart = futureDate.plusHours(1); // 15:00

        TaskTimeDto conflictingTask = new TaskTimeDto();
        conflictingTask.setTaskID(99L); // Different task ID
        conflictingTask.setStartDate(conflictingTaskStart);
        conflictingTask.setEstimation(120);

        List<TaskTimeDto> busyTimes = new ArrayList<>();
        busyTimes.add(conflictingTask);

        when(taskRequestDao.getBusytime(eq(taskerID), eq(futureDate.toLocalDate())))
                .thenReturn(busyTimes);

        when(taskRequestDao.getEstimation(taskID)).thenReturn(180);

        assertThrows(BadRescheduleException.class, () ->
                taskRescheduleService.rescheduleTask(taskID, requestDto, requesterID)
        );

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
        when(taskStatusDao.getStatus(taskID)).thenReturn(Optional.of(Status.InProgress));

        assertThrows(BadRescheduleException.class, () ->
                taskRescheduleService.rescheduleTask(taskID, requestDto, requesterID)
        );

        verify(taskDao, never()).updateTaskStartDate(anyLong(), any(LocalDateTime.class));
        verify(emailServiceImp, never()).sendEmail(any());
    }

    @Test
    void testRescheduleTaskSkipsOwnSlot() {
        long taskID = 10L;
        long requesterID = 5L;
        long taskerID = 5L;

        LocalDateTime currentDate = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0);
        LocalDateTime futureDate = LocalDateTime.now().plusDays(1).withHour(14).withMinute(0);

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

        // ✅ Create TaskTimeDto for the task's own old slot (should be skipped)
        TaskTimeDto ownTaskSlot = new TaskTimeDto();
        ownTaskSlot.setTaskID(taskID); // Same task ID - will be skipped
        ownTaskSlot.setStartDate(currentDate);
        ownTaskSlot.setEstimation(120);

        List<TaskTimeDto> busyTimes = new ArrayList<>();
        busyTimes.add(ownTaskSlot);

        when(taskRequestDao.getBusytime(eq(taskerID), eq(futureDate.toLocalDate())))
                .thenReturn(busyTimes);

        when(taskRequestDao.getEstimation(taskID)).thenReturn(120);

        RescheduleResponseDto response = taskRescheduleService.rescheduleTask(taskID, requestDto, requesterID);

        assertEquals(taskID, response.getTaskID());
        assertEquals(futureDate, response.getNewStartDate());

        verify(emailServiceImp, times(2)).sendEmail(any());
    }

    @Test
    void testRescheduleNoConflictDifferentDay() {
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

        // ✅ Empty list = no conflicts
        when(taskRequestDao.getBusytime(eq(taskerID), eq(futureDate.toLocalDate())))
                .thenReturn(new ArrayList<>());

        when(taskRequestDao.getEstimation(taskID)).thenReturn(120);

        RescheduleResponseDto response = taskRescheduleService.rescheduleTask(taskID, requestDto, requesterID);

        assertEquals(taskID, response.getTaskID());
        assertEquals(futureDate, response.getNewStartDate());

        verify(emailServiceImp, times(2)).sendEmail(any());
    }
}