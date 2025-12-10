package com.homemate.taskmanagementtests;

import com.homemate.taskmanagement.dao.impl.TaskDaoImpl;
import com.homemate.taskmanagement.exceptions.BadStateUpdateException;
import com.homemate.taskmanagement.exceptions.TaskNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("task")
public class TaskDaoImplUpdateIntegrationTest {

    private final TaskDaoImpl underTest;
    @Autowired
    public TaskDaoImplUpdateIntegrationTest(TaskDaoImpl underTest) {
        this.underTest = underTest;
    }


    // ========== updateTaskWorkedHours Tests ==========

    @Test
    void testThatUpdateTaskWorkedHoursSuccessfullyUpdatesExistingTask() {
        // Given - Task ID 6 with workedHours = 0 from taskData.sql
        Long taskId = 6L;
        double additionalHours = 5.5;

        // Get initial worked hours
        Double initialHours = underTest.getTaskWorkedHours(taskId);
        assertThat(initialHours).isEqualTo(0.0);

        // When - Update worked hours
        boolean updated = underTest.updateTaskWorkedHours(taskId, additionalHours);

        // Then - Verify update was successful
        assertThat(updated).isTrue();

        // Verify hours were added correctly
        Double updatedHours = underTest.getTaskWorkedHours(taskId);
        assertThat(updatedHours).isEqualTo(5.5);
    }

    @Test
    void testThatUpdateTaskWorkedHoursReturnsFalseForNonExistentTask() {
        // Given - Non-existent task ID
        Long taskId = 999L;
        double additionalHours = 5.5;

        // When - Update worked hours
        boolean updated = underTest.updateTaskWorkedHours(taskId, additionalHours);

        // Then - Verify update failed
        assertThat(updated).isFalse();
    }

    // ========== updateTaskStartInProgress Tests ==========

    @Test
    void testThatUpdateTaskStartInProgressSuccessfullyUpdatesExistingTask() {
        // Given - Task ID 6 with startInProgress = null from taskData.sql
        Long taskId = 6L;
        Timestamp startTime = Timestamp.valueOf(LocalDateTime.of(2025, 12, 4, 10, 30));

        // When - Update startInProgress
        boolean updated = underTest.updateTaskStartInProgress(taskId, startTime);

        // Then - Verify update was successful
        assertThat(updated).isTrue();

        // Verify time was set correctly
        Timestamp retrievedTime = underTest.getStartInProgress(taskId);
        assertThat(retrievedTime).isEqualTo(startTime);
    }

    @Test
    void testThatUpdateTaskStartInProgressReturnsFalseForNonExistentTask() {
        // Given - Non-existent task ID
        Long taskId = 999L;
        Timestamp startTime = Timestamp.valueOf(LocalDateTime.of(2024, 11, 15, 10, 30));

        // When - Update startInProgress
        boolean updated = underTest.updateTaskStartInProgress(taskId, startTime);

        // Then - Verify update failed
        assertThat(updated).isFalse();
    }

    // ========== updateTaskEndData Tests ==========

    @Test
    void testThatUpdateTaskEndDateSuccessfullyUpdatesExistingTask() {
        // Given - Task ID 6 with endDate = null from taskData.sql
        Long taskId = 6L;
        Timestamp endTime = Timestamp.valueOf(LocalDateTime.of(2025, 11, 15, 18, 0));

        // When - Update endDate
        boolean updated = underTest.updateTaskEndData(taskId, endTime);

        // Then - Verify update was successful
        assertThat(updated).isTrue();

        // Verify end date was set (you can verify this by getting task details)
        var taskDetails = underTest.getTaskDetails(taskId);
        assertThat(taskDetails).isPresent();
        assertThat(taskDetails.get().getEndDate()).isNotNull();
    }

    @Test
    void testThatUpdateTaskEndDateReturnsFalseForNonExistentTask() {
        // Given - Non-existent task ID
        Long taskId = 999L;
        Timestamp endTime = Timestamp.valueOf(LocalDateTime.of(2024, 11, 15, 18, 0));

        // When - Update endDate
        boolean updated = underTest.updateTaskEndData(taskId, endTime);

        // Then - Verify update failed
        assertThat(updated).isFalse();
    }

