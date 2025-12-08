package com.homemate.taskmanagementtests;


import com.homemate.taskmanagement.dao.impl.TaskDaoImpl;
import com.homemate.taskmanagement.dto.RescheduleRequestDto;
import com.homemate.taskmanagement.dto.RescheduleResponseDto;
import com.homemate.taskmanagement.exceptions.BadRescheduleException;
import com.homemate.taskmanagement.exceptions.TaskNotFoundException;
import com.homemate.taskmanagement.model.Status;
import com.homemate.taskmanagement.service.TaskManagementService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TaskManagementServiceRescheduleTest {
    @Mock
    private TaskDaoImpl taskDao;

    @InjectMocks
    private TaskManagementService taskManagementService;

    @Test
    void testValidReschedule() {
        long taskID = 10L;
        long requesterID = 5L;

        LocalDateTime futureDate = LocalDateTime.now().plusDays(2);

        RescheduleRequestDto requestDto = new RescheduleRequestDto();
        requestDto.setNewStartDate(futureDate);

        when(taskDao.getTaskerID(taskID)).thenReturn(Optional.of(requesterID));
        when(taskDao.getUserID(taskID)).thenReturn(Optional.of(20L));
        when(taskDao.getStatus(taskID)).thenReturn(Optional.of(Status.Accepted));
        when(taskDao.updateTaskStartDate(taskID, futureDate)).thenReturn(true);

        RescheduleResponseDto response = taskManagementService.rescheduleTask(taskID, requestDto, requesterID);

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

        when(taskDao.getTaskerID(taskID)).thenReturn(Optional.of(requesterID));
        when(taskDao.getUserID(taskID)).thenReturn(Optional.of(20L));
        when(taskDao.getStatus(taskID)).thenReturn(Optional.of(Status.Accepted));

        assertThrows(BadRescheduleException.class, () ->
                taskManagementService.rescheduleTask(taskID, requestDto, requesterID)
        );



    }
    @Test
    void testRequesterNotUserOrTasker() {
        long taskID = 10L;

        RescheduleRequestDto requestDto = new RescheduleRequestDto();
        requestDto.setNewStartDate(LocalDateTime.now().plusDays(1));

        when(taskDao.getTaskerID(taskID)).thenReturn(Optional.of(99L));
        when(taskDao.getUserID(taskID)).thenReturn(Optional.of(100L));

        long unauthorizedID = 50L;

        assertThrows(BadRescheduleException.class, () ->
                taskManagementService.rescheduleTask(taskID, requestDto, unauthorizedID)
        );
    }

    @Test
    void testTaskIdDoesNotExist() {
        long taskID = 10L;
        long requesterID = 5L;

        RescheduleRequestDto requestDto = new RescheduleRequestDto();
        requestDto.setNewStartDate(LocalDateTime.now().plusDays(1));

        when(taskDao.getTaskerID(taskID)).thenReturn(Optional.empty());

        assertThrows(TaskNotFoundException.class, () ->
                taskManagementService.rescheduleTask(taskID, requestDto, requesterID)
        );
    }

}
