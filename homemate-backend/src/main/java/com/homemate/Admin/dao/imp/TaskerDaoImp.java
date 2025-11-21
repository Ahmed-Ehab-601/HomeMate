package com.homemate.Admin.dao.imp;

import com.homemate.Admin.dao.TaskerDao;
import com.homemate.Admin.domain.entities.Tasker;
import com.homemate.Admin.domain.filters.Filter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static com.homemate.Admin.dao.imp.Helper.buildParams;
import static com.homemate.Admin.dao.imp.Helper.buildWhere;

@Repository
public class TaskerDaoImp implements TaskerDao {
    private final JdbcTemplate jdbcTemplate;
    private static final RowMapper<Tasker> TASKER_ROW_MAPPER = new RowMapperTasker();

    public TaskerDaoImp(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Tasker> findTaskers(List<Filter> filters, Long limit, Long offest) {

        StringBuilder sql=new StringBuilder("SELECT * FROM Taskers ");
        String whereConditions=buildWhere(filters);
        sql.append(whereConditions);
        sql.append(" ORDER BY taskerID ASC");
        sql.append(" LIMIT ?, ?");
        List<Object> params = Helper.buildParams(filters);
        params.add(offest);
        params.add(limit);
        return jdbcTemplate.query(sql.toString(), TASKER_ROW_MAPPER,params.toArray());
    }



    @Override
    public Long countTaskers(List<Filter> filters) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM Taskers");
        String whereConditions = buildWhere(filters);
        sql.append(whereConditions);

        List<Object> params = buildParams(filters);

        Long count = jdbcTemplate.queryForObject(sql.toString(), Long.class, params.toArray());
        return count != null ? count : 0L;
    }




    private static class RowMapperTasker implements RowMapper<Tasker> {

        @Override
        public Tasker mapRow(ResultSet rs, int rowNum) throws SQLException {
            Tasker tasker = new Tasker();
            long taskerIdValue = rs.getLong("taskerID");
            tasker.setTaskerID(rs.wasNull() ? null : taskerIdValue);
            tasker.setUsername(rs.getString("username"));
            tasker.setEmail(rs.getString("email"));
            tasker.setPhone(rs.getString("phone"));
            tasker.setFname(rs.getString("firstName"));
            tasker.setLname(rs.getString("lastName"));
            tasker.setSuspended(rs.getBoolean("suspended"));
            Double avgRating = rs.getObject("rating", Double.class);
            tasker.setAvgRating(avgRating);
            Double hourRate = rs.getObject("hourrate", Double.class);
            tasker.setHourRate(hourRate);
            String genderStr = rs.getString("gender");
            if (genderStr != null) {
                tasker.setGender("M".equalsIgnoreCase(genderStr) ? Tasker.Gender.M : Tasker.Gender.F);
            } else {
                tasker.setGender(null);
            }

            return tasker;
        }
    }

}
