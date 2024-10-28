package dao;

import connection.DatabaseConnection;
import model.Currency;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JdbcCurrencyConnection implements CrudManager<Currency> {
    private final DatabaseConnection dataBaseConnection = new DatabaseConnection();
    private final Connection connect = dataBaseConnection.getConnection();

    public JdbcCurrencyConnection() throws SQLException {
    }

    @Override
    public List<Currency> findAll() throws SQLException {
        List<Currency> currencies = new ArrayList<>();
        String sql = "SELECT * FROM currencies";
        try (PreparedStatement preparedStatement = connect.prepareStatement(sql)) {
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Currency currency = getCurrency(resultSet);
                currencies.add(currency);
            }
            return currencies;
        }
    }

    @Override
    public int create(Currency currency) throws SQLException {
        String sql = "INSERT INTO currencies(code, name, sign) VALUES(?, ?, ?)";
        try (PreparedStatement preparedStatement = connect.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            connect.setAutoCommit(false);
            preparedStatement.setString(1, currency.getCode());
            preparedStatement.setString(2, currency.getName());
            preparedStatement.setString(3, currency.getSign());
            preparedStatement.executeUpdate();
            ResultSet resultSet = preparedStatement.getGeneratedKeys();
            connect.commit();
            return resultSet.getInt(1);
        }
    }
    @Override
    public void update(Currency entity) throws SQLException {
        String sql = "UPDATE currencies SET name = ?, code = ?, sign = ? WHERE id = ?";
        try (PreparedStatement preparedStatement = connect.prepareStatement(sql)) {
            connect.setAutoCommit(false);
            preparedStatement.setString(1, entity.getName());
            preparedStatement.setString(2, entity.getCode());
            preparedStatement.setString(3, entity.getSign());
            preparedStatement.setInt(4, entity.getID());
            preparedStatement.executeUpdate();
            connect.commit();
        }
    }

    @Override
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM currencies WHERE id = ?";
        try (PreparedStatement preparedStatement = connect.prepareStatement(sql)) {
            connect.setAutoCommit(false);
            preparedStatement.setInt(1, id);
            preparedStatement.executeUpdate();
            connect.commit();
        }
    }

    public Currency findByCode(String code) throws SQLException {
        String sql = "SELECT * FROM currencies WHERE code = ?";
        try (PreparedStatement preparedStatement = connect.prepareStatement(sql)) {
            preparedStatement.setString(1, code);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return getCurrency(resultSet);
            }
        }
        return null;
    }

    private static Currency getCurrency(ResultSet resultSet) throws SQLException {
        return new Currency (resultSet.getInt("id"),
                resultSet.getString("code"),
                resultSet.getString("name"),
                resultSet.getString("sign")
                );
    }
}