    // ========== updateTaskBill Tests ==========

    @Test
    void testThatUpdateTaskBillSuccessfullyUpdatesExistingTask() {
        // Given - Task ID 6 with bill = 0 from taskData.sql
        Long taskId = 6L;
        double bill = 250.75;

        // When - Update bill
        boolean updated = underTest.updateTaskBill(taskId, bill);

        // Then - Verify update was successful
        assertThat(updated).isTrue();

        // Verify bill was set correctly
        var taskDetails = underTest.getTaskDetails(taskId);
        assertThat(taskDetails).isPresent();
        assertThat(taskDetails.get().getBill()).isEqualTo(bill);
    }

    @Test
    void testThatUpdateTaskBillReturnsFalseForNonExistentTask() {
        // Given - Non-existent task ID
        Long taskId = 999L;
        double bill = 250.75;

        // When - Update bill
        boolean updated = underTest.updateTaskBill(taskId, bill);

        // Then - Verify update failed
        assertThat(updated).isFalse();
    }

    // ========== getStartInProgress Tests ==========

    @Test
    void testThatGetStartInProgressReturnsCorrectTimeForExistingTask() {
        // Given - Task ID 7 with startInProgress set from taskData.sql
        Long taskId = 7L;

        // When - Get startInProgress
        Timestamp startTime = underTest.getStartInProgress(taskId);

        // Then - Verify time is returned
        assertThat(startTime).isNotNull();
        assertThat(startTime).isEqualTo(Timestamp.valueOf(LocalDateTime.of(2024, 11, 14, 11, 0)));
    }

    @Test
    void testThatGetStartInProgressThrowsExceptionForNonExistentTask() {
        // Given - Non-existent task ID
        Long taskId = 999L;

        // When & Then - Verify exception is thrown
        assertThatThrownBy(() -> underTest.getStartInProgress(taskId))
                .isInstanceOf(TaskNotFoundException.class)
                .hasMessage("id not correct");
    }

    // ========== getTaskWorkedHours Tests ==========

    @Test
    void testThatGetTaskWorkedHoursReturnsCorrectHoursForExistingTask() {
        // Given - Task ID 1 with workedHours = 4 from taskData.sql
        Long taskId = 1L;

        // When - Get worked hours
        Double workedHours = underTest.getTaskWorkedHours(taskId);

        // Then - Verify hours are returned
        assertThat(workedHours).isNotNull();
        assertThat(workedHours).isEqualTo(4.0);
    }

    @Test
    void testThatGetTaskWorkedHoursThrowsExceptionForNonExistentTask() {
        // Given - Non-existent task ID
        Long taskId = 999L;

        // When & Then - Verify exception is thrown
        assertThatThrownBy(() -> underTest.getTaskWorkedHours(taskId))
                .isInstanceOf(TaskNotFoundException.class)
                .hasMessage("id not correct");
    }

    // ========== updateTaskerWorkedHours Tests ==========

    @Test
    void testThatUpdateTaskerWorkedHoursSuccessfullyUpdatesExistingTasker() {
        // Given - Tasker ID 1 with WorkedHours = 440.00 from taskData.sql
        Long taskerId = 1L;
        double additionalHours = 8.5;

        // When - Update tasker worked hours
        boolean updated = underTest.updateTaskerWorkedHours(taskerId, additionalHours);

        // Then - Verify update was successful
        assertThat(updated).isTrue();

        // Note: You would need a getTaskerWorkedHours method to verify the actual value
        // For now, we just verify the update returns true
    }

    @Test
    void testThatUpdateTaskerWorkedHoursReturnsFalseForNonExistentTasker() {
        // Given - Non-existent tasker ID
        Long taskerId = 999L;
        double additionalHours = 8.5;

        // When - Update tasker worked hours
        boolean updated = underTest.updateTaskerWorkedHours(taskerId, additionalHours);

        // Then - Verify update failed
        assertThat(updated).isFalse();
    }

    // ========== getTaskerHourRate Tests ==========

