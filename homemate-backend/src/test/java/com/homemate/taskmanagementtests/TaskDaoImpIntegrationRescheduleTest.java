package com.homemate.taskmanagementtests;

import com.homemate.taskmanagement.dao.TaskRescheduleDao;
import lombok.AllArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("task")

public class TaskDaoImpIntegrationRescheduleTest {

    private final TaskRescheduleDao underTest;
    @Autowired
    public TaskDaoImpIntegrationRescheduleTest(TaskRescheduleDao underTest) {
        this.underTest = underTest;
    }


    @Test
    void testThatUpdateTaskStartDateSuccessfullyUpdatesExistingTask() {
        Long taskId = 8L;
        LocalDateTime newStartDate = LocalDateTime.now().plusDays(3);

        boolean updated = underTest.updateTaskStartDate(taskId, newStartDate);

        assertThat(updated).isTrue();

        LocalDateTime dbStartDate = underTest.getStartDate(taskId).orElse(null);
        assertThat(dbStartDate).isNotNull();

        LocalDateTime expected = newStartDate.truncatedTo(ChronoUnit.SECONDS);
        Assertions.assertNotNull(dbStartDate);
        LocalDateTime actual = dbStartDate.truncatedTo(ChronoUnit.SECONDS);

        assertThat(actual).isEqualTo(expected);
    }


    @Test
    void testThatUpdateTaskStartDateReturnsFalseForNonExistentTask() {
        Long taskId = 999L;
        LocalDateTime newStartDate = LocalDateTime.now().plusDays(3);

        boolean updated = underTest.updateTaskStartDate(taskId, newStartDate);

        assertThat(updated).isFalse();
    }
    @Test
    void testGetStartDateReturnsValueForExistingTask() {
        Long taskId = 8L;
        LocalDateTime expectedStartDate = LocalDateTime.now().plusDays(1);

        // Make sure the task exists and has a start date
        underTest.updateTaskStartDate(taskId, expectedStartDate);

        Optional<LocalDateTime> result = underTest.getStartDate(taskId);

        assertThat(result).isPresent();
        LocalDateTime actual = result.get().truncatedTo(ChronoUnit.SECONDS);
        LocalDateTime expected = expectedStartDate.truncatedTo(ChronoUnit.SECONDS);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void testGetStartDateReturnsEmptyForNonExistentTask() {
        Long taskId = 999L;

        Optional<LocalDateTime> result = underTest.getStartDate(taskId);

        assertThat(result).isEmpty();
    }


}
