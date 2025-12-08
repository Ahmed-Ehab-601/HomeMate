package com.homemate. taskmanagementtests;

import com.homemate.taskmanagement.dao.TaskDao;
import com.homemate.taskmanagement.exceptions.BadStateUpdateException;
import com.homemate.taskmanagement.model.Status;
import com.homemate.taskmanagement.service.*;
import org.junit.jupiter. api.Test;
import org. junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class StatusFactoryTest {


    @InjectMocks
    private StatusFactory statusFactory;

    @Test
    void testThatCreateTaskStateReturnsAcceptedState() {
        // Given
        Status status = Status. Accepted;
        Long taskId = 1L;
        Long taskerId = 10L;

        // When
        TaskState taskState = statusFactory.createTaskState(status, taskId, taskerId);

        // Then
        assertThat(taskState).isInstanceOf(Accepted.class);
    }

    @Test
    void testThatCreateTaskStateReturnsInProgressState() {
        // Given
        Status status = Status.InProgress;
        Long taskId = 1L;
        Long taskerId = 10L;

        // When
        TaskState taskState = statusFactory.createTaskState(status, taskId, taskerId);

        // Then
        assertThat(taskState).isInstanceOf(InProgress.class);
    }

    @Test
    void testThatCreateTaskStateReturnsSuspendedState() {
        // Given
        Status status = Status.Suspended;
        Long taskId = 1L;
        Long taskerId = 10L;

        // When
        TaskState taskState = statusFactory.createTaskState(status, taskId, taskerId);

        // Then
        assertThat(taskState).isInstanceOf(Suspended.class);
    }

    @Test
    void testThatCreateTaskStateReturnsDoneState() {
        // Given
        Status status = Status.Done;
        Long taskId = 1L;
        Long taskerId = 10L;

        // When
        TaskState taskState = statusFactory.createTaskState(status, taskId, taskerId);

        // Then
        assertThat(taskState).isInstanceOf(Done.class);
    }

    @Test
    void testThatCreateTaskStateThrowsExceptionForInReviewStatus() {
        // Given
        Status status = Status.InReview;
        Long taskId = 1L;
        Long taskerId = 10L;

        // When & Then
        assertThatThrownBy(() -> statusFactory.createTaskState(status, taskId, taskerId))
                . isInstanceOf(BadStateUpdateException.class)
                .hasMessage("can't update state of inReview or Rejected task");
    }

    @Test
    void testThatCreateTaskStateThrowsExceptionForRejectedStatus() {
        // Given
        Status status = Status.Rejected;
        Long taskId = 1L;
        Long taskerId = 10L;

        // When & Then
        assertThatThrownBy(() -> statusFactory. createTaskState(status, taskId, taskerId))
                .isInstanceOf(BadStateUpdateException. class)
                .hasMessage("can't update state of inReview or Rejected task");
    }
}