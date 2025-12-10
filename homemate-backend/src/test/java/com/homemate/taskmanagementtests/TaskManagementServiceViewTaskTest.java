package com.homemate.taskmanagementtests;

import com.homemate.TaskerProfile.DTO.ReviewDTO;
import com.homemate.TaskerProfile.DTO.ReviewImageDTO;
import com.homemate.TaskerProfile.Dao.ReviewDao;
import com.homemate.taskmanagement.dao.impl.TaskDaoImpl;
import com.homemate.taskmanagement.dto.TaskDto;
import com.homemate.taskmanagement.exceptions.BadViewExecption;
import com.homemate.taskmanagement.exceptions.TaskNotFoundException;
import com.homemate.taskmanagement.mappers.TaskMapper;
import com.homemate.taskmanagement.service.TaskManagementService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class TaskManagementServiceViewTaskTest {
    @Mock
    private TaskMapper taskMapper;

    @Mock
    private TaskDaoImpl taskDao;

    @Mock
    private ReviewDao reviewDao;

    @InjectMocks
    private TaskManagementService taskManagementService;

    @Test
    void testGetReviewByTaskId_HappyPath() {
        long taskId = 10L;
        long viewerID = 100L;

        // Mock task exists
        when(taskDao.getUserID(taskId)).thenReturn(Optional.of(100L));
        when(taskDao.getTaskerID(taskId)).thenReturn(Optional.of(200L));

        // Mock review exists
        ReviewDTO reviewDTO = new ReviewDTO();
        reviewDTO.setReviewId(10);
        when(taskDao.getReviewByTaskId(taskId)).thenReturn(Optional.of(reviewDTO));

        // Mock review images
        List<ReviewImageDTO> images = List.of(new ReviewImageDTO(), new ReviewImageDTO());
        when(reviewDao.getReviewImages(10)).thenReturn(images);

        Optional<ReviewDTO> result = taskManagementService.getReviewByTaskId(taskId, viewerID);

        assertTrue(result.isPresent());
        assertEquals(2, result.get().getReviewImages().size());
    }


    @Test
    void testGetReviewByTaskId_NoReview() {
        long taskId = 10L;
        long viewerID = 100L;

        // Mock task exists
        when(taskDao.getUserID(taskId)).thenReturn(Optional.of(100L));
        when(taskDao.getTaskerID(taskId)).thenReturn(Optional.of(200L));

        // Mock no review
        when(taskDao.getReviewByTaskId(taskId)).thenReturn(Optional.empty());

        Optional<ReviewDTO> result = taskManagementService.getReviewByTaskId(taskId, viewerID);

        assertTrue(result.isEmpty());
    }

    @Test
    void testGetReviewByTaskId_NotTheOwner() {
        long taskId = 10L;
        long viewerID = 100L;

        // Mock task exists with different user and tasker IDs
        when(taskDao.getUserID(taskId)).thenReturn(Optional.of(200L));  // Different user
        when(taskDao.getTaskerID(taskId)).thenReturn(Optional.of(300L)); // Different tasker

        // Assert that BadViewException is thrown
        assertThrows(BadViewExecption.class, () -> {
            taskManagementService.getReviewByTaskId(taskId, viewerID);
        });

        // Verify that getReviewByTaskId was never called since exception is thrown first
        verify(taskDao, never()).getReviewByTaskId(taskId);
    }



    @Test
    void testGetReviewByTaskId_TaskNotFound() {
        long taskId = 3L;
        long viewerID = 100L; // Any viewer ID for this test

        // Mock task does not exist
        when(taskDao.getUserID(taskId)).thenReturn(Optional.empty());

        TaskNotFoundException exception = assertThrows(TaskNotFoundException.class, () -> {
            taskManagementService.getReviewByTaskId(taskId, viewerID);
        });

        assertEquals("Task with ID 3 not found", exception.getMessage());
    }

    @Test
    void testGetReviewByTaskId_UnauthorizedAccess() {
        long taskId = 10L;
        long viewerID = 999L; // Not user nor tasker

        // Mock task exists
        when(taskDao.getUserID(taskId)).thenReturn(Optional.of(100L));
        when(taskDao.getTaskerID(taskId)).thenReturn(Optional.of(200L));

        // Expect exception
        assertThrows(BadViewExecption.class,
                () -> taskManagementService.getReviewByTaskId(taskId, viewerID));
    }


    @Test
    void testGetTaskDetails_HappyPath_UserIsViewer() {
        long taskId = 1L;
        long viewerID = 100L;

        // Mock task exists
        when(taskDao.getTaskerID(taskId)).thenReturn(Optional.of(200L));
        when(taskDao.getUserID(taskId)).thenReturn(Optional.of(viewerID));

        TaskDto taskDto = new TaskDto();
        when(taskDao.getTaskDetails(taskId)).thenReturn(Optional.of(taskDto));

        Optional<TaskDto> result = taskManagementService.getTaskDetails(taskId, viewerID);

        assertTrue(result.isPresent());
        assertEquals(taskDto, result.get());
    }

    @Test
    void testGetTaskDetails_TaskNotFound() {
        long taskId = 2L;
        long viewerID = 100L;

        // Mock task does not exist
        when(taskDao.getTaskerID(taskId)).thenReturn(Optional.empty());
        when(taskDao.getUserID(taskId)).thenReturn(Optional.empty());

        TaskNotFoundException exception = assertThrows(TaskNotFoundException.class, () -> {
            taskManagementService.getTaskDetails(taskId, viewerID);
        });

        assertEquals("Task with ID 2 not found", exception.getMessage());
    }

    @Test
    void testGetTaskDetails_UnauthorizedAccess() {
        long taskId = 3L;
        long viewerID = 300L;

        // Mock task exists
        when(taskDao.getTaskerID(taskId)).thenReturn(Optional.of(100L));
        when(taskDao.getUserID(taskId)).thenReturn(Optional.of(200L));

        BadViewExecption exception = assertThrows(BadViewExecption.class, () -> {
            taskManagementService.getTaskDetails(taskId, viewerID);
        });

        assertEquals("You are neither the user nor the tasker for this task", exception.getMessage());
    }

    @Test
    void testViewerIDEquals_ReturnsTrue_WhenIdsMatch() {
        long viewerID = 100L;
        Long id = 100L;

        boolean result = taskManagementService.viewerIDEquals(id, viewerID);

        assertTrue(result);
    }

    @Test
    void testViewerIDEquals_ReturnsFalse_WhenIdsDoNotMatch() {
        long viewerID = 100L;
        Long id = 200L;

        boolean result = taskManagementService.viewerIDEquals(id, viewerID);

        assertFalse(result);
    }

    @Test
    void testViewerIDEquals_ReturnsFalse_WhenIdIsNull() {
        long viewerID = 100L;
        Long id = null;

        boolean result = taskManagementService.viewerIDEquals(id, viewerID);

        assertFalse(result);
    }




}
