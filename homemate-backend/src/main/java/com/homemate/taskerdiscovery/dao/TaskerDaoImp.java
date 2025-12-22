package com.homemate.taskerdiscovery.dao;

import com.homemate.taskerdiscovery.dto.FindTaskerCriteriaDto;
import com.homemate.taskerdiscovery.model.Tasker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Repository("taskerDiscoveryDao")
public class TaskerDaoImp implements TaskerDao {

    private final JdbcTemplate jdbcTemplate;
    private final TaskerCardRowMapper rowMapper;

    @Autowired
    public TaskerDaoImp(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.rowMapper = new TaskerCardRowMapper();
    }

    @Override
    public List<Tasker> findTaskersWithFilters(FindTaskerCriteriaDto filter, int page, int pageSize) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT taskerID, firstName, lastName, image, rating, availability, bio, addressCity, hourRate ,stripe_account_id " +
                "FROM Tasker WHERE 1=1");
        List<Object> params = new ArrayList<>();
        try {
            if (filter.getServiceID() != null) {
                sql.append(" AND serviceID = ?");
                params.add(filter.getServiceID());
            }


            if (filter.getAvailability() != null && !filter.getAvailability().isEmpty()) {
                sql.append(" AND availability = ?");
                params.add(filter.getAvailability());
            }
            if (filter.getGender() != null && !filter.getGender().isEmpty()) {
                sql.append(" AND gender = ?");
                params.add(filter.getGender());
            }
            if (filter.getMinRating() != null) {
                sql.append(" AND rating >= ?");
                params.add(filter.getMinRating());
            }
            if (filter.getMaxRating() != null) {
                sql.append(" AND rating <= ?");
                params.add(filter.getMaxRating());
            }
            if (filter.getMinHourRate() != null) {
                sql.append(" AND hourRate >= ?");
                params.add(filter.getMinHourRate());
            }
            if (filter.getMaxHourRate() != null) {
                sql.append(" AND hourRate <= ?");
                params.add(filter.getMaxHourRate());
            }
            if (filter.getCity() != null && !filter.getCity().isEmpty()) {
                sql.append(" AND addressCity = ?");
                params.add(filter.getCity());
            }

            if (filter.getSearch() != null && !filter.getSearch().isEmpty()) {
                sql.append(" AND (firstName LIKE ? OR lastName LIKE ?)");
                String searchPattern = "%" + filter.getSearch() + "%";
                params.add(searchPattern);
                params.add(searchPattern);
            }

            if (filter.getSortBy() != null && !filter.getSortBy().isEmpty()) {
                String sortColumn;
                switch (filter.getSortBy().toLowerCase()) {
                    case "rating":
                        sortColumn = "rating";
                        break;
                    case "hourrate":
                        sortColumn = "hourRate";
                        break;
                    default:
                        sortColumn = "firstName";
                        break;
                }
                sql.append(" ORDER BY ").append(sortColumn);
                if ("desc".equalsIgnoreCase(filter.getSortOrder())) {
                    sql.append(" DESC");
                } else {
                    sql.append(" ASC");
                }

                sql.append(", taskerID ASC");
            } else {
                sql.append(" ORDER BY taskerID ASC");
            }

            int offset = (page - 1) * pageSize;

            if (offset < 0) {
                System.out.println("errrroooo +" + offset);
            }

            sql.append(" LIMIT ? OFFSET ?");
            params.add(pageSize);
            params.add(offset);

            return jdbcTemplate.query(sql.toString(), params.toArray(), rowMapper);
        } catch (Exception e) {
            System.err.println("Error fetching taskers: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    public Tasker findTaskerByID(long taskerID){
        String sql = "SELECT * FROM Tasker WHERE taskerID = ?";
        return jdbcTemplate.queryForObject(sql, rowMapper,taskerID);
    }
}