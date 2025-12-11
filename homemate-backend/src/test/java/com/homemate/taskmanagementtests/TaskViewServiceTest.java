package com.homemate.taskmanagementtests;

import com.homemate.TaskerProfile.DTO.ReviewDTO;
import com.homemate.TaskerProfile.DTO.ReviewImageDTO;
import com.homemate.TaskerProfile.Dao.ReviewDao;
import com.homemate.taskmanagement.dao.TaskRequestDao;
import com.homemate.taskmanagement.dao.TaskRescheduleDao;
import com.homemate.taskmanagement.dao.TaskReviewDao;
import com.homemate.taskmanagement.dao.TaskStatusDao;
import com.homemate.taskmanagement.dto.TaskDto;
import com.homemate.taskmanagement.exceptions.BadViewException;
import com.homemate.taskmanagement.exceptions.TaskNotFoundException;
import com.homemate.taskmanagement.model.Status;
import com.homemate.taskmanagement.service.TaskViewService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TaskViewServiceTest {

    @Mock
    private TaskRequestDao taskRequestDao;

    @Mock
    private TaskStatusDao taskStatusDao;

    @Mock
    private TaskRescheduleDao taskRescheduleDao;

    @Mock
    private TaskReviewDao taskReviewDao;

    @Mock
    private ReviewDao reviewDao;

    @InjectMocks
    private TaskViewService taskViewService;

    private TaskDto taskDto;

    @BeforeEach
    void setUp() {
        taskDto = TaskDto.builder()
                .taskID(100L)
                .startDate(LocalDateTime.of(2025, 11, 20, 10, 0))
                .endDate(LocalDateTime.of(2025, 11, 20, 18, 0))
                .status(Status.InReview)
                .description("Test task description")
                .workedHours(0.0)
                .startInProgress(null)
                .bill(0.0)
                .userName("John Doe")
                .taskerName("Jane Smith")
                .serviceName("Plumbing")
                .chatID(50L)
                .addressDetails("123 Main St")
                .userMail("john@example.com")
                .taskerMail("jane@example.com")
                .build();
    }

    // ==================== getReviewByTaskId Tests ====================

    @Test
    void testGetReviewByTaskId_HappyPath() {
        long taskId = 10L;
        long viewerID = 100L;

        // Mock task exists
        when(taskRescheduleDao.getUserID(taskId)).thenReturn(Optional.of(100L));
        when(taskStatusDao.getTaskerID(taskId)).thenReturn(Optional.of(200L));

        // Mock review exists
        ReviewDTO reviewDTO = new ReviewDTO();
        reviewDTO.setReviewId(10);
        when(taskReviewDao.getReviewByTaskId(taskId)).thenReturn(Optional.of(reviewDTO));

        // Mock review images
        List<ReviewImageDTO> images = List.of(new ReviewImageDTO(), new ReviewImageDTO());
        when(reviewDao.getReviewImages(10)).thenReturn(images);

        Optional<ReviewDTO> result = taskViewService.getReviewByTaskId(taskId, viewerID);

        assertThat(result).isPresent();
        assertThat(result.get().getReviewImages()).hasSize(2);

        verify(taskRescheduleDao).getUserID(taskId);
        verify(taskStatusDao).getTaskerID(taskId);
        verify(taskReviewDao).getReviewByTaskId(taskId);
        verify(reviewDao).getReviewImages(10);
    }

    @Test
    void testGetReviewByTaskId_NoReview() {
        long taskId = 10L;
        long viewerID = 100L;

        // Mock task exists
        when(taskRescheduleDao.getUserID(taskId)).thenReturn(Optional.of(100L));
        when(taskStatusDao.getTaskerID(taskId)).thenReturn(Optional.of(200L));

        // Mock no review
        when(taskReviewDao.getReviewByTaskId(taskId)).thenReturn(Optional.empty());

        Optional<ReviewDTO> result = taskViewService.getReviewByTaskId(taskId, viewerID);

        assertThat(result).isEmpty();

        verify(taskRescheduleDao).getUserID(taskId);
        verify(taskStatusDao).getTaskerID(taskId);
        verify(taskReviewDao).getReviewByTaskId(taskId);
        verify(reviewDao, never()).getReviewImages(anyInt());
    }

    @Test
    void testGetReviewByTaskId_NotTheOwner() {
        long taskId = 10L;
        long viewerID = 100L;

        // Mock task exists with different user and tasker IDs
        when(taskRescheduleDao.getUserID(taskId)).thenReturn(Optional.of(200L));  // Different user
        when(taskStatusDao.getTaskerID(taskId)).thenReturn(Optional.of(300L)); // Different tasker

        // Assert that BadViewException is thrown
        assertThatThrownBy(() -> taskViewService.getReviewByTaskId(taskId, viewerID))
                .isInstanceOf(BadViewException.class)
                .hasMessage("You are neither the user nor the tasker for this task");

        // Verify that getReviewByTaskId was never called since exception is thrown first
        verify(taskReviewDao, never()).getReviewByTaskId(taskId);
    }

    @Test
    void testGetReviewByTaskId_TaskNotFound() {
        long taskId = 3L;
        long viewerID = 100L;

        // Mock task does not exist
        when(taskRescheduleDao.getUserID(taskId)).thenReturn(Optional.empty());
        when(taskStatusDao.getTaskerID(taskId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskViewService.getReviewByTaskId(taskId, viewerID))
                .isInstanceOf(TaskNotFoundException.class)
                .hasMessage("Task with ID 3 not found");

        verify(taskRescheduleDao).getUserID(taskId);
        verify(taskStatusDao).getTaskerID(taskId);
        verify(taskReviewDao, never()).getReviewByTaskId(taskId);
    }

    @Test
    void testGetReviewByTaskId_UnauthorizedAccess() {
        long taskId = 10L;
        long viewerID = 999L; // Not user nor tasker

        // Mock task exists
        when(taskRescheduleDao.getUserID(taskId)).thenReturn(Optional.of(100L));
        when(taskStatusDao.getTaskerID(taskId)).thenReturn(Optional.of(200L));

        // Expect exception
        assertThatThrownBy(() -> taskViewService.getReviewByTaskId(taskId, viewerID))
                .isInstanceOf(BadViewException.class)
                .hasMessage("You are neither the user nor the tasker for this task");

        verify(taskRescheduleDao).getUserID(taskId);
        verify(taskStatusDao).getTaskerID(taskId);
        verify(taskReviewDao, never()).getReviewByTaskId(taskId);
    }

    // ==================== getTaskDetails Tests ====================

    @Test
    void testGetTaskDetails_HappyPath_UserIsViewer() {
        long taskId = 1L;
        long viewerID = 100L;

        // Mock task exists
        when(taskStatusDao.getTaskerID(taskId)).thenReturn(Optional.of(200L));
        when(taskRescheduleDao.getUserID(taskId)).thenReturn(Optional.of(viewerID));

        TaskDto taskDto = new TaskDto();
        when(taskRequestDao.getTaskDetails(taskId)).thenReturn(Optional.of(taskDto));

        Optional<TaskDto> result = taskViewService.getTaskDetails(taskId, viewerID);

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(taskDto);

        verify(taskStatusDao).getTaskerID(taskId);
        verify(taskRescheduleDao).getUserID(taskId);
        verify(taskRequestDao).getTaskDetails(taskId);
    }

    @Test
    void testGetTaskDetails_Success() {
        // Arrange
        Long taskId = 100L;
        Long viewerId = 200L; // the user requesting the task details
        Long userId = 200L;   // owner user
        Long taskerId = 300L; // assigned tasker

        // mocks for authorization check
        when(taskStatusDao.getTaskerID(taskId)).thenReturn(Optional.of(taskerId));
        when(taskRescheduleDao.getUserID(taskId)).thenReturn(Optional.of(userId));

        // mock actual task details
        when(taskRequestDao.getTaskDetails(taskId)).thenReturn(Optional.of(taskDto));

        // Act
        Optional<TaskDto> result = taskViewService.getTaskDetails(taskId, viewerId);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get())
                .extracting(TaskDto::getTaskID, TaskDto::getUserName, TaskDto::getTaskerMail)
                .containsExactly(100L, "John Doe", "jane@example.com");

        // Verify all DAO calls
        verify(taskStatusDao).getTaskerID(taskId);
        verify(taskRescheduleDao).getUserID(taskId);
        verify(taskRequestDao).getTaskDetails(taskId);
    }

    @Test
    void testGetTaskDetails_ReturnsEmpty() {
        // Arrange
        Long taskId = 999L;
        Long viewerId = 200L;   // user requesting details
        Long userId = 200L;     // owner user
        Long taskerId = 300L;   // assigned tasker

        // Required authorization mocks
        when(taskStatusDao.getTaskerID(taskId)).thenReturn(Optional.of(taskerId));
        when(taskRescheduleDao.getUserID(taskId)).thenReturn(Optional.of(userId));

        // Task not found
        when(taskRequestDao.getTaskDetails(taskId)).thenReturn(Optional.empty());

        // Act
        Optional<TaskDto> result = taskViewService.getTaskDetails(taskId, viewerId);

        // Assert
        assertThat(result).isEmpty();

        // Verify all interactions
        verify(taskStatusDao).getTaskerID(taskId);
        verify(taskRescheduleDao).getUserID(taskId);
        verify(taskRequestDao).getTaskDetails(taskId);
    }

    @Test
    void testGetTaskDetails_TaskNotFound() {
        long taskId = 2L;
        long viewerID = 100L;

        // Mock task does not exist
        when(taskStatusDao.getTaskerID(taskId)).thenReturn(Optional.empty());
        when(taskRescheduleDao.getUserID(taskId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskViewService.getTaskDetails(taskId, viewerID))
                .isInstanceOf(TaskNotFoundException.class)
                .hasMessage("Task with ID 2 not found");

        verify(taskStatusDao).getTaskerID(taskId);
        verify(taskRescheduleDao).getUserID(taskId);
        verify(taskRequestDao, never()).getTaskDetails(anyLong());
    }

    @Test
    void testGetTaskDetails_ThrowsTaskNotFoundException_WhenTaskNotFound() {
        // Arrange
        Long taskId = 999L;
        Long viewerId = 200L;

        when(taskStatusDao.getTaskerID(taskId)).thenReturn(Optional.empty());
        when(taskRescheduleDao.getUserID(taskId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> taskViewService.getTaskDetails(taskId, viewerId))
                .isInstanceOf(TaskNotFoundException.class)
                .hasMessageContaining("Task with ID " + taskId + " not found");

        verify(taskStatusDao).getTaskerID(taskId);
        verify(taskRescheduleDao).getUserID(taskId);
        verify(taskRequestDao, never()).getTaskDetails(anyLong());
    }

    @Test
    void testGetTaskDetails_UnauthorizedAccess() {
        long taskId = 3L;
        long viewerID = 300L;

        // Mock task exists
        when(taskStatusDao.getTaskerID(taskId)).thenReturn(Optional.of(100L));
        when(taskRescheduleDao.getUserID(taskId)).thenReturn(Optional.of(200L));

        assertThatThrownBy(() -> taskViewService.getTaskDetails(taskId, viewerID))
                .isInstanceOf(BadViewException.class)
                .hasMessage("You are neither the user nor the tasker for this task");

        verify(taskStatusDao).getTaskerID(taskId);
        verify(taskRescheduleDao).getUserID(taskId);
        verify(taskRequestDao, never()).getTaskDetails(anyLong());
    }
}