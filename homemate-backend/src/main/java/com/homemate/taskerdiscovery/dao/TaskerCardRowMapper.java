package com.homemate.taskerdiscovery.dao;

import com.homemate.taskerdiscovery.model.Tasker;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class TaskerCardRowMapper implements RowMapper<Tasker> {

    @Override
    public Tasker mapRow(ResultSet rs, int rowNum) throws SQLException {
        Tasker tasker = new Tasker();
        tasker.setTaskerID(rs.getInt("taskerID"));
        tasker.setFirstName(rs.getString("firstName"));
        tasker.setLastName(rs.getString("lastName"));
        //tasker.setImage(rs.getBytes("image"));
        tasker.setRating(rs.getDouble("rating"));
        tasker.setAvailability(rs.getString("availability"));
        tasker.setBio(rs.getString("bio"));
        tasker.setAddressCity(rs.getString("addressCity"));
        tasker.setHourRate(rs.getDouble("hourRate"));
        return tasker;
    }
}
