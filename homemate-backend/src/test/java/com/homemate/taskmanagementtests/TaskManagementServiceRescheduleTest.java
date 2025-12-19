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

        LocalDateTime futureDate = LocalDateTime.now().plusDays(2);
        RescheduleRequestDto requestDto = new RescheduleRequestDto();
        requestDto.setNewStartDate(futureDate);

        TaskDto taskDto = new TaskDto();
        taskDto.setUserMail("user@example.com");
        taskDto.setTaskerMail("tasker@example.com");

        when(taskStatusDao.getTaskerID(taskID)).thenReturn(Optional.of(requesterID));
        when(taskDao.getUserID(taskID)).thenReturn(Optional.of(20L));
        when(taskStatusDao.getStatus(taskID)).thenReturn(Optional.of(Status.Accepted));
        when(taskDao.updateTaskStartDate(taskID, futureDate)).thenReturn(true);
        when(taskRequestDao.getTaskDetails(taskID)).thenReturn(Optional.of(taskDto));

        RescheduleResponseDto response = taskRescheduleService.rescheduleTask(taskID, requestDto, requesterID);

        assertEquals(taskID, response.getTaskID());
        assertEquals(futureDate, response.getNewStartDate());
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

        when(taskStatusDao.getTaskerID(taskID)).thenReturn(Optional.of(requesterID));
        when(taskDao.getUserID(taskID)).thenReturn(Optional.of(20L));
        when(taskStatusDao.getStatus(taskID)).thenReturn(Optional.of(Status.Accepted));

        assertThrows(BadRescheduleException.class, () ->
                taskRescheduleService.rescheduleTask(taskID, requestDto, requesterID)
        );
    }

    @Test
    void testRequesterNotUserOrTasker() {
        long taskID = 10L;

        RescheduleRequestDto requestDto = new RescheduleRequestDto();
        requestDto.setNewStartDate(LocalDateTime.now().plusDays(1));

        TaskDto taskDto = new TaskDto();
        taskDto.setUserMail("user@example.com");
        taskDto.setTaskerMail("tasker@example.com");

        when(taskStatusDao.getTaskerID(taskID)).thenReturn(Optional.of(99L));
        when(taskDao.getUserID(taskID)).thenReturn(Optional.of(100L));


        long unauthorizedID = 50L;

        assertThrows(BadRescheduleException.class, () ->
                taskRescheduleService.rescheduleTask(taskID, requestDto, unauthorizedID)
        );
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
    }

    @Test
    void testRescheduleSendsEmailSuccessfully() {
        // Arrange
        long taskID = 10L;
        long requesterID = 5L;
        LocalDateTime futureDate = LocalDateTime.now().plusDays(2);

        RescheduleRequestDto requestDto = new RescheduleRequestDto();
        requestDto.setNewStartDate(futureDate);

        TaskDto taskDto = new TaskDto();
        taskDto.setUserMail("user@example.com");
        taskDto.setTaskerMail("tasker@example.com");

        when(taskStatusDao.getTaskerID(taskID)).thenReturn(Optional.of(requesterID));
        when(taskDao.getUserID(taskID)).thenReturn(Optional.of(20L));
        when(taskStatusDao.getStatus(taskID)).thenReturn(Optional.of(Status.Accepted));
        when(taskDao.updateTaskStartDate(taskID, futureDate)).thenReturn(true);
        when(taskRequestDao.getTaskDetails(taskID)).thenReturn(Optional.of(taskDto));

        // Act
        taskRescheduleService.rescheduleTask(taskID, requestDto, requesterID);

        // Assert: email sent
        verify(emailServiceImp).sendUserEmail(any());
    }


    @Test
    void testRescheduleEmailFails() {
        // Arrange
        long taskID = 10L;
        long requesterID = 5L;
        LocalDateTime futureDate = LocalDateTime.now().plusDays(2);

        RescheduleRequestDto requestDto = new RescheduleRequestDto();
        requestDto.setNewStartDate(futureDate);

        TaskDto taskDto = new TaskDto();
        taskDto.setUserMail("user@example.com");
        taskDto.setTaskerMail("tasker@example.com");

        when(taskStatusDao.getTaskerID(taskID)).thenReturn(Optional.of(requesterID));
        when(taskDao.getUserID(taskID)).thenReturn(Optional.of(20L));
        when(taskStatusDao.getStatus(taskID)).thenReturn(Optional.of(Status.Accepted));
        when(taskDao.updateTaskStartDate(taskID, futureDate)).thenReturn(true);
        when(taskRequestDao.getTaskDetails(taskID)).thenReturn(Optional.of(taskDto));

        // Simulate email failure
        doThrow(new RuntimeException("Email failed")).when(emailServiceImp).sendUserEmail(any());

        // Act & Assert
        assertThrows(RuntimeException.class, () ->
                taskRescheduleService.rescheduleTask(taskID, requestDto, requesterID)
        );
    }


}
