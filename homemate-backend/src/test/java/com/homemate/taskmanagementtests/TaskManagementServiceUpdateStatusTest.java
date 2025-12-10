package com.homemate.taskmanagementtests;

import com.homemate.TaskerProfile.Dao.ReviewDao;
import com.homemate.notification.domains.dto.EmailRequest;
import com.homemate.notification.service.imp.EmailServiceImp;
import com.homemate.taskmanagement.dao.TaskDao;
import com.homemate.taskmanagement.dto.TaskDto;
import com.homemate.taskmanagement.exceptions.BadStateUpdateException;
import com.homemate.taskmanagement.exceptions.TaskNotFoundException;
import com.homemate.taskmanagement.model.Status;
import com.homemate.taskmanagement.service.Accepted;
import com.homemate.taskmanagement.service.StatusFactory;
import com.homemate.taskmanagement.service.TaskManagementService;
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
    private TaskDao taskDao;
    @Mock
    private SimpMessagingTemplate simpMessagingTemplate;

    @Mock
    private EmailServiceImp emailServiceImp;

    @InjectMocks
    private TaskManagementService taskManagementService;
    @InjectMocks
    private Accepted accepted;

    private Long taskId;
    private Long taskerId;
    private TaskDto taskDto;
    @Mock
    private ReviewDao reviewDao;

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


        taskManagementService = new TaskManagementService(null, taskDao,new StatusFactory(taskDao),reviewDao,simpMessagingTemplate,emailServiceImp);
    }

    // ========== Valid State Transitions ==========

    @Test
    void testThatUpdateTaskStatusFromAcceptedToInProgressSucceeds() {
        // Given
        Status currentStatus = Status.Accepted;
        Status newStatus = Status.InProgress;

        when(taskDao.getStatus(taskId)).thenReturn(Optional.of(currentStatus));
        when(taskDao.getTaskerID(taskId)).thenReturn(Optional.of(taskerId));
        when(taskDao.updateStatus(eq(taskId), eq(newStatus))).thenReturn(true);
        when(taskDao.updateTaskStartInProgress(eq(taskId), any(Timestamp.class))).thenReturn(true);

        taskDto.setStatus(newStatus);
        when(taskDao.getTaskDetails(taskId)).thenReturn(Optional.of(taskDto));

        // When
        TaskDto result = taskManagementService.updateTaskStatus(taskId, taskerId, newStatus);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(newStatus);

        verify(taskDao).getStatus(taskId);
        verify(taskDao).getTaskerID(taskId);
        verify(taskDao).updateTaskStartInProgress(eq(taskId), any(Timestamp.class));
        verify(taskDao).updateStatus(taskId, newStatus);
        verify(taskDao).getTaskDetails(taskId);
    }

    @Test
    void testThatUpdateTaskStatusFromInProgressToSuspendedSucceeds() {
        // Given
        Status currentStatus = Status.InProgress;
        Status newStatus = Status.Suspended;
        Timestamp startTime = Timestamp.valueOf(LocalDateTime.now().minusHours(2));

        when(taskDao.getStatus(taskId)).thenReturn(Optional.of(currentStatus));
        when(taskDao.getTaskerID(taskId)).thenReturn(Optional.of(taskerId));
        when(taskDao.updateTaskWorkedHours(eq(taskId), anyDouble())).thenReturn(true);
        when(taskDao.updateTaskStartInProgress(taskId, null)).thenReturn(true);
        when(taskDao.updateStatus(taskId, newStatus)).thenReturn(true);
        when(taskDao.getStartInProgress(taskId)).thenReturn(startTime); // Add this line


        taskDto.setStatus(newStatus);
        when(taskDao.getTaskDetails(taskId)).thenReturn(Optional.of(taskDto));

        // When
        TaskDto result = taskManagementService.updateTaskStatus(taskId, taskerId, newStatus);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(newStatus);

        verify(taskDao).getStartInProgress(taskId);
        verify(taskDao).updateTaskWorkedHours(eq(taskId), anyDouble());
        verify(taskDao).updateTaskStartInProgress(taskId, null);
        verify(taskDao).updateStatus(taskId, newStatus);
    }

    @Test
    void testThatUpdateTaskStatusFromSuspendedToInProgressSucceeds() {
        // Given
        Status currentStatus = Status.Suspended;
        Status newStatus = Status.InProgress;

        when(taskDao.getStatus(taskId)).thenReturn(Optional.of(currentStatus));
        when(taskDao.getTaskerID(taskId)).thenReturn(Optional.of(taskerId));
        when(taskDao.updateStatus(taskId, newStatus)).thenReturn(true);
        when(taskDao.updateTaskStartInProgress(eq(taskId), any(Timestamp.class))).thenReturn(true);

        taskDto.setStatus(newStatus);
        when(taskDao.getTaskDetails(taskId)).thenReturn(Optional.of(taskDto));

        // When
        TaskDto result = taskManagementService.updateTaskStatus(taskId, taskerId, newStatus);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(newStatus);

        verify(taskDao).updateTaskStartInProgress(eq(taskId), any(Timestamp.class));
        verify(taskDao).updateStatus(taskId, newStatus);
    }

    @Test
    void testThatUpdateTaskStatusFromSuspendedToDoneSucceeds() {
        // Given
        Status currentStatus = Status.Suspended;
        Status newStatus = Status.Done;
        Timestamp startTime = Timestamp.valueOf(LocalDateTime.now().minusHours(3));
        Double hourRate = 50.0;
        Double workedHours = 5.0;

        when(taskDao.getStatus(taskId)).thenReturn(Optional.of(currentStatus));
        when(taskDao.getTaskerID(taskId)).thenReturn(Optional.of(taskerId));
        when(taskDao.getStartInProgress(taskId)).thenReturn(startTime);
        when(taskDao.updateTaskWorkedHours(eq(taskId), anyDouble())).thenReturn(true);
        when(taskDao.updateTaskStartInProgress(taskId, null)).thenReturn(true);
        when(taskDao.getTaskerHourRate(taskerId)).thenReturn(hourRate);
        when(taskDao.getTaskWorkedHours(taskId)).thenReturn(workedHours);
        when(taskDao.updateTaskerWorkedHours(taskerId, workedHours)).thenReturn(true);
        when(taskDao.updateTaskBill(eq(taskId), anyDouble())).thenReturn(true);
        when(taskDao.updateTaskerTotalEarning(eq(taskerId), anyDouble())).thenReturn(true);
        when(taskDao.updateTaskEndData(eq(taskId), any(Timestamp.class))).thenReturn(true);
        when(taskDao.updateStatus(taskId, newStatus)).thenReturn(true);

        taskDto.setStatus(newStatus);
        when(taskDao.getTaskDetails(taskId)).thenReturn(Optional.of(taskDto));

        // When
        TaskDto result = taskManagementService.updateTaskStatus(taskId, taskerId, newStatus);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(newStatus);

        verify(taskDao).getTaskerHourRate(taskerId);
        verify(taskDao).getTaskWorkedHours(taskId);
        verify(taskDao).updateTaskerWorkedHours(taskerId, workedHours);
        verify(taskDao).updateTaskBill(eq(taskId), eq(hourRate * workedHours));
        verify(taskDao).updateTaskerTotalEarning(eq(taskerId), eq(hourRate * workedHours));
        verify(taskDao).updateTaskEndData(eq(taskId), any(Timestamp.class));
        verify(taskDao).updateStatus(taskId, newStatus);
    }

    @Test
    void testThatUpdateTaskStatusFromInProgressToDoneSucceeds() {
        // Given
        Status currentStatus = Status.InProgress;
        Status newStatus = Status.Done;
        Timestamp startTime = Timestamp.valueOf(LocalDateTime.now().minusHours(4));
        Double hourRate = 45.0;
        Double workedHours = 6.0;

        when(taskDao.getStatus(taskId)).thenReturn(Optional.of(currentStatus));
        when(taskDao.getTaskerID(taskId)).thenReturn(Optional.of(taskerId));
        when(taskDao.getStartInProgress(taskId)).thenReturn(startTime);
        when(taskDao.updateTaskWorkedHours(eq(taskId), anyDouble())).thenReturn(true);
        when(taskDao.updateTaskStartInProgress(taskId, null)).thenReturn(true);
        when(taskDao.getTaskerHourRate(taskerId)).thenReturn(hourRate);
        when(taskDao.getTaskWorkedHours(taskId)).thenReturn(workedHours);
        when(taskDao.updateTaskerWorkedHours(taskerId, workedHours)).thenReturn(true);
        when(taskDao.updateTaskBill(eq(taskId), anyDouble())).thenReturn(true);
        when(taskDao.updateTaskerTotalEarning(eq(taskerId), anyDouble())).thenReturn(true);
        when(taskDao.updateTaskEndData(eq(taskId), any(Timestamp.class))).thenReturn(true);
        when(taskDao.updateStatus(taskId, newStatus)).thenReturn(true);

        taskDto.setStatus(newStatus);
        when(taskDao.getTaskDetails(taskId)).thenReturn(Optional.of(taskDto));

        // When
        TaskDto result = taskManagementService.updateTaskStatus(taskId, taskerId, newStatus);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(newStatus);

        verify(taskDao).getStartInProgress(taskId);
        verify(taskDao).updateTaskWorkedHours(eq(taskId), anyDouble());
        verify(taskDao).updateStatus(taskId, newStatus);
    }

    // ========== Invalid State Transitions ==========

    @Test
    void testThatUpdateTaskStatusThrowsExceptionWhenTaskNotFound() {
        // Given
        Status newStatus = Status.InProgress;
        when(taskDao.getStatus(taskId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> taskManagementService.updateTaskStatus(taskId, taskerId, newStatus))
                .isInstanceOf(TaskNotFoundException.class)
                .hasMessage("wrong task id");

        verify(taskDao).getStatus(taskId);
        verify(taskDao, never()).getTaskerID(anyLong());
        verify(taskDao, never()).updateStatus(anyLong(), any(Status.class));
    }

    @Test
    void testThatUpdateTaskStatusThrowsExceptionWhenTaskerIDDoesNotMatch() {
        // Given
        Status currentStatus = Status.Accepted;
        Status newStatus = Status.InProgress;
        Long wrongTaskerId = 99L;

        when(taskDao.getStatus(taskId)).thenReturn(Optional.of(currentStatus));
        when(taskDao.getTaskerID(taskId)).thenReturn(Optional.of(taskerId));

        // When & Then
        assertThatThrownBy(() -> taskManagementService.updateTaskStatus(taskId, wrongTaskerId, newStatus))
                .isInstanceOf(BadStateUpdateException.class)
                .hasMessage("the task id does not belong to this tasker");

        verify(taskDao).getStatus(taskId);
        verify(taskDao).getTaskerID(taskId);
        verify(taskDao, never()).updateStatus(anyLong(), any(Status.class));
    }

    @Test
    void testThatUpdateTaskStatusThrowsExceptionWhenTaskerIDNotFound() {
        // Given
        Status currentStatus = Status.Accepted;
        Status newStatus = Status.InProgress;

        when(taskDao.getStatus(taskId)).thenReturn(Optional.of(currentStatus));
        when(taskDao.getTaskerID(taskId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> taskManagementService.updateTaskStatus(taskId, taskerId, newStatus))
                .isInstanceOf(BadStateUpdateException.class)
                .hasMessage("the task id does not belong to this tasker");

        verify(taskDao).getStatus(taskId);
        verify(taskDao).getTaskerID(taskId);
        verify(taskDao, never()).updateStatus(anyLong(), any(Status.class));
    }

    @Test
    void testThatUpdateTaskStatusThrowsExceptionForInReviewStatus() {
        // Given
        Status currentStatus = Status.InReview;
        Status newStatus = Status.Accepted;

        when(taskDao.getStatus(taskId)).thenReturn(Optional.of(currentStatus));
        when(taskDao.getTaskerID(taskId)).thenReturn(Optional.of(taskerId));

        // When & Then
        assertThatThrownBy(() -> taskManagementService.updateTaskStatus(taskId, taskerId, newStatus))
                .isInstanceOf(BadStateUpdateException.class)
                .hasMessage("can't update state of inReview or Rejected task");

        verify(taskDao).getStatus(taskId);
        verify(taskDao).getTaskerID(taskId);
    }

    @Test
    void testThatUpdateTaskStatusThrowsExceptionForRejectedStatus() {
        // Given
        Status currentStatus = Status.Rejected;
        Status newStatus = Status.Accepted;

        when(taskDao.getStatus(taskId)).thenReturn(Optional.of(currentStatus));
        when(taskDao.getTaskerID(taskId)).thenReturn(Optional.of(taskerId));

        // When & Then
        assertThatThrownBy(() -> taskManagementService.updateTaskStatus(taskId, taskerId, newStatus))
                .isInstanceOf(BadStateUpdateException.class)
                .hasMessage("can't update state of inReview or Rejected task");

        verify(taskDao).getStatus(taskId);
        verify(taskDao).getTaskerID(taskId);
    }

    @Test
    void testThatUpdateTaskStatusThrowsExceptionFromAcceptedToSuspended() {
        // Given
        Status currentStatus = Status.Accepted;
        Status newStatus = Status.Suspended;

        when(taskDao.getStatus(taskId)).thenReturn(Optional.of(currentStatus));
        when(taskDao.getTaskerID(taskId)).thenReturn(Optional.of(taskerId));

        // When & Then
        assertThatThrownBy(() -> taskManagementService.updateTaskStatus(taskId, taskerId, newStatus))
                .isInstanceOf(BadStateUpdateException.class)
                .hasMessage("accepted can be changed to in progress only");

        verify(taskDao).getStatus(taskId);
        verify(taskDao).getTaskerID(taskId);
    }

    @Test
    void testThatUpdateTaskStatusThrowsExceptionFromAcceptedToDone() {
        // Given
        Status currentStatus = Status.Accepted;
        Status newStatus = Status.Done;

        when(taskDao.getStatus(taskId)).thenReturn(Optional.of(currentStatus));
        when(taskDao.getTaskerID(taskId)).thenReturn(Optional.of(taskerId));

        // When & Then
        assertThatThrownBy(() -> taskManagementService.updateTaskStatus(taskId, taskerId, newStatus))
                .isInstanceOf(BadStateUpdateException.class)
                .hasMessage("accepted can be changed to in progress only");

        verify(taskDao).getStatus(taskId);
        verify(taskDao).getTaskerID(taskId);
    }

    @Test
    void testThatUpdateTaskStatusThrowsExceptionFromInProgressToAccepted() {
        // Given
        Status currentStatus = Status.InProgress;
        Status newStatus = Status.Accepted;

        when(taskDao.getStatus(taskId)).thenReturn(Optional.of(currentStatus));
        when(taskDao.getTaskerID(taskId)).thenReturn(Optional.of(taskerId));

        // When & Then
        assertThatThrownBy(() -> taskManagementService.updateTaskStatus(taskId, taskerId, newStatus))
                .isInstanceOf(BadStateUpdateException.class)
                .hasMessage("In Progress can be changed to in done or suspended only");

        verify(taskDao).getStatus(taskId);
        verify(taskDao).getTaskerID(taskId);
    }

    @Test
    void testThatUpdateTaskStatusThrowsExceptionFromSuspendedToAccepted() {
        // Given
        Status currentStatus = Status.Suspended;
        Status newStatus = Status.Accepted;

        when(taskDao.getStatus(taskId)).thenReturn(Optional.of(currentStatus));
        when(taskDao.getTaskerID(taskId)).thenReturn(Optional.of(taskerId));

        // When & Then
        assertThatThrownBy(() -> taskManagementService.updateTaskStatus(taskId, taskerId, newStatus))
                .isInstanceOf(BadStateUpdateException.class)
                .hasMessage("Suspended can be changed to Done or InProgress only");

        verify(taskDao).getStatus(taskId);
        verify(taskDao).getTaskerID(taskId);
    }

    @Test
    void testThatUpdateTaskStatusThrowsExceptionFromDoneToAnyState() {
        // Given
        Status currentStatus = Status.Done;
        Status newStatus = Status.InProgress;

        when(taskDao.getStatus(taskId)).thenReturn(Optional.of(currentStatus));
        when(taskDao.getTaskerID(taskId)).thenReturn(Optional.of(taskerId));

        // When & Then
        assertThatThrownBy(() -> taskManagementService.updateTaskStatus(taskId, taskerId, newStatus))
                .isInstanceOf(BadStateUpdateException.class)
                .hasMessage("can't change done status");

        verify(taskDao).getStatus(taskId);
        verify(taskDao).getTaskerID(taskId);
    }

    @Test
    void testThatUpdateTaskStatusHandlesNullStartInProgressForDoneFromInProgress() {
        // Given - Task going from InProgress to Done but has null startInProgress
        Status currentStatus = Status.InProgress;
        Status newStatus = Status.Done;
        Double hourRate = 40.0;
        Double workedHours = 3.0;

        when(taskDao.getStatus(taskId)).thenReturn(Optional.of(currentStatus));
        when(taskDao.getTaskerID(taskId)).thenReturn(Optional.of(taskerId));
        when(taskDao.getStartInProgress(taskId)).thenReturn(null); // No start time
        when(taskDao.getTaskerHourRate(taskerId)).thenReturn(hourRate);
        when(taskDao.getTaskWorkedHours(taskId)).thenReturn(workedHours);
        when(taskDao.updateTaskerWorkedHours(taskerId, workedHours)).thenReturn(true);
        when(taskDao.updateTaskBill(eq(taskId), anyDouble())).thenReturn(true);
        when(taskDao.updateTaskerTotalEarning(eq(taskerId), anyDouble())).thenReturn(true);
        when(taskDao.updateTaskEndData(eq(taskId), any(Timestamp.class))).thenReturn(true);
        when(taskDao.updateStatus(taskId, newStatus)).thenReturn(true);

        taskDto.setStatus(newStatus);
        when(taskDao.getTaskDetails(taskId)).thenReturn(Optional.of(taskDto));

        // When
        TaskDto result = taskManagementService.updateTaskStatus(taskId, taskerId, newStatus);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(newStatus);

        // Verify updateTaskWorkedHours was not called since startInProgress is null
        verify(taskDao, never()).updateTaskWorkedHours(eq(taskId), anyDouble());
        verify(taskDao).updateStatus(taskId, newStatus);
        verify(taskDao).updateTaskBill(eq(taskId), eq(hourRate * workedHours));
    }
    @Test
    void testThatSendEmailDoesNotThrowException() {
        // When & Then - Should not throw any exception
        assertThatCode(() -> accepted.sendEmail())
                .doesNotThrowAnyException();
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

        when(taskDao.getStatus(taskId)).thenReturn(Optional.of(currentStatus));
        when(taskDao.getTaskerID(taskId)).thenReturn(Optional.of(taskerId));
        when(taskDao.updateStatus(taskId, newStatus)).thenReturn(true);
        when(taskDao.getTaskDetails(taskId)).thenReturn(Optional.of(taskDto));

        // When
        taskManagementService.updateTaskStatus(taskId, taskerId, newStatus);

        // Then
        verify(emailServiceImp).sendUserEmail(any());
    }

    @Test
    void testUpdateTaskStatusEmailFails() {
        // Given
        Status currentStatus = Status.Accepted;
        Status newStatus = Status.InProgress;

        taskDto.setStatus(newStatus);
        taskDto.setUserMail("user@example.com");
        taskDto.setTaskerMail("tasker@example.com");

        when(taskDao.getStatus(taskId)).thenReturn(Optional.of(currentStatus));
        when(taskDao.getTaskerID(taskId)).thenReturn(Optional.of(taskerId));
        when(taskDao.updateStatus(taskId, newStatus)).thenReturn(true);
        when(taskDao.getTaskDetails(taskId)).thenReturn(Optional.of(taskDto));

        // Simulate email failure
        doThrow(new RuntimeException("Email failed")).when(emailServiceImp).sendUserEmail(any());

        // Act & Assert
        assertThatThrownBy(() -> taskManagementService.updateTaskStatus(taskId, taskerId, newStatus))
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

        when(taskDao.getStatus(taskId)).thenReturn(Optional.of(currentStatus));
        when(taskDao.getTaskerID(taskId)).thenReturn(Optional.of(taskerId));
        when(taskDao.updateStatus(taskId, newStatus)).thenReturn(true);
        when(taskDao.getTaskDetails(taskId)).thenReturn(Optional.of(taskDto));

        // When
        taskManagementService.updateTaskStatus(taskId, taskerId, newStatus);

        // Then
        ArgumentCaptor<EmailRequest> emailCaptor = ArgumentCaptor.forClass(EmailRequest.class);
        verify(emailServiceImp).sendUserEmail(emailCaptor.capture());

        EmailRequest capturedEmail = emailCaptor.getValue();
        assertThat(capturedEmail.getEmailType()).isEqualTo(EmailRequest.EmailType.TASK_STATUS);
        assertThat(capturedEmail.getRecipientEmail()).isEqualTo("user@example.com");
    }




}