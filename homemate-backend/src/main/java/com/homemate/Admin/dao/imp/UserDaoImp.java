package com.homemate.Admin.dao.imp;
import com.homemate.Admin.dao.UserDao;
import com.homemate.Admin.domain.entities.User;
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

public class UserDaoImp implements UserDao {

    private static final RowMapper<User> USER_ROW_MAPPER = new RowMapperUser();

    private final JdbcTemplate jdbcTemplate;

    public UserDaoImp(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<User> findUsers(List<Filter> filters, Long limit, Long offest) {

        StringBuilder sql=new StringBuilder("SELECT * FROM Users ");
        String whereConditions=buildWhere(filters);
        sql.append(whereConditions);
        sql.append(" ORDER BY userID ASC");
        sql.append(" LIMIT ?, ?");

        List<Object> params = Helper.buildParams(filters);
        // pass offset then limit for MySQL LIMIT offset,count
        params.add(offest);
        params.add(limit);
        return jdbcTemplate.query(sql.toString(), USER_ROW_MAPPER,params.toArray());
    }

    @Override
    public Long countUsers(List<Filter> filters) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM Users");
        String whereConditions = buildWhere(filters);
        sql.append(whereConditions);

        List<Object> params = buildParams(filters);

        Long count = jdbcTemplate.queryForObject(sql.toString(), Long.class, params.toArray());
        return count != null ? count : 0L;
    }



    private static class RowMapperUser implements RowMapper<User> {
        @Override
        public User mapRow(ResultSet rs, int rowNum) throws SQLException {
            User user = new User();
            user.setUserId(rs.getLong("userID"));
            user.setUsername(rs.getString("username"));
            user.setEmail(rs.getString("email"));
            user.setAdmin(rs.getBoolean("admin"));
            user.setPhone(rs.getString("phone"));
            user.setSuspended(rs.getBoolean("suspended"));
            user.setFName(rs.getString("firstName"));
            user.setLName(rs.getString("lastName"));
            String genderStr = rs.getString("gender");
            if (genderStr != null) {
                user.setGender("M".equals(genderStr) ? User.Gender.M : User.Gender.F);
            }

            return user;
        }
    }



}
