package com.homemate.taskmanagementtests;

import com.homemate.taskmanagement.dao.impl.TaskDaoImpl;
import com.homemate.taskmanagement.exceptions.BadAcceptRejectException;
import com.homemate.taskmanagement.exceptions.TaskNotFoundException;
import com.homemate.taskmanagement.model.Status;
import com.homemate.taskmanagement.service.TaskManagementService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskManagementServiceAcceptRejectTest {

    @Mock
    private TaskDaoImpl taskDao;

    @InjectMocks
    private TaskManagementService taskManagementService;

    @Test
    void testThatAcceptOrRejectSuccessfullyAcceptsTask() {
        // Arrange
        Long taskId = 1L;
        Long taskerId = 10L;
        Status newStatus = Status.Accepted;

        when(taskDao.getStatus(taskId)).thenReturn(Optional.of(Status.InReview));
        when(taskDao.getTaskerID(taskId)).thenReturn(Optional.of(taskerId));
        when(taskDao.updateStatus(taskId, newStatus)).thenReturn(true);

        // Act & Assert
        assertThatCode(() -> taskManagementService.acceptOrReject(taskId, taskerId, newStatus))
                .doesNotThrowAnyException();

        verify(taskDao).getStatus(taskId);
        verify(taskDao).getTaskerID(taskId);
        verify(taskDao).updateStatus(taskId, newStatus);
    }

    @Test
    void testThatAcceptOrRejectSuccessfullyRejectsTask() {
        // Arrange
        Long taskId = 2L;
        Long taskerId = 5L;
        Status newStatus = Status.Rejected;

        when(taskDao.getStatus(taskId)).thenReturn(Optional.of(Status.InReview));
        when(taskDao.getTaskerID(taskId)).thenReturn(Optional.of(taskerId));
        when(taskDao.updateStatus(taskId, newStatus)).thenReturn(true);

        // Act & Assert
        assertThatCode(() -> taskManagementService.acceptOrReject(taskId, taskerId, newStatus))
                .doesNotThrowAnyException();

        verify(taskDao).getStatus(taskId);
        verify(taskDao).getTaskerID(taskId);
        verify(taskDao).updateStatus(taskId, newStatus);
    }

    @Test
    void testThatAcceptOrRejectThrowsTaskNotFoundExceptionWhenTaskDoesNotExist() {
        // Arrange
        Long taskId = 999L;
        Long taskerId = 10L;
        Status newStatus = Status.Accepted;

        when(taskDao.getStatus(taskId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> taskManagementService.acceptOrReject(taskId, taskerId, newStatus))
                .isInstanceOf(TaskNotFoundException.class)
                .hasMessage("wrong task id");

        verify(taskDao).getStatus(taskId);
        verify(taskDao, never()).getTaskerID(anyLong());
        verify(taskDao, never()).updateStatus(anyLong(), any(Status.class));
    }

    @Test
    void testThatAcceptOrRejectThrowsBadAcceptRejectExceptionWhenTaskNotInReview() {
        // Arrange
        Long taskId = 1L;
        Long taskerId = 10L;
        Status newStatus = Status.Accepted;

        when(taskDao.getStatus(taskId)).thenReturn(Optional.of(Status.Accepted));

        // Act & Assert
        assertThatThrownBy(() -> taskManagementService.acceptOrReject(taskId, taskerId, newStatus))
                .isInstanceOf(BadAcceptRejectException.class)
                .hasMessage("bad request the task must be inReview");

        verify(taskDao).getStatus(taskId);
        verify(taskDao, never()).getTaskerID(anyLong());
        verify(taskDao, never()).updateStatus(anyLong(), any(Status.class));
    }

    @Test
    void testThatAcceptOrRejectThrowsBadAcceptRejectExceptionWhenTaskerIDNotFound() {
        // Arrange
        Long taskId = 1L;
        Long taskerId = 10L;
        Status newStatus = Status.Accepted;

        when(taskDao.getStatus(taskId)).thenReturn(Optional.of(Status.InReview));
        when(taskDao.getTaskerID(taskId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> taskManagementService.acceptOrReject(taskId, taskerId, newStatus))
                .isInstanceOf(BadAcceptRejectException.class)
                .hasMessage("the task id don't belong to this tasker");

        verify(taskDao).getStatus(taskId);
        verify(taskDao).getTaskerID(taskId);
        verify(taskDao, never()).updateStatus(anyLong(), any(Status.class));
    }

    @Test
    void testThatAcceptOrRejectThrowsBadAcceptRejectExceptionWhenTaskerIDDoesNotMatch() {
        // Arrange
        Long taskId = 1L;
        Long correctTaskerId = 10L;
        Long wrongTaskerId = 20L;
        Status newStatus = Status.Accepted;

        when(taskDao.getStatus(taskId)).thenReturn(Optional.of(Status.InReview));
        when(taskDao.getTaskerID(taskId)).thenReturn(Optional.of(correctTaskerId));

        // Act & Assert
        assertThatThrownBy(() -> taskManagementService.acceptOrReject(taskId, wrongTaskerId, newStatus))
                .isInstanceOf(BadAcceptRejectException.class)
                .hasMessage("the task id don't belong to this tasker");

        verify(taskDao).getStatus(taskId);
        verify(taskDao).getTaskerID(taskId);
        verify(taskDao, never()).updateStatus(anyLong(), any(Status.class));
    }

}