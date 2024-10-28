package servlets;

import com.fasterxml.jackson.databind.ObjectMapper;
import model.ExchangeRate;
import model.response.ErrorResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import service.JdbcCurrencyConnection;
import service.JdbcExchangeConnection;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static jakarta.servlet.http.HttpServletResponse.*;

@WebServlet("/exchangeRates")
public class ExchangeRatesListServlet extends HttpServlet {
    private final JdbcCurrencyConnection currencyConnection = new JdbcCurrencyConnection();
    private final JdbcExchangeConnection exchangeConnection = new JdbcExchangeConnection();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ExchangeRatesListServlet() throws SQLException {
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            List<ExchangeRate> exchangeRates = exchangeConnection.findAll();
            objectMapper.writeValue(response.getWriter(), exchangeRates);
        } catch (SQLException e) {
            response.setStatus(SC_INTERNAL_SERVER_ERROR);
            ErrorResponse errorResponse = new ErrorResponse(SC_INTERNAL_SERVER_ERROR,
                    "There is a problem with database. Please, try again later.");
            objectMapper.writeValue(response.getWriter(), errorResponse);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String baseCurrencyCode = request.getParameter("baseCurrencyCode");
        String targetCurrencyCode = request.getParameter("targetCurrencyCode");
        double rate = Double.parseDouble(request.getParameter("rate"));

        if (baseCurrencyCode == null || baseCurrencyCode.isBlank()) {
            response.setStatus(SC_BAD_REQUEST);
            ErrorResponse errorResponse = new ErrorResponse(SC_BAD_REQUEST,
                    "There is no Base Currency parameter in the request. Please try again later.");
            objectMapper.writeValue(response.getWriter(), errorResponse);
            return;
        }

        if (targetCurrencyCode == null || targetCurrencyCode.isBlank()) {
            response.setStatus(SC_BAD_REQUEST);
            ErrorResponse errorResponse = new ErrorResponse(SC_BAD_REQUEST,
                    "There is no Target Currency parameter in the request. Please try again later.");
            objectMapper.writeValue(response.getWriter(), errorResponse);
            return;
        }

        if (Double.isNaN(rate) || rate <= 0) {
            response.setStatus(SC_BAD_REQUEST);
            ErrorResponse errorResponse = new ErrorResponse(SC_BAD_REQUEST,
                    "There is no rate parameter or it's incorrect in the request. Please try again later.");
            objectMapper.writeValue(response.getWriter(), errorResponse);
            return;
        }
        try {
            Optional<ExchangeRate> existingRate = exchangeConnection.findByCodes(baseCurrencyCode, targetCurrencyCode);
            Optional<ExchangeRate> reverseRate = exchangeConnection.findByCodes(targetCurrencyCode, baseCurrencyCode);
            if (existingRate.isPresent() || reverseRate.isPresent()) {
                response.setStatus(SC_CONFLICT);
                objectMapper.writeValue(response.getWriter(), new ErrorResponse(
                        SC_CONFLICT,
                        "Exchange rate for the specified currency pair or its reverse already exists. Please try again later."
                ));
                return;
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
            response.setStatus(SC_NOT_FOUND);
            objectMapper.writeValue(response.getWriter(), new ErrorResponse(
                    SC_NOT_FOUND,
                    "One or both currencies for which you are trying to add an exchange rate does not exist in the database. Please try again later."
            ));
        }
        catch (SQLException e) {
            response.setStatus(SC_INTERNAL_SERVER_ERROR);
            ErrorResponse errorResponse = new ErrorResponse(SC_INTERNAL_SERVER_ERROR,
                    "There is a problem with database. Please, try again later.");
            objectMapper.writeValue(response.getWriter(), errorResponse);
        }
    }

}