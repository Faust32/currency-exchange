package servlets;

import exceptions.AlreadyExistsException;
import exceptions.DatabaseException;
import exceptions.NotFoundException;
import model.ExchangeRate;
import model.response.ErrorResponse;
import dao.JdbcCurrencyConnection;
import dao.JdbcExchangeConnection;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import utils.ParametersValidity;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static jakarta.servlet.http.HttpServletResponse.*;

@WebServlet("/exchangeRates")
public class ExchangeRatesListServlet extends HttpServlet {
    private final JdbcCurrencyConnection currencyConnection = new JdbcCurrencyConnection();
    private final JdbcExchangeConnection exchangeConnection = new JdbcExchangeConnection();
    private final ParametersValidity parametersValidity = new ParametersValidity();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ExchangeRatesListServlet() throws SQLException {
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            List<ExchangeRate> exchangeRates = exchangeConnection.findAll();
            objectMapper.writeValue(response.getWriter(), exchangeRates);
        } catch (SQLException e) {
            throw new DatabaseException("There is a problem with database.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String baseCurrencyCode = request.getParameter("baseCurrencyCode");
        String targetCurrencyCode = request.getParameter("targetCurrencyCode");
        BigDecimal rate = BigDecimal.valueOf(Double.parseDouble(request.getParameter("rate")));
        parametersValidity.validateCurrencies(baseCurrencyCode, targetCurrencyCode);
        parametersValidity.validateRate(rate);
        try {
            Optional<ExchangeRate> existingRate = exchangeConnection.findByCodes(baseCurrencyCode, targetCurrencyCode);
            Optional<ExchangeRate> reverseRate = exchangeConnection.findByCodes(targetCurrencyCode, baseCurrencyCode);
            if (existingRate.isPresent() || reverseRate.isPresent()) {
                throw new AlreadyExistsException("The exchange rate for this currencies already exists.");
            }
            ExchangeRate exchangeRate = new ExchangeRate(
                    0,
                    currencyConnection.findByCode(baseCurrencyCode),
                    currencyConnection.findByCode(targetCurrencyCode),
                    rate
            );
            int id = exchangeConnection.create(exchangeRate);
            exchangeRate.setID(id);
            objectMapper.writeValue(response.getWriter(), exchangeRate);
        } catch (NoSuchElementException e) {
            throw new NotFoundException("One or both currencies for the exchange rate do not exist in database.");
        }
        catch (SQLException e) {
            throw new DatabaseException("There is a problem with database.");
        }
    }

}