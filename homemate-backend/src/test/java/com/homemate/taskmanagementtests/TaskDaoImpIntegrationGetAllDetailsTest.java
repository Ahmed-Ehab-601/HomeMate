package com.homemate.taskmanagementtests;

import com.homemate.TaskerProfile.DTO.ReviewDTO;
import com.homemate.taskmanagement.dao.impl.TaskDaoImpl;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest
@ExtendWith(SpringExtension.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("task")
public class TaskDaoImpIntegrationGetAllDetailsTest {

    private final TaskDaoImpl underTest;

    @Autowired
    public TaskDaoImpIntegrationGetAllDetailsTest(TaskDaoImpl underTest) {
        this.underTest = underTest;
    }



    @Test
    void testThatGetReviewReturnsCorrectReviewForExistingTask() {
        // Given - Task ID 1 has Review with ID = 1
        long taskId = 1L;

        // When - Get Review
        Optional<ReviewDTO> reviewDTO = underTest.getReviewByTaskId(taskId);

        // Then - Verify Review is returned
        assertThat(reviewDTO).isPresent();
        assertThat(reviewDTO.get().getReviewId()).isEqualTo(1L);
    }

    @Test
    void testThatGetReviewReturnsEmptyForNonExistentTask() {
        // Given - Non-existent task ID
        Long taskId = 999L;

        // When - Get Review
        Optional<ReviewDTO> reviewDTO = underTest.getReviewByTaskId(taskId);

        // Then - Verify empty result
        assertThat(reviewDTO).isEmpty();
    }
    @Test
    void testThatGetUserIDReturnsCorrectTaskerForDifferentTask() {
        // Given - Task ID 2 belongs to user ID 2 from taskData.sql
        Long taskId = 2L;

        // When - Get tasker ID
        Optional<Long> userID = underTest.getUserID(taskId);

        // Then - Verify correct tasker ID
        assertThat(userID).isPresent();
        assertThat(userID.get()).isEqualTo(2L);
    }

    @Test
    void testThatGetUserIDReturnsEmptyForNonExistentTask() {
        // Given - Non-existent task ID
        Long taskId = 999L;

        // When - Get tasker ID
        Optional<Long> userID = underTest.getUserID(taskId);

        // Then - Verify empty result
        assertThat(userID).isEmpty();
    }
}