    @Test
    void testThatGetTaskerHourRateReturnsCorrectRateForExistingTasker() {
        // Given - Tasker ID 1 with hourRate = 35.00 from taskData.sql
        Long taskerId = 1L;

        // When - Get hour rate
        Double hourRate = underTest.getTaskerHourRate(taskerId);

        // Then - Verify rate is returned
        assertThat(hourRate).isNotNull();
        assertThat(hourRate).isEqualTo(35.00);
    }

    @Test
    void testThatGetTaskerHourRateThrowsExceptionForNonExistentTasker() {
        // Given - Non-existent tasker ID
        Long taskerId = 999L;

        // When & Then - Verify exception is thrown
        assertThatThrownBy(() -> underTest.getTaskerHourRate(taskerId))
                .isInstanceOf(BadStateUpdateException.class)
                .hasMessage("tasker Id  not correct");
    }

    // ========== updateTaskerTotalEarning Tests ==========

    @Test
    void testThatUpdateTaskerTotalEarningSuccessfullyUpdatesExistingTasker() {
        // Given - Tasker ID 1 with totalEarning = 15400.00 from taskData.sql
        Long taskerId = 1L;
        double additionalEarning = 500.00;

        // When - Update total earning
        boolean updated = underTest.updateTaskerTotalEarning(taskerId, additionalEarning);

        // Then - Verify update was successful
        assertThat(updated).isTrue();

        // Note: You would need a getTaskerTotalEarning method to verify the actual value
        // For now, we just verify the update returns true
    }

    @Test
    void testThatUpdateTaskerTotalEarningReturnsFalseForNonExistentTasker() {
        // Given - Non-existent tasker ID
        Long taskerId = 999L;
        double additionalEarning = 500.00;

        // When - Update total earning
        boolean updated = underTest.updateTaskerTotalEarning(taskerId, additionalEarning);

        // Then - Verify update failed
        assertThat(updated).isFalse();
    }

    // ========== Additional Edge Case Tests ==========

    @Test
    void testThatUpdateTaskWorkedHoursAccumulatesCorrectly() {
        // Given - Task with existing worked hours
        Long taskId = 1L; // Has 4 hours initially
        double firstAddition = 2.5;
        double secondAddition = 1.5;

        Double initialHours = underTest.getTaskWorkedHours(taskId);

        // When - Update twice
        underTest.updateTaskWorkedHours(taskId, firstAddition);
        underTest.updateTaskWorkedHours(taskId, secondAddition);

        // Then - Verify hours accumulated correctly
        Double finalHours = underTest.getTaskWorkedHours(taskId);
        assertThat(finalHours).isEqualTo(initialHours + firstAddition + secondAddition);
    }

    @Test
    void testThatUpdateTaskStartInProgressCanBeUpdatedMultipleTimes() {
        // Given - Task ID 6
        Long taskId = 6L;
        Timestamp firstTime = Timestamp.valueOf(LocalDateTime.of(2024, 11, 15, 10, 0));
        Timestamp secondTime = Timestamp.valueOf(LocalDateTime.of(2024, 11, 15, 11, 0));

        // When - Update twice
        underTest.updateTaskStartInProgress(taskId, firstTime);
        underTest.updateTaskStartInProgress(taskId, secondTime);

        // Then - Verify last update is preserved
        Timestamp retrievedTime = underTest.getStartInProgress(taskId);
        assertThat(retrievedTime).isEqualTo(secondTime);
    }

    @Test
    void testThatGetStartInProgressReturnsNullForTaskWithNullStartTime() {
        // Given - Task ID 6 with null startInProgress
        Long taskId = 6L;

        // When - Get startInProgress
        Timestamp startTime = underTest.getStartInProgress(taskId);

        // Then - Verify null is returned (not exception)
        assertThat(startTime).isNull();
    }

    @Test
    void testThatGetTaskWorkedHoursReturnsZeroForNewTask() {
        // Given - Task ID 6 with 0 worked hours
        Long taskId = 6L;

        // When - Get worked hours
        Double workedHours = underTest.getTaskWorkedHours(taskId);

        // Then - Verify 0 is returned
        assertThat(workedHours).isEqualTo(0.0);
    }
}