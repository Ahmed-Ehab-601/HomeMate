package com.homemate.Interface;

import com.homemate.Dto.ServiceDto;
import com.homemate.Model.ServiceEntity;

import java.sql.SQLException;
import java.util.List;

public interface IServiceDao<S> {

    public void save(S service) throws SQLException;

    public void delete(long id) throws SQLException;

    public void update(long id ,S service) throws SQLException;


    public ServiceEntity get(long serviceID) throws SQLException;

    public List<S> getAll() throws SQLException;

    int countCompletedTasksByServiceId(Long serviceID) throws Exception;

    int countTasker(long serviceID) throws Exception;
}
