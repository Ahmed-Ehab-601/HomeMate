package com.homemate.taskmanagementtests;

import com.homemate.notification.domains.dto.EmailRequest;
import com.homemate.notification.domains.model.EmailType;
import com.homemate.notification.service.impl.EmailServiceImpl;
import com.homemate.taskmanagement.dao.TaskRequestDao;
import com.homemate.taskmanagement.dao.TaskStatusDao;
import com.homemate.taskmanagement.dto.TaskDto;
import com.homemate.taskmanagement.exceptions.BadAcceptRejectException;
import com.homemate.taskmanagement.exceptions.BadStateUpdateException;
import com.homemate.taskmanagement.exceptions.TaskNotFoundException;
import com.homemate.taskmanagement.model.Status;
import com.homemate.taskmanagement.service.state.Accepted;
import com.homemate.taskmanagement.service.stateFactory.StatusFactory;
import com.homemate.taskmanagement.service.TaskStatusService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskManagementServiceUpdateStatusTest {

    @Mock
    private TaskStatusDao taskStatusDao;
    @Mock
    private TaskRequestDao taskRequestDao;

    @Mock
    private SimpMessagingTemplate simpMessagingTemplate;

    @Mock
    private EmailServiceImpl emailServiceImp;

    @InjectMocks
    private TaskStatusService taskStatusService;
    @InjectMocks
    private Accepted accepted;

    private Long taskId;
    private Long taskerId;
    private TaskDto taskDto;


    @BeforeEach
    void setUp() {
        taskId = 1L;
        taskerId = 10L;

        taskDto = TaskDto.builder()
                .taskID(taskId)
                .status(Status.InProgress)
                .taskerName("John Tasker")
                .userName("Jane User")
                .build();


        taskStatusService = new TaskStatusService(new StatusFactory(taskStatusDao),simpMessagingTemplate,emailServiceImp,taskStatusDao,taskRequestDao);
    }

    // ========== Valid State Transitions ==========

    @Test
    void testThatUpdateTaskStatusFromAcceptedToInProgressSucceeds() {
        // Given
        Status currentStatus = Status.Accepted;
        Status newStatus = Status.InProgress;

        when(taskStatusDao.getStatus(taskId)).thenReturn(Optional.of(currentStatus));
        when(taskStatusDao.getTaskerID(taskId)).thenReturn(Optional.of(taskerId));
        when(taskStatusDao.updateStatus(eq(taskId), eq(newStatus))).thenReturn(true);
        when(taskStatusDao.updateTaskStartInProgress(eq(taskId), any(Timestamp.class))).thenReturn(true);

        taskDto.setStatus(newStatus);
        when(taskRequestDao.getTaskDetails(taskId)).thenReturn(Optional.of(taskDto));

        // When
        TaskDto result = taskStatusService.updateTaskStatus(taskId, taskerId, newStatus);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(newStatus);

        verify(taskStatusDao).getStatus(taskId);
        verify(taskStatusDao).getTaskerID(taskId);
        verify(taskStatusDao).updateTaskStartInProgress(eq(taskId), any(Timestamp.class));
        verify(taskStatusDao).updateStatus(taskId, newStatus);
        verify(taskRequestDao).getTaskDetails(taskId);
    }

    @Test
    void testThatUpdateTaskStatusFromInProgressToSuspendedSucceeds() {
        // Given
        Status currentStatus = Status.InProgress;
        Status newStatus = Status.Suspended;
        Timestamp startTime = Timestamp.valueOf(LocalDateTime.now().minusHours(2));

        when(taskStatusDao.getStatus(taskId)).thenReturn(Optional.of(currentStatus));
        when(taskStatusDao.getTaskerID(taskId)).thenReturn(Optional.of(taskerId));
        when(taskStatusDao.updateTaskWorkedHours(eq(taskId), anyDouble())).thenReturn(true);
        when(taskStatusDao.updateTaskStartInProgress(taskId, null)).thenReturn(true);
        when(taskStatusDao.updateStatus(taskId, newStatus)).thenReturn(true);
        when(taskStatusDao.getStartInProgress(taskId)).thenReturn(startTime); // Add this line


        taskDto.setStatus(newStatus);
        when(taskRequestDao.getTaskDetails(taskId)).thenReturn(Optional.of(taskDto));

        // When
        TaskDto result = taskStatusService.updateTaskStatus(taskId, taskerId, newStatus);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(newStatus);

        verify(taskStatusDao).getStartInProgress(taskId);
        verify(taskStatusDao).updateTaskWorkedHours(eq(taskId), anyDouble());
        verify(taskStatusDao).updateTaskStartInProgress(taskId, null);
        verify(taskStatusDao).updateStatus(taskId, newStatus);
    }

    @Test
    void testThatUpdateTaskStatusFromSuspendedToInProgressSucceeds() {
        // Given
        Status currentStatus = Status.Suspended;
        Status newStatus = Status.InProgress;

        when(taskStatusDao.getStatus(taskId)).thenReturn(Optional.of(currentStatus));
        when(taskStatusDao.getTaskerID(taskId)).thenReturn(Optional.of(taskerId));
        when(taskStatusDao.updateStatus(taskId, newStatus)).thenReturn(true);
        when(taskStatusDao.updateTaskStartInProgress(eq(taskId), any(Timestamp.class))).thenReturn(true);

        taskDto.setStatus(newStatus);
        when(taskRequestDao.getTaskDetails(taskId)).thenReturn(Optional.of(taskDto));

        // When
        TaskDto result = taskStatusService.updateTaskStatus(taskId, taskerId, newStatus);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(newStatus);

        verify(taskStatusDao).updateTaskStartInProgress(eq(taskId), any(Timestamp.class));
        verify(taskStatusDao).updateStatus(taskId, newStatus);
    }

    @Test
    void testThatUpdateTaskStatusFromSuspendedToDoneSucceeds() {
        // Given
        Status currentStatus = Status.Suspended;
        Status newStatus = Status.Done;
        Timestamp startTime = Timestamp.valueOf(LocalDateTime.now().minusHours(3));
        Double hourRate = 50.0;
        Double workedHours = 5.0;

        when(taskStatusDao.getStatus(taskId)).thenReturn(Optional.of(currentStatus));
        when(taskStatusDao.getTaskerID(taskId)).thenReturn(Optional.of(taskerId));
        when(taskStatusDao.getStartInProgress(taskId)).thenReturn(startTime);
        when(taskStatusDao.updateTaskWorkedHours(eq(taskId), anyDouble())).thenReturn(true);
        when(taskStatusDao.updateTaskStartInProgress(taskId, null)).thenReturn(true);
        when(taskStatusDao.getTaskerHourRate(taskerId)).thenReturn(hourRate);
        when(taskStatusDao.getTaskWorkedHours(taskId)).thenReturn(workedHours);
        when(taskStatusDao.updateTaskerWorkedHours(taskerId, workedHours)).thenReturn(true);
        when(taskStatusDao.updateTaskBill(eq(taskId), anyDouble())).thenReturn(true);
        when(taskStatusDao.updateTaskerTotalEarning(eq(taskerId), anyDouble())).thenReturn(true);
        when(taskStatusDao.updateTaskEndData(eq(taskId), any(Timestamp.class))).thenReturn(true);
        when(taskStatusDao.updateStatus(taskId, newStatus)).thenReturn(true);

        taskDto.setStatus(newStatus);
        when(taskRequestDao.getTaskDetails(taskId)).thenReturn(Optional.of(taskDto));

        // When
        TaskDto result = taskStatusService.updateTaskStatus(taskId, taskerId, newStatus);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(newStatus);

        verify(taskStatusDao).getTaskerHourRate(taskerId);
        verify(taskStatusDao).getTaskWorkedHours(taskId);
        verify(taskStatusDao).updateTaskerWorkedHours(taskerId, workedHours);
        verify(taskStatusDao).updateTaskBill(eq(taskId), eq(hourRate * workedHours));
        verify(taskStatusDao).updateTaskerTotalEarning(eq(taskerId), eq(hourRate * workedHours));
        verify(taskStatusDao).updateTaskEndData(eq(taskId), any(Timestamp.class));
        verify(taskStatusDao).updateStatus(taskId, newStatus);
    }

    @Test
    void testThatUpdateTaskStatusFromInProgressToDoneSucceeds() {
        // Given
        Status currentStatus = Status.InProgress;
        Status newStatus = Status.Done;
        Timestamp startTime = Timestamp.valueOf(LocalDateTime.now().minusHours(4));
        Double hourRate = 45.0;
        double workedHours = 6.0;

        when(taskStatusDao.getStatus(taskId)).thenReturn(Optional.of(currentStatus));
        when(taskStatusDao.getTaskerID(taskId)).thenReturn(Optional.of(taskerId));
        when(taskStatusDao.getStartInProgress(taskId)).thenReturn(startTime);
        when(taskStatusDao.updateTaskWorkedHours(eq(taskId), anyDouble())).thenReturn(true);
        when(taskStatusDao.updateTaskStartInProgress(taskId, null)).thenReturn(true);
        when(taskStatusDao.getTaskerHourRate(taskerId)).thenReturn(hourRate);
        when(taskStatusDao.getTaskWorkedHours(taskId)).thenReturn(workedHours);
        when(taskStatusDao.updateTaskerWorkedHours(taskerId, workedHours)).thenReturn(true);
        when(taskStatusDao.updateTaskBill(eq(taskId), anyDouble())).thenReturn(true);
        when(taskStatusDao.updateTaskerTotalEarning(eq(taskerId), anyDouble())).thenReturn(true);
        when(taskStatusDao.updateTaskEndData(eq(taskId), any(Timestamp.class))).thenReturn(true);
        when(taskStatusDao.updateStatus(taskId, newStatus)).thenReturn(true);

        taskDto.setStatus(newStatus);
        when(taskRequestDao.getTaskDetails(taskId)).thenReturn(Optional.of(taskDto));

        // When
        TaskDto result = taskStatusService.updateTaskStatus(taskId, taskerId, newStatus);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(newStatus);

        verify(taskStatusDao).getStartInProgress(taskId);
        verify(taskStatusDao).updateTaskWorkedHours(eq(taskId), anyDouble());
        verify(taskStatusDao).updateStatus(taskId, newStatus);
    }

    // ========== Invalid State Transitions ==========

    @Test
    void testThatUpdateTaskStatusThrowsExceptionWhenTaskNotFound() {
        // Given
        Status newStatus = Status.InProgress;
        when(taskStatusDao.getStatus(taskId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> taskStatusService.updateTaskStatus(taskId, taskerId, newStatus))
                .isInstanceOf(TaskNotFoundException.class)
                .hasMessage("wrong task id");

        verify(taskStatusDao).getStatus(taskId);
        verify(taskStatusDao, never()).getTaskerID(anyLong());
        verify(taskStatusDao, never()).updateStatus(anyLong(), any(Status.class));
    }

    @Test
    void testThatUpdateTaskStatusThrowsExceptionWhenTaskerIDDoesNotMatch() {
        // Given
        Status currentStatus = Status.Accepted;
        Status newStatus = Status.InProgress;
        Long wrongTaskerId = 99L;

        when(taskStatusDao.getStatus(taskId)).thenReturn(Optional.of(currentStatus));
        when(taskStatusDao.getTaskerID(taskId)).thenReturn(Optional.of(taskerId));

        // When & Then
        assertThatThrownBy(() -> taskStatusService.updateTaskStatus(taskId, wrongTaskerId, newStatus))
                .isInstanceOf(BadAcceptRejectException.class)
                .hasMessage("the task id does not belong to this tasker");

        verify(taskStatusDao).getStatus(taskId);
        verify(taskStatusDao).getTaskerID(taskId);
        verify(taskStatusDao, never()).updateStatus(anyLong(), any(Status.class));
    }

    @Test
    void testThatUpdateTaskStatusThrowsExceptionWhenTaskerIDNotFound() {
        // Given
        Status currentStatus = Status.Accepted;
        Status newStatus = Status.InProgress;

        when(taskStatusDao.getStatus(taskId)).thenReturn(Optional.of(currentStatus));
        when(taskStatusDao.getTaskerID(taskId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> taskStatusService.updateTaskStatus(taskId, taskerId, newStatus))
                .isInstanceOf(BadAcceptRejectException.class)
                .hasMessage("the task id does not belong to this tasker");

        verify(taskStatusDao).getStatus(taskId);
        verify(taskStatusDao).getTaskerID(taskId);
        verify(taskStatusDao, never()).updateStatus(anyLong(), any(Status.class));
    }

    @Test
    void testThatUpdateTaskStatusThrowsExceptionForInReviewStatus() {
        // Given
        Status currentStatus = Status.InReview;
        Status newStatus = Status.Accepted;

        when(taskStatusDao.getStatus(taskId)).thenReturn(Optional.of(currentStatus));
        when(taskStatusDao.getTaskerID(taskId)).thenReturn(Optional.of(taskerId));

        // When & Then
        assertThatThrownBy(() -> taskStatusService.updateTaskStatus(taskId, taskerId, newStatus))
                .isInstanceOf(BadStateUpdateException.class)
                .hasMessage("can't update state of inReview or Rejected task");

        verify(taskStatusDao).getStatus(taskId);
        verify(taskStatusDao).getTaskerID(taskId);
    }

    @Test
    void testThatUpdateTaskStatusThrowsExceptionForRejectedStatus() {
        // Given
        Status currentStatus = Status.Rejected;
        Status newStatus = Status.Accepted;

        when(taskStatusDao.getStatus(taskId)).thenReturn(Optional.of(currentStatus));
        when(taskStatusDao.getTaskerID(taskId)).thenReturn(Optional.of(taskerId));

        // When & Then
        assertThatThrownBy(() -> taskStatusService.updateTaskStatus(taskId, taskerId, newStatus))
                .isInstanceOf(BadStateUpdateException.class)
                .hasMessage("can't update state of inReview or Rejected task");

        verify(taskStatusDao).getStatus(taskId);
        verify(taskStatusDao).getTaskerID(taskId);
    }

    @Test
    void testThatUpdateTaskStatusThrowsExceptionFromAcceptedToSuspended() {
        // Given
        Status currentStatus = Status.Accepted;
        Status newStatus = Status.Suspended;

        when(taskStatusDao.getStatus(taskId)).thenReturn(Optional.of(currentStatus));
        when(taskStatusDao.getTaskerID(taskId)).thenReturn(Optional.of(taskerId));

        // When & Then
        assertThatThrownBy(() -> taskStatusService.updateTaskStatus(taskId, taskerId, newStatus))
                .isInstanceOf(BadStateUpdateException.class)
                .hasMessage("accepted can be changed to in progress only");

        verify(taskStatusDao).getStatus(taskId);
        verify(taskStatusDao).getTaskerID(taskId);
    }

    @Test
    void testThatUpdateTaskStatusThrowsExceptionFromAcceptedToDone() {
        // Given
        Status currentStatus = Status.Accepted;
        Status newStatus = Status.Done;

        when(taskStatusDao.getStatus(taskId)).thenReturn(Optional.of(currentStatus));
        when(taskStatusDao.getTaskerID(taskId)).thenReturn(Optional.of(taskerId));

        // When & Then
        assertThatThrownBy(() -> taskStatusService.updateTaskStatus(taskId, taskerId, newStatus))
                .isInstanceOf(BadStateUpdateException.class)
                .hasMessage("accepted can be changed to in progress only");

        verify(taskStatusDao).getStatus(taskId);
        verify(taskStatusDao).getTaskerID(taskId);
    }

    @Test
    void testThatUpdateTaskStatusThrowsExceptionFromInProgressToAccepted() {
        // Given
        Status currentStatus = Status.InProgress;
        Status newStatus = Status.Accepted;

        when(taskStatusDao.getStatus(taskId)).thenReturn(Optional.of(currentStatus));
        when(taskStatusDao.getTaskerID(taskId)).thenReturn(Optional.of(taskerId));

        // When & Then
        assertThatThrownBy(() -> taskStatusService.updateTaskStatus(taskId, taskerId, newStatus))
                .isInstanceOf(BadStateUpdateException.class)
                .hasMessage("In Progress can be changed to in done or suspended only");

        verify(taskStatusDao).getStatus(taskId);
        verify(taskStatusDao).getTaskerID(taskId);
    }

    @Test
    void testThatUpdateTaskStatusThrowsExceptionFromSuspendedToAccepted() {
        // Given
        Status currentStatus = Status.Suspended;
        Status newStatus = Status.Accepted;

        when(taskStatusDao.getStatus(taskId)).thenReturn(Optional.of(currentStatus));
        when(taskStatusDao.getTaskerID(taskId)).thenReturn(Optional.of(taskerId));

        // When & Then
        assertThatThrownBy(() -> taskStatusService.updateTaskStatus(taskId, taskerId, newStatus))
                .isInstanceOf(BadStateUpdateException.class)
                .hasMessage("Suspended can be changed to Done or InProgress only");

        verify(taskStatusDao).getStatus(taskId);
        verify(taskStatusDao).getTaskerID(taskId);
    }

    @Test
    void testThatUpdateTaskStatusThrowsExceptionFromDoneToAnyState() {
        // Given
        Status currentStatus = Status.Done;
        Status newStatus = Status.InProgress;

        when(taskStatusDao.getStatus(taskId)).thenReturn(Optional.of(currentStatus));
        when(taskStatusDao.getTaskerID(taskId)).thenReturn(Optional.of(taskerId));

        // When & Then
        assertThatThrownBy(() -> taskStatusService.updateTaskStatus(taskId, taskerId, newStatus))
                .isInstanceOf(BadStateUpdateException.class)
                .hasMessage("can't change done status");

        verify(taskStatusDao).getStatus(taskId);
        verify(taskStatusDao).getTaskerID(taskId);
    }

    @Test
    void testThatUpdateTaskStatusHandlesNullStartInProgressForDoneFromInProgress() {
        // Given - Task going from InProgress to Done but has null startInProgress
        Status currentStatus = Status.InProgress;
        Status newStatus = Status.Done;
        Double hourRate = 40.0;
        Double workedHours = 3.0;

        when(taskStatusDao.getStatus(taskId)).thenReturn(Optional.of(currentStatus));
        when(taskStatusDao.getTaskerID(taskId)).thenReturn(Optional.of(taskerId));
        when(taskStatusDao.getStartInProgress(taskId)).thenReturn(null); // No start time
        when(taskStatusDao.getTaskerHourRate(taskerId)).thenReturn(hourRate);
        when(taskStatusDao.getTaskWorkedHours(taskId)).thenReturn(workedHours);
        when(taskStatusDao.updateTaskerWorkedHours(taskerId, workedHours)).thenReturn(true);
        when(taskStatusDao.updateTaskBill(eq(taskId), anyDouble())).thenReturn(true);
        when(taskStatusDao.updateTaskerTotalEarning(eq(taskerId), anyDouble())).thenReturn(true);
        when(taskStatusDao.updateTaskEndData(eq(taskId), any(Timestamp.class))).thenReturn(true);
        when(taskStatusDao.updateStatus(taskId, newStatus)).thenReturn(true);

        taskDto.setStatus(newStatus);
        when(taskRequestDao.getTaskDetails(taskId)).thenReturn(Optional.of(taskDto));

        // When
        TaskDto result = taskStatusService.updateTaskStatus(taskId, taskerId, newStatus);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(newStatus);

        // Verify updateTaskWorkedHours was not called since startInProgress is null
        verify(taskStatusDao, never()).updateTaskWorkedHours(eq(taskId), anyDouble());
        verify(taskStatusDao).updateStatus(taskId, newStatus);
        verify(taskStatusDao).updateTaskBill(eq(taskId), eq(hourRate * workedHours));
    }

    @Test
    void testThatUpdateWorkedHoursDoesNotThrowException() {
        // When & Then - Should not throw any exception
        assertThatCode(() -> accepted.updateWorkedHours())
                .doesNotThrowAnyException();
    }

    @Test
    void testThatUpdateStatusDoesNotThrowException() {
        // When & Then - Should not throw any exception
        assertThatCode(() -> accepted.updateStatus())
                .doesNotThrowAnyException();
    }

    @Test
    void testUpdateTaskStatusSendsEmailSuccessfully() {
        // Given
        Status currentStatus = Status.Accepted;
        Status newStatus = Status.InProgress;

        taskDto.setStatus(newStatus);
        taskDto.setUserMail("user@example.com");
        taskDto.setTaskerMail("tasker@example.com");

        when(taskStatusDao.getStatus(taskId)).thenReturn(Optional.of(currentStatus));
        when(taskStatusDao.getTaskerID(taskId)).thenReturn(Optional.of(taskerId));
        when(taskStatusDao.updateStatus(taskId, newStatus)).thenReturn(true);
        when(taskRequestDao.getTaskDetails(taskId)).thenReturn(Optional.of(taskDto));

        // When
        taskStatusService.updateTaskStatus(taskId, taskerId, newStatus);

        // Then
        verify(emailServiceImp).sendEmail(any());
    }

    @Test
    void testUpdateTaskStatusEmailFails() {
        // Given
        Status currentStatus = Status.Accepted;
        Status newStatus = Status.InProgress;

        taskDto.setStatus(newStatus);
        taskDto.setUserMail("user@example.com");
        taskDto.setTaskerMail("tasker@example.com");

        when(taskStatusDao.getStatus(taskId)).thenReturn(Optional.of(currentStatus));
        when(taskStatusDao.getTaskerID(taskId)).thenReturn(Optional.of(taskerId));
        when(taskStatusDao.updateStatus(taskId, newStatus)).thenReturn(true);
        when(taskRequestDao.getTaskDetails(taskId)).thenReturn(Optional.of(taskDto));

        // Simulate email failure
        doThrow(new RuntimeException("Email failed")).when(emailServiceImp).sendEmail(any());

        // Act & Assert
        assertThatThrownBy(() -> taskStatusService.updateTaskStatus(taskId, taskerId, newStatus))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Email failed");
    }

    @Test
    void testEmailTypeIsCorrectOnTaskStatusUpdate() {
        // Given
        Status currentStatus = Status.Accepted;
        Status newStatus = Status.InProgress;

        taskDto.setStatus(newStatus);
        taskDto.setUserMail("user@example.com");
        taskDto.setTaskerMail("tasker@example.com");

        when(taskStatusDao.getStatus(taskId)).thenReturn(Optional.of(currentStatus));
        when(taskStatusDao.getTaskerID(taskId)).thenReturn(Optional.of(taskerId));
        when(taskStatusDao.updateStatus(taskId, newStatus)).thenReturn(true);
        when(taskRequestDao.getTaskDetails(taskId)).thenReturn(Optional.of(taskDto));

        // When
        taskStatusService.updateTaskStatus(taskId, taskerId, newStatus);

        // Then
        ArgumentCaptor<EmailRequest> emailCaptor = ArgumentCaptor.forClass(EmailRequest.class);
        verify(emailServiceImp).sendEmail(emailCaptor.capture());

        EmailRequest capturedEmail = emailCaptor.getValue();
        assertThat(capturedEmail.getEmailType()).isEqualTo(EmailType.TASK_STATUS);
        assertThat(capturedEmail.getRecipientEmail()).isEqualTo("user@example.com");
    }


}