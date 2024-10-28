package service;

import java.sql.SQLException;
import java.util.List;

public interface CrudManager<T> {

    List<T> findAll() throws SQLException;

    int create(T entity) throws SQLException;

    void update(T entity) throws SQLException;

    void delete(int id) throws SQLException;
}
