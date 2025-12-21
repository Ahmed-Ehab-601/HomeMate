package com.homemate.analysis.dao.imp;

import com.homemate.analysis.dao.AnalysisDao;
import com.homemate.analysis.dto.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class AnalysisDaoImp implements AnalysisDao {

    private final JdbcTemplate jdbcTemplate;

    public AnalysisDaoImp(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public UserAnalysisDto getUserAnalysis() {
        return UserAnalysisDto.builder()
                .genderDistribution(getGenderDistribution())
                .ageDistribution(getAgeDistribution())
                .suspendedUsersCount(getSuspendedUsersCount())
                .averageTasksPerUser(getAverageTasksPerUser())
                .usersWithNoCompletedTasksCount(getUsersWithNoCompletedTasksCount())
                .build();
    }

    @Override
    public TaskerAnalysisDto getTaskerAnalysis() {
        return TaskerAnalysisDto.builder()
                .taskersPerService(getTaskersPerService())
                .availabilityDistribution(getAvailabilityDistribution())
                .build();
    }

    @Override
    public TaskAnalysisDto getTaskAnalysis() {
        return TaskAnalysisDto.builder()
                .statusOverview(getTaskStatusOverview())
                .tasksPerService(getTasksPerService())
                .averageBillPerService(getAverageBillPerService())
                .averageTaskDurationHours(getAverageTaskDuration())
                .peakCreationHours(getPeakCreationHours())
                .tasksWithReportsCount(getTasksWithReportsCount())
                .tasksWithDelayedCompletionCount(0) // Logic depends on 'expected' which is missing from schema
                .build();
    }

    // --- User Queries ---

    private Map<String, Integer> getGenderDistribution() {
        String sql = "SELECT gender, COUNT(*) FROM Users GROUP BY gender";
        Map<String, Integer> map = new HashMap<>();
        jdbcTemplate.query(sql, rs -> {
            String g = rs.getString("gender");
            if (g == null) g = "Unknown";
            map.put(g, rs.getInt(2));
        });
        return map;
    }

    private Map<String, Integer> getAgeDistribution() {
        // Calculate age from birthDate
        String sql = "SELECT timestampdiff(YEAR, birthDate, CURDATE()) as age, COUNT(*) as cnt FROM Users WHERE birthDate IS NOT NULL GROUP BY age";
        Map<String, Integer> map = new HashMap<>();
        jdbcTemplate.query(sql, rs -> {
            map.put(String.valueOf(rs.getInt("age")), rs.getInt("cnt"));
        });
        return map;
    }



    private Integer getSuspendedUsersCount() {
        String sql = "SELECT COUNT(*) FROM Users WHERE suspended = TRUE";
        return jdbcTemplate.queryForObject(sql, Integer.class);
    }

    private Double getAverageTasksPerUser() {
        String sql = "SELECT CAST(COUNT(taskID) AS CHAR) FROM Task"; // Cast to facilitate double div in java or SQL
        Long totalTasks = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM Task", Long.class);
        Long totalUsers = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM Users", Long.class);
        if (totalUsers == null || totalUsers == 0) return 0.0;
        return (double) totalTasks / totalUsers;
    }

    private Integer getUsersWithNoCompletedTasksCount() {
        String sql = "SELECT COUNT(*) FROM Users u WHERE NOT EXISTS (SELECT 1 FROM Task t WHERE t.userID = u.userID AND t.status = 'Done')";
        return jdbcTemplate.queryForObject(sql, Integer.class);
    }



    // --- Tasker Queries ---

    private Map<String, Integer> getTaskersPerService() {
        String sql = "SELECT s.name, COUNT(t.taskerID) as cnt FROM Tasker t JOIN Service s ON t.serviceID = s.serviceID GROUP BY s.name";
        Map<String, Integer> map = new HashMap<>();
        jdbcTemplate.query(sql, rs -> {
            map.put(rs.getString("name"), rs.getInt("cnt"));
        });
        return map;
    }

    private Map<String, Integer> getAvailabilityDistribution() {
        String sql = "SELECT availability, COUNT(*) as cnt FROM Tasker GROUP BY availability";
        Map<String, Integer> map = new HashMap<>();
        jdbcTemplate.query(sql, rs -> {
            map.put(rs.getString("availability"), rs.getInt("cnt"));
        });
        return map;
    }
    // --- Task Queries ---

    private Map<String, Integer> getTaskStatusOverview() {
        String sql = "SELECT status, COUNT(*) as cnt FROM Task GROUP BY status";
        Map<String, Integer> map = new HashMap<>();
        jdbcTemplate.query(sql, rs -> {
            map.put(rs.getString("status"), rs.getInt("cnt"));
        });
        return map;
    }

    private Map<String, Integer> getTasksPerService() {
        String sql = "SELECT s.name, COUNT(*) as cnt FROM Task t JOIN Service s ON t.serviceID = s.serviceID GROUP BY s.name";
        Map<String, Integer> map = new HashMap<>();
        jdbcTemplate.query(sql, rs -> {
            map.put(rs.getString("name"), rs.getInt("cnt"));
        });
        return map;
    }

    private Map<String, Double> getAverageBillPerService() {
        String sql = "SELECT s.name, AVG(bill) as avg_bill FROM Task t JOIN Service s ON t.serviceID = s.serviceID GROUP BY s.name";
        Map<String, Double> map = new HashMap<>();
        jdbcTemplate.query(sql, rs -> {
            map.put(rs.getString("name"), rs.getDouble("avg_bill"));
        });
        return map;
    }

    private Double getAverageTaskDuration() {
        // Duration in hours
        String sql = "SELECT AVG(TIMESTAMPDIFF(HOUR, startDate, endDate)) FROM Task WHERE startDate IS NOT NULL AND endDate IS NOT NULL";
        Double val = jdbcTemplate.queryForObject(sql, Double.class);
        return val != null ? val : 0.0;
    }

    private Map<Integer, Integer> getPeakCreationHours() {
        String sql = "SELECT HOUR(startDate) as h, COUNT(*) as cnt FROM Task WHERE startDate IS NOT NULL GROUP BY h ORDER BY h";
        Map<Integer, Integer> map = new LinkedHashMap<>();
        jdbcTemplate.query(sql, rs -> {
            map.put(rs.getInt("h"), rs.getInt("cnt"));
        });
        return map;
    }



    private Integer getTasksWithReportsCount() {
        String sql = "SELECT COUNT(DISTINCT taskID) FROM Report";
        Integer val = jdbcTemplate.queryForObject(sql, Integer.class);
        return val != null ? val : 0;
    }
}
