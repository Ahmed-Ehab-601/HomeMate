package com.homemate.analysis.dao;

import com.homemate.analysis.dto.*;
import org.springframework.jdbc.core.JdbcTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class TaskerAnalysisDao {
    private final JdbcTemplate jdbcTemplate;

    public GenderCountResponse fetchGenderCounts() {
        String sql = "SELECT " +
                "SUM(CASE WHEN gender = 'M' THEN 1 ELSE 0 END) AS male, " +
                "SUM(CASE WHEN gender = 'F' THEN 1 ELSE 0 END) AS female " +
                "FROM Tasker";
        return jdbcTemplate.query(sql, rs -> {
            if (rs.next()) {
                return new GenderCountResponse(rs.getLong("male"), rs.getLong("female"));
            }
            return new GenderCountResponse(0, 0);
        });
    }

    public AgeBucketsResponse fetchAgeBuckets() {
        String sql = "SELECT " +
                "SUM(CASE WHEN age BETWEEN 0 AND 9 THEN 1 ELSE 0 END) AS b_0_10, " +
                "SUM(CASE WHEN age BETWEEN 10 AND 19 THEN 1 ELSE 0 END) AS b_10_20, " +
                "SUM(CASE WHEN age BETWEEN 20 AND 29 THEN 1 ELSE 0 END) AS b_20_30, " +
                "SUM(CASE WHEN age BETWEEN 30 AND 39 THEN 1 ELSE 0 END) AS b_30_40, " +
                "SUM(CASE WHEN age BETWEEN 40 AND 49 THEN 1 ELSE 0 END) AS b_40_50, " +
                "SUM(CASE WHEN age BETWEEN 50 AND 59 THEN 1 ELSE 0 END) AS b_50_60, " +
                "SUM(CASE WHEN age >= 60 THEN 1 ELSE 0 END) AS b_60_plus " +
                "FROM (SELECT TIMESTAMPDIFF(YEAR, birthDate, CURDATE()) AS age FROM Tasker WHERE birthDate IS NOT NULL) t";

        return jdbcTemplate.query(sql, rs -> {
            Map<String, Long> buckets = new HashMap<>();
            if (rs.next()) {
                buckets.put("0-10", rs.getLong("b_0_10"));
                buckets.put("10-20", rs.getLong("b_10_20"));
                buckets.put("20-30", rs.getLong("b_20_30"));
                buckets.put("30-40", rs.getLong("b_30_40"));
                buckets.put("40-50", rs.getLong("b_40_50"));
                buckets.put("50-60", rs.getLong("b_50_60"));
                buckets.put("60+", rs.getLong("b_60_plus"));
            }
            return new AgeBucketsResponse(buckets);
        });
    }

    public NewAccountsResponse fetchNewAccountsCounts(NewAccountsRequest request) {
        List<TimeRange> ranges = request.getRanges();
        String sql = "SELECT COUNT(*) FROM Tasker WHERE createdTime >= ? AND createdTime < ?";
        return new NewAccountsResponse(
                ranges.stream().map(r -> {
                    Timestamp from = Timestamp.valueOf(r.getFrom());
                    Timestamp to = Timestamp.valueOf(r.getTo());
                    return jdbcTemplate.queryForObject(sql, Long.class, from, to);
                }).toList()
        );
    }

    public RatingRangesResponse fetchRatingRanges() {
        String sql = "SELECT " +
                "SUM(CASE WHEN rating >= 0 AND rating < 1 THEN 1 ELSE 0 END) AS r_0_1, " +
                "SUM(CASE WHEN rating >= 1 AND rating < 2 THEN 1 ELSE 0 END) AS r_1_2, " +
                "SUM(CASE WHEN rating >= 2 AND rating < 3 THEN 1 ELSE 0 END) AS r_2_3, " +
                "SUM(CASE WHEN rating >= 3 AND rating < 4 THEN 1 ELSE 0 END) AS r_3_4, " +
                "SUM(CASE WHEN rating >= 4 AND rating <= 5 THEN 1 ELSE 0 END) AS r_4_5 " +
                "FROM Tasker";

        return jdbcTemplate.query(sql, rs -> {
            Map<String, Long> ranges = new HashMap<>();
            if (rs.next()) {
                ranges.put("0-1", rs.getLong("r_0_1"));
                ranges.put("1-2", rs.getLong("r_1_2"));
                ranges.put("2-3", rs.getLong("r_2_3"));
                ranges.put("3-4", rs.getLong("r_3_4"));
                ranges.put("4-5", rs.getLong("r_4_5"));
            }
            return new RatingRangesResponse(ranges);
        });
    }

    public HourRateRangesResponse fetchHourRateRanges() {
        String sql = "SELECT " +
                "SUM(CASE WHEN hourRate >= 0 AND hourRate < 10 THEN 1 ELSE 0 END) AS hr_0_10, " +
                "SUM(CASE WHEN hourRate >= 10 AND hourRate < 20 THEN 1 ELSE 0 END) AS hr_10_20, " +
                "SUM(CASE WHEN hourRate >= 20 AND hourRate < 30 THEN 1 ELSE 0 END) AS hr_20_30, " +
                "SUM(CASE WHEN hourRate >= 30 AND hourRate < 40 THEN 1 ELSE 0 END) AS hr_30_40, " +
                "SUM(CASE WHEN hourRate >= 40 AND hourRate < 50 THEN 1 ELSE 0 END) AS hr_40_50, " +
                "SUM(CASE WHEN hourRate >= 50 AND hourRate < 60 THEN 1 ELSE 0 END) AS hr_50_60, " +
                "SUM(CASE WHEN hourRate >= 60 AND hourRate < 70 THEN 1 ELSE 0 END) AS hr_60_70, " +
                "SUM(CASE WHEN hourRate >= 70 AND hourRate < 80 THEN 1 ELSE 0 END) AS hr_70_80, " +
                "SUM(CASE WHEN hourRate >= 80 THEN 1 ELSE 0 END) AS hr_80_plus " +
                "FROM Tasker";

        return jdbcTemplate.query(sql, rs -> {
            Map<String, Long> ranges = new HashMap<>();
            if (rs.next()) {
                ranges.put("0-10", rs.getLong("hr_0_10"));
                ranges.put("10-20", rs.getLong("hr_10_20"));
                ranges.put("20-30", rs.getLong("hr_20_30"));
                ranges.put("30-40", rs.getLong("hr_30_40"));
                ranges.put("40-50", rs.getLong("hr_40_50"));
                ranges.put("50-60", rs.getLong("hr_50_60"));
                ranges.put("60-70", rs.getLong("hr_60_70"));
                ranges.put("70-80", rs.getLong("hr_70_80"));
                ranges.put("80+", rs.getLong("hr_80_plus"));
            }
            return new HourRateRangesResponse(ranges);
        });
    }

    public WorkedHoursRangesResponse fetchWorkedHoursRanges() {
        String sql = "SELECT " +
                "SUM(CASE WHEN WorkedHours >= 0 AND WorkedHours < 10 THEN 1 ELSE 0 END) AS wh_0_10, " +
                "SUM(CASE WHEN WorkedHours >= 10 AND WorkedHours < 20 THEN 1 ELSE 0 END) AS wh_10_20, " +
                "SUM(CASE WHEN WorkedHours >= 20 AND WorkedHours < 30 THEN 1 ELSE 0 END) AS wh_20_30, " +
                "SUM(CASE WHEN WorkedHours >= 30 AND WorkedHours < 40 THEN 1 ELSE 0 END) AS wh_30_40, " +
                "SUM(CASE WHEN WorkedHours >= 40 AND WorkedHours < 50 THEN 1 ELSE 0 END) AS wh_40_50, " +
                "SUM(CASE WHEN WorkedHours >= 50 AND WorkedHours < 60 THEN 1 ELSE 0 END) AS wh_50_60, " +
                "SUM(CASE WHEN WorkedHours >= 60 AND WorkedHours < 70 THEN 1 ELSE 0 END) AS wh_60_70, " +
                "SUM(CASE WHEN WorkedHours >= 70 AND WorkedHours < 80 THEN 1 ELSE 0 END) AS wh_70_80, " +
                "SUM(CASE WHEN WorkedHours >= 80 AND WorkedHours < 90 THEN 1 ELSE 0 END) AS wh_80_90, " +
                "SUM(CASE WHEN WorkedHours >= 90 AND WorkedHours < 100 THEN 1 ELSE 0 END) AS wh_90_100, " +
                "SUM(CASE WHEN WorkedHours >= 100 AND WorkedHours < 110 THEN 1 ELSE 0 END) AS wh_100_110, " +
                "SUM(CASE WHEN WorkedHours >= 110 AND WorkedHours < 120 THEN 1 ELSE 0 END) AS wh_110_120, " +
                "SUM(CASE WHEN WorkedHours >= 120 AND WorkedHours < 130 THEN 1 ELSE 0 END) AS wh_120_130, " +
                "SUM(CASE WHEN WorkedHours >= 130 AND WorkedHours < 140 THEN 1 ELSE 0 END) AS wh_130_140, " +
                "SUM(CASE WHEN WorkedHours >= 140 AND WorkedHours < 150 THEN 1 ELSE 0 END) AS wh_140_150, " +
                "SUM(CASE WHEN WorkedHours >= 150 AND WorkedHours < 160 THEN 1 ELSE 0 END) AS wh_150_160, " +
                "SUM(CASE WHEN WorkedHours >= 160 AND WorkedHours < 170 THEN 1 ELSE 0 END) AS wh_160_170, " +
                "SUM(CASE WHEN WorkedHours >= 170 AND WorkedHours < 180 THEN 1 ELSE 0 END) AS wh_170_180, " +
                "SUM(CASE WHEN WorkedHours >= 180 AND WorkedHours < 190 THEN 1 ELSE 0 END) AS wh_180_190, " +
                "SUM(CASE WHEN WorkedHours >= 190 AND WorkedHours < 200 THEN 1 ELSE 0 END) AS wh_190_200, " +
                "SUM(CASE WHEN WorkedHours >= 200 THEN 1 ELSE 0 END) AS wh_200_plus " +
                "FROM Tasker";

        return jdbcTemplate.query(sql, rs -> {
            Map<String, Long> ranges = new HashMap<>();
            if (rs.next()) {
                ranges.put("0-10", rs.getLong("wh_0_10"));
                ranges.put("10-20", rs.getLong("wh_10_20"));
                ranges.put("20-30", rs.getLong("wh_20_30"));
                ranges.put("30-40", rs.getLong("wh_30_40"));
                ranges.put("40-50", rs.getLong("wh_40_50"));
                ranges.put("50-60", rs.getLong("wh_50_60"));
                ranges.put("60-70", rs.getLong("wh_60_70"));
                ranges.put("70-80", rs.getLong("wh_70_80"));
                ranges.put("80-90", rs.getLong("wh_80_90"));
                ranges.put("90-100", rs.getLong("wh_90_100"));
                ranges.put("100-110", rs.getLong("wh_100_110"));
                ranges.put("110-120", rs.getLong("wh_110_120"));
                ranges.put("120-130", rs.getLong("wh_120_130"));
                ranges.put("130-140", rs.getLong("wh_130_140"));
                ranges.put("140-150", rs.getLong("wh_140_150"));
                ranges.put("150-160", rs.getLong("wh_150_160"));
                ranges.put("160-170", rs.getLong("wh_160_170"));
                ranges.put("170-180", rs.getLong("wh_170_180"));
                ranges.put("180-190", rs.getLong("wh_180_190"));
                ranges.put("190-200", rs.getLong("wh_190_200"));
                ranges.put("200+", rs.getLong("wh_200_plus"));
            }
            return new WorkedHoursRangesResponse(ranges);
        });
    }

    public ServiceCountResponse fetchServiceCounts(ServiceCountRequest request) {
        List<Long> serviceIds = request.getServiceIds();
        if (serviceIds == null || serviceIds.isEmpty()) {
            return new ServiceCountResponse(new HashMap<>());
        }

        Map<Long, Long> counts = new HashMap<>();
        String sql = "SELECT COUNT(*) FROM Tasker WHERE serviceID = ?";
        
        for (Long serviceId : serviceIds) {
            Long count = jdbcTemplate.queryForObject(sql, Long.class, serviceId);
            counts.put(serviceId, count != null ? count : 0L);
        }
        
        return new ServiceCountResponse(counts);
    }

    public CityCountResponse fetchCityCounts(CityCountRequest request) {
        List<String> cities = request.getCities();
        if (cities == null || cities.isEmpty()) {
            return new CityCountResponse(new HashMap<>());
        }

        Map<String, Long> counts = new HashMap<>();
        String sql = "SELECT COUNT(*) FROM Tasker WHERE addressCity = ?";
        
        for (String city : cities) {
            Long count = jdbcTemplate.queryForObject(sql, Long.class, city);
            counts.put(city, count != null ? count : 0L);
        }
        
        return new CityCountResponse(counts);
    }

    public TaskerStatusCountsResponse fetchStatusCounts() {
        long suspended = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM Tasker WHERE suspended = TRUE",
                Long.class
        );
        long active = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM Tasker WHERE suspended = FALSE",
                Long.class
        );
        return new TaskerStatusCountsResponse(suspended, active);
    }
}
