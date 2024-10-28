package dao;

import connection.DatabaseConnection;
import model.Currency;
import model.ExchangeRate;
import utils.NumberFormatter;

import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcExchangeConnection implements CrudManager<ExchangeRate>{
    private final DatabaseConnection dataBaseConnection = new DatabaseConnection();
    private final Connection connect = dataBaseConnection.getConnection();
    private final NumberFormatter formatter = new NumberFormatter();

    public JdbcExchangeConnection() throws SQLException {
    }

    @Override
    public List<ExchangeRate> findAll() throws SQLException {
        String sql =
             """
             SELECT
                er.id,
            
                currency1.id AS base_id,
                currency1.code AS base_code,
                currency1.name AS base_name,
                currency1.sign AS base_sign,
            
                currency2.id AS target_id,
                currency2.code AS target_code,
                currency2.name AS target_name,
                currency2.sign AS target_sign,
            
                er.rate AS rate
            FROM
                exchange_rates er
            JOIN
                currencies currency1 ON er.base_id = currency1.id
            JOIN
                currencies currency2 ON er.target_id = currency2.id
            """;
        try (PreparedStatement preparedStatement = connect.prepareStatement(sql)) {
            ResultSet resultSet = preparedStatement.executeQuery();
            List<ExchangeRate> exchangeRates = new ArrayList<>();
            while (resultSet.next()) {
                ExchangeRate exchangeRate = getExchangeRate(resultSet);
                exchangeRates.add(exchangeRate);
            }
            return exchangeRates;
        }
    }

    @Override
    public int create(ExchangeRate entity) throws SQLException {
        String sql =
            """
            INSERT INTO
                exchange_rates(base_id, target_id, rate)
            VALUES(
               (SELECT id FROM currencies WHERE code = ?),
               (SELECT id FROM currencies WHERE code = ?),
               ?
            )
            """;
        try (PreparedStatement preparedStatement = connect.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            connect.setAutoCommit(false);
            preparedStatement.setString(1, entity.getBaseCurrency().getCode());
            preparedStatement.setString(2, entity.getTargetCurrency().getCode());
            preparedStatement.setDouble(3, formatter.roundToTwoDecimalPlaces(entity.getRate()).doubleValue());
            preparedStatement.executeUpdate();
            ResultSet resultSet = preparedStatement.getGeneratedKeys();
            resultSet.next();
            connect.commit();
            return resultSet.getInt(1);
        }
    }

    @Override
    public void update(ExchangeRate entity) throws SQLException {
        String sql =
            """
            UPDATE
                exchange_rates
            SET
                rate = ?
            WHERE
                base_id = ? AND target_id = ?
            """;
        try (PreparedStatement preparedStatement = connect.prepareStatement(sql)) {
            connect.setAutoCommit(false);
            preparedStatement.setDouble(1, formatter.roundToTwoDecimalPlaces(entity.getRate()).doubleValue());
            preparedStatement.setInt(2, entity.getBaseCurrency().getID());
            preparedStatement.setInt(3, entity.getTargetCurrency().getID());
            preparedStatement.executeUpdate();
            connect.commit();
        }
    }

    @Override
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM exchange_rates WHERE id = ?";
        try (PreparedStatement preparedStatement = connect.prepareStatement(sql)) {
            connect.setAutoCommit(false);
            preparedStatement.setInt(1, id);
            preparedStatement.executeQuery();
            connect.commit();
        }

    }

    public Optional<ExchangeRate> findByCodes(String baseCode, String targetCode) throws SQLException {
        String sql =
            """
            SELECT
                er.id,
            
                currency1.id AS base_id,
                currency1.code AS base_code,
                currency1.name AS base_name,
                currency1.sign AS base_sign,
            
                currency2.id AS target_id,
                currency2.code AS target_code,
                currency2.name AS target_name,
                currency2.sign AS target_sign,
            
                er.rate AS rate
            FROM
                exchange_rates er
            JOIN
                currencies currency1 ON er.base_id = currency1.id
            JOIN
                currencies currency2 ON er.target_id = currency2.id
            WHERE
                currency1.code = ? AND currency2.code = ?
            """;
        try (PreparedStatement preparedStatement = connect.prepareStatement(sql)) {
            preparedStatement.setString(1, baseCode);
            preparedStatement.setString(2, targetCode);
            ResultSet resultSet =  preparedStatement.executeQuery();
            if (resultSet.next()) {
                return Optional.of(getExchangeRate(resultSet));
            }
        }
        return Optional.empty();
    }


    public BigDecimal findRateByUsdBase(String baseCurrency, String targetCurrency) throws SQLException {
        Optional<ExchangeRate> baseToUsdRate = findByCodes(baseCurrency, "USD");
        Optional<ExchangeRate> usdToBaseRate = findByCodes("USD", baseCurrency);
        Optional<ExchangeRate> targetToUsdRate = findByCodes(targetCurrency, "USD");
        Optional<ExchangeRate> usdToTargetRate = findByCodes("USD", targetCurrency);

        if (baseToUsdRate.isPresent()) {
            if (targetToUsdRate.isPresent()) {
                return formatter.roundToTwoDecimalPlaces(
                        baseToUsdRate.get().getRate()
                                .divide(targetToUsdRate.get().getRate(), MathContext.DECIMAL128)
                );
            } else if (usdToTargetRate.isPresent()) {
                return formatter.roundToTwoDecimalPlaces(
                        baseToUsdRate.get().getRate()
                                .multiply(usdToTargetRate.get().getRate())
                );
            }
        } else if (usdToBaseRate.isPresent()) {
            if (targetToUsdRate.isPresent()) {
                return formatter.roundToTwoDecimalPlaces(
                        targetToUsdRate.get().getRate()
                                .divide(usdToBaseRate.get().getRate(), MathContext.DECIMAL128)
                );
            } else if (usdToTargetRate.isPresent()) {
                return formatter.roundToTwoDecimalPlaces(
                        BigDecimal.ONE
                                .divide(usdToBaseRate.get().getRate()
                                        .multiply(usdToTargetRate.get().getRate()), MathContext.DECIMAL128)
                );
            }
        }
        return BigDecimal.ZERO;
    }

    private ExchangeRate getExchangeRate(ResultSet resultSet) throws SQLException {
        return new ExchangeRate(
                    resultSet.getInt("ID"),
                    new Currency(
                            resultSet.getInt("base_id"),
                            resultSet.getString("base_code"),
                            resultSet.getString("base_name"),
                            resultSet.getString("base_sign")
                    ),
                    new Currency(
                            resultSet.getInt("target_id"),
                            resultSet.getString("target_code"),
                            resultSet.getString("target_name"),
                            resultSet.getString("target_sign")
                    ),
                    BigDecimal.valueOf(resultSet.getDouble("rate"))
        );
    }
}