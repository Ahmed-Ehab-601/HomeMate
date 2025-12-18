package com.homemate.taskmanagementtests;

import com.homemate.notification.service.imp.EmailServiceImp;
import com.homemate.taskmanagement.dao.TaskRequestDao;
import com.homemate.taskmanagement.dao.TaskStatusDao;
import com.homemate.taskmanagement.dto.TaskDto;
import com.homemate.taskmanagement.exceptions.BadAcceptRejectException;
import com.homemate.taskmanagement.exceptions.TaskNotFoundException;
import com.homemate.taskmanagement.model.Status;
import com.homemate.taskmanagement.service.TaskStatusService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskManagementServiceAcceptRejectTest {

    @Mock
    private TaskStatusDao taskStatusDao;
    @Mock
    private TaskRequestDao taskRequestDao;

    @Mock
    private SimpMessagingTemplate simpMessagingTemplate;

    @Mock
    private EmailServiceImp emailServiceImp;

    @InjectMocks
    private TaskStatusService taskStatusService;

    @Test
    void testThatAcceptOrRejectSuccessfullyAcceptsTask() {
        // Arrange
        Long taskId = 1L;
        Long taskerId = 10L;
        Status newStatus = Status.Accepted;

        when(taskStatusDao.getStatus(taskId)).thenReturn(Optional.of(Status.InReview));
        when(taskStatusDao.getTaskerID(taskId)).thenReturn(Optional.of(taskerId));
        when(taskStatusDao.updateStatus(taskId, newStatus)).thenReturn(true);
        when(taskRequestDao.getTaskDetails(taskId)).thenReturn(Optional.of(new TaskDto()));

        // Act & Assert
        assertThatCode(() -> taskStatusService.acceptOrReject(taskId, taskerId, newStatus))
                .doesNotThrowAnyException();

        verify(taskStatusDao).getStatus(taskId);
        verify(taskStatusDao).getTaskerID(taskId);
        verify(taskStatusDao).updateStatus(taskId, newStatus);
    }

    @Test
    void testThatAcceptOrRejectSuccessfullyRejectsTask() {
        // Arrange
        Long taskId = 2L;
        Long taskerId = 5L;
        Status newStatus = Status.Rejected;

        when(taskStatusDao.getStatus(taskId)).thenReturn(Optional.of(Status.InReview));
        when(taskStatusDao.getTaskerID(taskId)).thenReturn(Optional.of(taskerId));
        when(taskStatusDao.updateStatus(taskId, newStatus)).thenReturn(true);
        when(taskRequestDao.getTaskDetails(taskId)).thenReturn(Optional.of(new TaskDto()));

        // Act & Assert
        assertThatCode(() -> taskStatusService.acceptOrReject(taskId, taskerId, newStatus))
                .doesNotThrowAnyException();

        verify(taskStatusDao).getStatus(taskId);
        verify(taskStatusDao).getTaskerID(taskId);
        verify(taskStatusDao).updateStatus(taskId, newStatus);
    }

    @Test
    void testThatAcceptOrRejectThrowsTaskNotFoundExceptionWhenTaskDoesNotExist() {
        // Arrange
        Long taskId = 999L;
        Long taskerId = 10L;
        Status newStatus = Status.Accepted;

        when(taskStatusDao.getStatus(taskId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> taskStatusService.acceptOrReject(taskId, taskerId, newStatus))
                .isInstanceOf(TaskNotFoundException.class)
                .hasMessage("wrong task id");

        verify(taskStatusDao).getStatus(taskId);
        verify(taskStatusDao, never()).getTaskerID(anyLong());
        verify(taskStatusDao, never()).updateStatus(anyLong(), any(Status.class));
    }

    @Test
    void testThatAcceptOrRejectThrowsBadAcceptRejectExceptionWhenTaskNotInReview() {
        // Arrange
        Long taskId = 1L;
        Long taskerId = 10L;
        Status newStatus = Status.Accepted;
        when(taskStatusDao.getStatus(taskId)).thenReturn(Optional.of(Status.Accepted));
        when(taskStatusDao.getTaskerID(taskId)).thenReturn(Optional.of(taskerId));

        // Act & Assert
        assertThatThrownBy(() -> taskStatusService.acceptOrReject(taskId, taskerId, newStatus))
                .isInstanceOf(BadAcceptRejectException.class)
                .hasMessage("bad request the task must be inReview");

        verify(taskStatusDao).getStatus(taskId);
        verify(taskStatusDao).getTaskerID(taskId);

        verify(taskStatusDao, never())
                .updateStatus(anyLong(), any());
    }

    @Test
    void testThatAcceptOrRejectThrowsBadAcceptRejectExceptionWhenTaskerIDNotFound() {
        // Arrange
        Long taskId = 1L;
        Long taskerId = 10L;
        Status newStatus = Status.Accepted;

        when(taskStatusDao.getStatus(taskId)).thenReturn(Optional.of(Status.InReview));
        when(taskStatusDao.getTaskerID(taskId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> taskStatusService.acceptOrReject(taskId, taskerId, newStatus))
                .isInstanceOf(BadAcceptRejectException.class)
                .hasMessage("the task id does not belong to this tasker");

        verify(taskStatusDao).getStatus(taskId);
        verify(taskStatusDao).getTaskerID(taskId);
        verify(taskStatusDao, never()).updateStatus(anyLong(), any(Status.class));
    }

    @Test
    void testThatAcceptOrRejectThrowsBadAcceptRejectExceptionWhenTaskerIDDoesNotMatch() {
        // Arrange
        Long taskId = 1L;
        Long correctTaskerId = 10L;
        Long wrongTaskerId = 20L;
        Status newStatus = Status.Accepted;

        when(taskStatusDao.getStatus(taskId)).thenReturn(Optional.of(Status.InReview));
        when(taskStatusDao.getTaskerID(taskId)).thenReturn(Optional.of(correctTaskerId));

        // Act & Assert
        assertThatThrownBy(() -> taskStatusService.acceptOrReject(taskId, wrongTaskerId, newStatus))
                .isInstanceOf(BadAcceptRejectException.class)
                .hasMessage("the task id does not belong to this tasker");

        verify(taskStatusDao).getStatus(taskId);
        verify(taskStatusDao).getTaskerID(taskId);
        verify(taskStatusDao, never()).updateStatus(anyLong(), any(Status.class));
    }


    @Test
    void testAcceptOrRejectSendsEmailSuccessfully() {
        // Arrange
        Long taskId = 1L;
        Long taskerId = 10L;
        Status newStatus = Status.Accepted;

        TaskDto taskDto = new TaskDto();
        taskDto.setUserMail("user@example.com");

        when(taskStatusDao.getStatus(taskId)).thenReturn(Optional.of(Status.InReview));
        when(taskStatusDao.getTaskerID(taskId)).thenReturn(Optional.of(taskerId));
        when(taskStatusDao.updateStatus(taskId, newStatus)).thenReturn(true);
        when(taskRequestDao.getTaskDetails(taskId)).thenReturn(Optional.of(taskDto));

        // Act & Assert
        assertThatCode(() -> taskStatusService.acceptOrReject(taskId, taskerId, newStatus))
                .doesNotThrowAnyException();

        // Verify email was sent
        verify(emailServiceImp).sendUserEmail(any());
    }


    @Test
    void testAcceptOrRejectEmailFails() {
        // Arrange
        Long taskId = 1L;
        Long taskerId = 10L;
        Status newStatus = Status.Accepted;

        TaskDto taskDto = new TaskDto();
        taskDto.setUserMail("user@example.com");

        when(taskStatusDao.getStatus(taskId)).thenReturn(Optional.of(Status.InReview));
        when(taskStatusDao.getTaskerID(taskId)).thenReturn(Optional.of(taskerId));
        when(taskStatusDao.updateStatus(taskId, newStatus)).thenReturn(true);
        when(taskRequestDao.getTaskDetails(taskId)).thenReturn(Optional.of(taskDto));

        // Simulate email sending failure
        doThrow(new RuntimeException("Email failed")).when(emailServiceImp).sendUserEmail(any());

        // Act & Assert
        assertThatThrownBy(() -> taskStatusService.acceptOrReject(taskId, taskerId, newStatus))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Email failed");
    }


}