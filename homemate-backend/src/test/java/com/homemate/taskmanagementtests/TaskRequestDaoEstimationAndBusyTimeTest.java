package com.homemate.taskmanagementtests;

import com.homemate.TaskerProfile.models.TaskerAvailability;
import com.homemate.taskmanagement.dao.TaskRequestDao;
import com.homemate.taskmanagement.mappers.TaskerBusyTimeMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskRequestDaoEstimationAndBusyTimeTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private com.homemate.taskmanagement.dao.TaskRowMapper taskRowMapper;

    @Mock
    private TaskerBusyTimeMapper taskerBusyTimeMapper;

    @InjectMocks
    private TaskRequestDao taskRequestDao;

    private Long taskId;
    private Long taskerId;
    private LocalDate testDay;

    @BeforeEach
    void setUp() {
        taskId = 100L;
        taskerId = 2L;
        testDay = LocalDate.of(2025, 11, 20);
    }

    // ========== add (Estimation) Tests ==========

    @Test
    void testAddEstimation_Success() {
        // Arrange
        int estimationMinutes = 120; // 2 hours in minutes

        when(jdbcTemplate.update(anyString(), eq(estimationMinutes), eq(taskId))).thenReturn(1);

        // Act
        assertThatCode(() -> taskRequestDao.add(taskId, estimationMinutes))
                .doesNotThrowAnyException();

        // Assert
        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(jdbcTemplate).update(
                sqlCaptor.capture(),
                eq(estimationMinutes),
                eq(taskId)
        );

        String sql = sqlCaptor.getValue();
        assertThat(sql).contains("UPDATE Task");
        assertThat(sql).contains("SET estimation = ?");
        assertThat(sql).contains("WHERE taskID = ?");
    }

    @Test
    void testAddEstimation_WithDifferentEstimationValues() {
        // Arrange
        int[] estimations = {30, 60, 120, 240, 480, 1440}; // Various minutes

        when(jdbcTemplate.update(anyString(), anyInt(), eq(taskId))).thenReturn(1);

        // Act & Assert
        for (int estimation : estimations) {
            assertThatCode(() -> taskRequestDao.add(taskId, estimation))
                    .doesNotThrowAnyException();
        }

        verify(jdbcTemplate, times(estimations.length)).update(anyString(), anyInt(), eq(taskId));
    }

    @Test
    void testAddEstimation_WithZeroEstimation() {
        // Arrange
        int estimationMinutes = 0;

        when(jdbcTemplate.update(anyString(), eq(estimationMinutes), eq(taskId))).thenReturn(1);

        // Act
        assertThatCode(() -> taskRequestDao.add(taskId, estimationMinutes))
                .doesNotThrowAnyException();

        // Assert
        verify(jdbcTemplate).update(anyString(), eq(0), eq(taskId));
    }

    @Test
    void testAddEstimation_WithLargeEstimation() {
        // Arrange
        int estimationMinutes = 2880; // 48 hours

        when(jdbcTemplate.update(anyString(), eq(estimationMinutes), eq(taskId))).thenReturn(1);

        // Act
        assertThatCode(() -> taskRequestDao.add(taskId, estimationMinutes))
                .doesNotThrowAnyException();

        // Assert
        verify(jdbcTemplate).update(anyString(), eq(2880), eq(taskId));
    }

    // ========== getBusytime Tests ==========

    @Test
    void testGetBusytime_Success_WithTasks() {
        // Arrange
        Map<LocalDateTime, Integer> expectedBusyTimes = new HashMap<>();
        expectedBusyTimes.put(LocalDateTime.of(2025, 11, 20, 10, 0), 120);
        expectedBusyTimes.put(LocalDateTime.of(2025, 11, 20, 14, 0), 60);

        LocalDateTime startOfDay = testDay.atStartOfDay();
        LocalDateTime endOfDay = testDay.plusDays(1).atStartOfDay();

        when(jdbcTemplate.query(
                anyString(),
                eq(taskerBusyTimeMapper),
                eq(taskerId),
                eq(Timestamp.valueOf(startOfDay)),
                eq(Timestamp.valueOf(endOfDay))
        )).thenReturn(expectedBusyTimes);

        // Act
        Map<LocalDateTime, Integer> result = taskRequestDao.getBusytime(taskerId, testDay);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result).containsEntry(LocalDateTime.of(2025, 11, 20, 10, 0), 120);
        assertThat(result).containsEntry(LocalDateTime.of(2025, 11, 20, 14, 0), 60);

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(jdbcTemplate).query(
                sqlCaptor.capture(),
                eq(taskerBusyTimeMapper),
                eq(taskerId),
                eq(Timestamp.valueOf(startOfDay)),
                eq(Timestamp.valueOf(endOfDay))
        );

        String sql = sqlCaptor.getValue();
        assertThat(sql).contains("SELECT");
        assertThat(sql).contains("startDate");
        assertThat(sql).contains("estimation");
        assertThat(sql).contains("FROM Task");
        assertThat(sql).contains("WHERE taskerID = ?");
        assertThat(sql).contains("startDate >= ?");
        assertThat(sql).contains("startDate < ?");
    }

    @Test
    void testGetBusytime_ReturnsEmptyMap_WhenNoTasks() {
        // Arrange
        Map<LocalDateTime, Integer> emptyBusyTimes = new HashMap<>();
        LocalDateTime startOfDay = testDay.atStartOfDay();
        LocalDateTime endOfDay = testDay.plusDays(1).atStartOfDay();

        when(jdbcTemplate.query(
                anyString(),
                eq(taskerBusyTimeMapper),
                eq(taskerId),
                eq(Timestamp.valueOf(startOfDay)),
                eq(Timestamp.valueOf(endOfDay))
        )).thenReturn(emptyBusyTimes);

        // Act
        Map<LocalDateTime, Integer> result = taskRequestDao.getBusytime(taskerId, testDay);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();

        verify(jdbcTemplate).query(
                anyString(),
                eq(taskerBusyTimeMapper),
                eq(taskerId),
                eq(Timestamp.valueOf(startOfDay)),
                eq(Timestamp.valueOf(endOfDay))
        );
    }

    @Test
    void testGetBusytime_WithDifferentDays() {
        // Arrange
        LocalDate day1 = LocalDate.of(2025, 11, 20);
        LocalDate day2 = LocalDate.of(2025, 11, 21);

        Map<LocalDateTime, Integer> busyTimesDay1 = new HashMap<>();
        busyTimesDay1.put(LocalDateTime.of(2025, 11, 20, 10, 0), 120);

        Map<LocalDateTime, Integer> busyTimesDay2 = new HashMap<>();
        busyTimesDay2.put(LocalDateTime.of(2025, 11, 21, 14, 0), 60);

        when(jdbcTemplate.query(
                anyString(),
                eq(taskerBusyTimeMapper),
                eq(taskerId),
                any(Timestamp.class),
                any(Timestamp.class)
        )).thenReturn(busyTimesDay1, busyTimesDay2);

        // Act
        Map<LocalDateTime, Integer> result1 = taskRequestDao.getBusytime(taskerId, day1);
        Map<LocalDateTime, Integer> result2 = taskRequestDao.getBusytime(taskerId, day2);

        // Assert
        assertThat(result1).hasSize(1);
        assertThat(result1).containsEntry(LocalDateTime.of(2025, 11, 20, 10, 0), 120);

        assertThat(result2).hasSize(1);
        assertThat(result2).containsEntry(LocalDateTime.of(2025, 11, 21, 14, 0), 60);

        verify(jdbcTemplate, times(2)).query(
                anyString(),
                eq(taskerBusyTimeMapper),
                eq(taskerId),
                any(Timestamp.class),
                any(Timestamp.class)
        );
    }

    @Test
    void testGetBusytime_WithMultipleTasksOnSameDay() {
        // Arrange
        Map<LocalDateTime, Integer> expectedBusyTimes = new HashMap<>();
        expectedBusyTimes.put(LocalDateTime.of(2025, 11, 20, 8, 0), 60);
        expectedBusyTimes.put(LocalDateTime.of(2025, 11, 20, 10, 0), 120);
        expectedBusyTimes.put(LocalDateTime.of(2025, 11, 20, 14, 0), 90);
        expectedBusyTimes.put(LocalDateTime.of(2025, 11, 20, 16, 0), 30);

        LocalDateTime startOfDay = testDay.atStartOfDay();
        LocalDateTime endOfDay = testDay.plusDays(1).atStartOfDay();

        when(jdbcTemplate.query(
                anyString(),
                eq(taskerBusyTimeMapper),
                eq(taskerId),
                eq(Timestamp.valueOf(startOfDay)),
                eq(Timestamp.valueOf(endOfDay))
        )).thenReturn(expectedBusyTimes);

        // Act
        Map<LocalDateTime, Integer> result = taskRequestDao.getBusytime(taskerId, testDay);

        // Assert
        assertThat(result).hasSize(4);
        assertThat(result).containsAllEntriesOf(expectedBusyTimes);
    }

    @Test
    void testGetBusytime_VerifiesCorrectDateRange() {
        // Arrange
        LocalDate day = LocalDate.of(2025, 11, 20);
        LocalDateTime expectedStartOfDay = day.atStartOfDay(); // 2025-11-20 00:00:00
        LocalDateTime expectedEndOfDay = day.plusDays(1).atStartOfDay(); // 2025-11-21 00:00:00

        Map<LocalDateTime, Integer> emptyBusyTimes = new HashMap<>();
        when(jdbcTemplate.query(
                anyString(),
                eq(taskerBusyTimeMapper),
                eq(taskerId),
                any(Timestamp.class),
                any(Timestamp.class)
        )).thenReturn(emptyBusyTimes);

        // Act
        taskRequestDao.getBusytime(taskerId, day);

        // Assert
        verify(jdbcTemplate).query(
                anyString(),
                eq(taskerBusyTimeMapper),
                eq(taskerId),
                eq(Timestamp.valueOf(expectedStartOfDay)),
                eq(Timestamp.valueOf(expectedEndOfDay))
        );
    }

    @Test
    void testGetBusytime_WithDifferentTaskerIds() {
        // Arrange
        Long taskerId1 = 1L;
        Long taskerId2 = 2L;

        Map<LocalDateTime, Integer> busyTimes1 = new HashMap<>();
        busyTimes1.put(LocalDateTime.of(2025, 11, 20, 10, 0), 120);

        Map<LocalDateTime, Integer> busyTimes2 = new HashMap<>();
        busyTimes2.put(LocalDateTime.of(2025, 11, 20, 14, 0), 60);

        when(jdbcTemplate.query(
                anyString(),
                eq(taskerBusyTimeMapper),
                anyLong(),
                any(Timestamp.class),
                any(Timestamp.class)
        )).thenReturn(busyTimes1, busyTimes2);

        // Act
        Map<LocalDateTime, Integer> result1 = taskRequestDao.getBusytime(taskerId1, testDay);
        Map<LocalDateTime, Integer> result2 = taskRequestDao.getBusytime(taskerId2, testDay);

        // Assert
        assertThat(result1).hasSize(1);
        assertThat(result2).hasSize(1);

        verify(jdbcTemplate).query(
                anyString(),
                eq(taskerBusyTimeMapper),
                eq(taskerId1),
                any(Timestamp.class),
                any(Timestamp.class)
        );
        verify(jdbcTemplate).query(
                anyString(),
                eq(taskerBusyTimeMapper),
                eq(taskerId2),
                any(Timestamp.class),
                any(Timestamp.class)
        );
    }
    @Test
    void testCheckAvailability_ReturnsAvailable() {
        // Arrange
        when(jdbcTemplate.queryForObject(anyString(), eq(String.class), eq(taskerId)))
                .thenReturn("AVAILABLE");

        // Act
        TaskerAvailability result = taskRequestDao.CheckAvailability(taskerId);

        // Assert
        assertThat(result).isEqualTo(TaskerAvailability.AVAILABLE);

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(jdbcTemplate).queryForObject(sqlCaptor.capture(), eq(String.class), eq(taskerId));

        String sql = sqlCaptor.getValue();
        assertThat(sql).contains("SELECT availability");
        assertThat(sql).contains("FROM Tasker");
        assertThat(sql).contains("WHERE taskerID = ?");
    }

    @Test
    void testCheckAvailability_ReturnsUnavailable() {
        // Arrange
        when(jdbcTemplate.queryForObject(anyString(), eq(String.class), eq(taskerId)))
                .thenReturn("UNAVAILABLE");

        // Act
        TaskerAvailability result = taskRequestDao.CheckAvailability(taskerId);

        // Assert
        assertThat(result).isEqualTo(TaskerAvailability.UNAVAILABLE);
        verify(jdbcTemplate).queryForObject(anyString(), eq(String.class), eq(taskerId));
    }

}

