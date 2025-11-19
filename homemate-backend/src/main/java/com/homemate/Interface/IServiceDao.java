package com.homemate.Interface;

import java.sql.SQLException;

public interface IServiceDao<S> {

    public void save(S service) throws SQLException;

    public void delete(long id) throws SQLException;

    public void update(long id ,S service) throws SQLException;

    public S get(S service) throws SQLException;

    public Iterable<S> getAll() throws SQLException;

}
