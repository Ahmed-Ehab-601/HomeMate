package com.homemate.taskmanagementtests;

import com.homemate.taskmanagement.dao.TaskStatusDao;
import com.homemate.taskmanagement.model.Status;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("task")

public class TaskDaoImplAcceptRejectIntegrationTest {

    private final TaskStatusDao underTest;

    @Autowired
    public TaskDaoImplAcceptRejectIntegrationTest(TaskStatusDao underTest) {
        this.underTest = underTest;
    }


    @Test
    void testThatGetStatusReturnsCorrectStatusForExistingTask() {
        // Given - Task ID 8 with status InReview from taskData.sql
        Long taskId = 8L;

        // When - Get status
        Optional<Status> status = underTest.getStatus(taskId);

        // Then - Verify status is returned
        assertThat(status).isPresent();
        assertThat(status.get()).isEqualTo(Status.InReview);
    }


    @Test
    void testThatGetStatusReturnsEmptyForNonExistentTask() {
        // Given - Non-existent task ID
        Long taskId = 999L;

        // When - Get status
        Optional<Status> status = underTest.getStatus(taskId);

        // Then - Verify empty result
        assertThat(status).isEmpty();
    }

    @Test
    void testThatUpdateStatusSuccessfullyUpdatesExistingTask() {
        // Given - Task ID 8 with status InReview
        Long taskId = 8L;
        Status newStatus = Status.Accepted;

        // Verify initial status
        Optional<Status> initialStatus = underTest.getStatus(taskId);
        assertThat(initialStatus).isPresent();
        assertThat(initialStatus.get()).isEqualTo(Status.InReview);

        // When - Update status
        boolean updated = underTest.updateStatus(taskId, newStatus);

        // Then - Verify update was successful
        assertThat(updated).isTrue();

        // Verify status was actually updated
        Optional<Status> updatedStatus = underTest.getStatus(taskId);
        assertThat(updatedStatus).isPresent();
        assertThat(updatedStatus.get()).isEqualTo(Status.Accepted);
    }


    @Test
    void testThatUpdateStatusReturnsFalseForNonExistentTask() {
        // Given - Non-existent task ID
        Long taskId = 999L;
        Status newStatus = Status.Accepted;

        // When - Update status
        boolean updated = underTest.updateStatus(taskId, newStatus);

        // Then - Verify update failed
        assertThat(updated).isFalse();
    }


    @Test
    void testThatGetTaskerIDReturnsCorrectTaskerForExistingTask() {
        // Given - Task ID 1 belongs to tasker ID 1 from taskData.sql
        Long taskId = 1L;

        // When - Get tasker ID
        Optional<Long> taskerId = underTest.getTaskerID(taskId);

        // Then - Verify tasker ID is returned
        assertThat(taskerId).isPresent();
        assertThat(taskerId.get()).isEqualTo(1L);
    }

    @Test
    void testThatGetTaskerIDReturnsCorrectTaskerForDifferentTask() {
        // Given - Task ID 2 belongs to tasker ID 2 from taskData.sql
        Long taskId = 2L;

        // When - Get tasker ID
        Optional<Long> taskerId = underTest.getTaskerID(taskId);

        // Then - Verify correct tasker ID
        assertThat(taskerId).isPresent();
        assertThat(taskerId.get()).isEqualTo(2L);
    }

    @Test
    void testThatGetTaskerIDReturnsEmptyForNonExistentTask() {
        // Given - Non-existent task ID
        Long taskId = 999L;

        // When - Get tasker ID
        Optional<Long> taskerId = underTest.getTaskerID(taskId);

        // Then - Verify empty result
        assertThat(taskerId).isEmpty();
    }

}