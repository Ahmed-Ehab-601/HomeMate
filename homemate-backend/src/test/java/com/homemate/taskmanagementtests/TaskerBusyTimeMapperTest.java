package com.homemate.taskmanagementtests;

import com.homemate.taskmanagement.mappers.TaskerBusyTimeMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskerBusyTimeMapperTest {

    private TaskerBusyTimeMapper mapper;
    private ResultSet resultSet;

    @BeforeEach
    void setUp() {
        mapper = new TaskerBusyTimeMapper();
        resultSet = mock(ResultSet.class);
    }

    // ========== extractData Tests ==========

    @Test
    void testExtractData_Success_WithSingleRow() throws SQLException {
        // Arrange
        LocalDateTime startDate = LocalDateTime.of(2025, 11, 20, 10, 0);
        int estimation = 120;

        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getTimestamp("startDate")).thenReturn(Timestamp.valueOf(startDate));
        when(resultSet.getInt("estimation")).thenReturn(estimation);

        // Act
        Map<LocalDateTime, Integer> result = mapper.extractData(resultSet);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result).containsEntry(startDate, estimation);

        verify(resultSet, times(2)).next();
        verify(resultSet).getTimestamp("startDate");
        verify(resultSet).getInt("estimation");
    }

    @Test
    void testExtractData_Success_WithMultipleRows() throws SQLException {
        // Arrange
        LocalDateTime startDate1 = LocalDateTime.of(2025, 11, 20, 10, 0);
        LocalDateTime startDate2 = LocalDateTime.of(2025, 11, 20, 14, 0);
        LocalDateTime startDate3 = LocalDateTime.of(2025, 11, 20, 16, 30);

        when(resultSet.next()).thenReturn(true, true, true, false);
        when(resultSet.getTimestamp("startDate"))
                .thenReturn(Timestamp.valueOf(startDate1))
                .thenReturn(Timestamp.valueOf(startDate2))
                .thenReturn(Timestamp.valueOf(startDate3));
        when(resultSet.getInt("estimation"))
                .thenReturn(120)
                .thenReturn(60)
                .thenReturn(90);

        // Act
        Map<LocalDateTime, Integer> result = mapper.extractData(resultSet);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(3);
        assertThat(result).containsEntry(startDate1, 120);
        assertThat(result).containsEntry(startDate2, 60);
        assertThat(result).containsEntry(startDate3, 90);

        verify(resultSet, times(4)).next();
        verify(resultSet, times(3)).getTimestamp("startDate");
        verify(resultSet, times(3)).getInt("estimation");
    }

    @Test
    void testExtractData_ReturnsEmptyMap_WhenNoRows() throws SQLException {
        // Arrange
        when(resultSet.next()).thenReturn(false);

        // Act
        Map<LocalDateTime, Integer> result = mapper.extractData(resultSet);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();

        verify(resultSet).next();
        verify(resultSet, never()).getTimestamp(anyString());
        verify(resultSet, never()).getInt(anyString());
    }

    @Test
    void testExtractData_WithZeroEstimation() throws SQLException {
        // Arrange
        LocalDateTime startDate = LocalDateTime.of(2025, 11, 20, 10, 0);

        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getTimestamp("startDate")).thenReturn(Timestamp.valueOf(startDate));
        when(resultSet.getInt("estimation")).thenReturn(0);

        // Act
        Map<LocalDateTime, Integer> result = mapper.extractData(resultSet);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result).containsEntry(startDate, 0);
    }

    @Test
    void testExtractData_WithVariousTimeSlots() throws SQLException {
        // Arrange
        LocalDateTime morning = LocalDateTime.of(2025, 11, 20, 8, 0);
        LocalDateTime noon = LocalDateTime.of(2025, 11, 20, 12, 0);
        LocalDateTime afternoon = LocalDateTime.of(2025, 11, 20, 15, 30);
        LocalDateTime evening = LocalDateTime.of(2025, 11, 20, 18, 45);

        when(resultSet.next()).thenReturn(true, true, true, true, false);
        when(resultSet.getTimestamp("startDate"))
                .thenReturn(Timestamp.valueOf(morning))
                .thenReturn(Timestamp.valueOf(noon))
                .thenReturn(Timestamp.valueOf(afternoon))
                .thenReturn(Timestamp.valueOf(evening));
        when(resultSet.getInt("estimation"))
                .thenReturn(60)
                .thenReturn(120)
                .thenReturn(90)
                .thenReturn(30);

        // Act
        Map<LocalDateTime, Integer> result = mapper.extractData(resultSet);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(4);
        assertThat(result).containsEntry(morning, 60);
        assertThat(result).containsEntry(noon, 120);
        assertThat(result).containsEntry(afternoon, 90);
        assertThat(result).containsEntry(evening, 30);
    }

    @Test
    void testExtractData_ThrowsSQLException_WhenColumnNotFound() throws SQLException {
        // Arrange
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getTimestamp("startDate")).thenThrow(new SQLException("Column not found"));

        // Act & Assert
        assertThatThrownBy(() -> mapper.extractData(resultSet))
                .isInstanceOf(SQLException.class)
                .hasMessage("Column not found");

        verify(resultSet).next();
        verify(resultSet).getTimestamp("startDate");
    }




}