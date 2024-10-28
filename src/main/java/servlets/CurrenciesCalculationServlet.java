package servlets;

import com.fasterxml.jackson.databind.ObjectMapper;
import model.ExchangeRate;
import model.response.ErrorResponse;
import model.response.ExchangeResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import service.JdbcCurrencyConnection;
import service.JdbcExchangeConnection;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

import static jakarta.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
import static jakarta.servlet.http.HttpServletResponse.SC_NOT_FOUND;

@WebServlet("/exchange")
public class CurrenciesCalculationServlet extends HttpServlet {
    private final JdbcCurrencyConnection currencyConnection = new JdbcCurrencyConnection();
    private final JdbcExchangeConnection exchangeConnection = new JdbcExchangeConnection();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public CurrenciesCalculationServlet() throws SQLException {
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String baseCurrency = request.getParameter("from");
        String targetCurrency = request.getParameter("to");
        double amount = Double.parseDouble(request.getParameter("amount"));
        try {
            Optional<ExchangeRate> exchangeRateOptional = exchangeConnection.findByCodes(baseCurrency, targetCurrency);
            Optional<ExchangeRate> reversedExchangeRateOptional = exchangeConnection.findByCodes(targetCurrency, baseCurrency);
            if (exchangeRateOptional.isEmpty() && reversedExchangeRateOptional.isEmpty()) {
                processUsdBaseExchange(response, baseCurrency, targetCurrency, amount);
            } else {
                double rate = 0;
                if (exchangeRateOptional.isPresent()) {
                    rate = exchangeRateOptional.get().getRate();
                }
                if (reversedExchangeRateOptional.isPresent()){
                    rate = 1 / reversedExchangeRateOptional.get().getRate();
                }
                processSuccessfulExchange(response, baseCurrency, targetCurrency, amount, rate);
            }
        } catch (SQLException e) {
            response.setStatus(SC_INTERNAL_SERVER_ERROR);
            ErrorResponse errorResponse = new ErrorResponse(SC_INTERNAL_SERVER_ERROR,
                    "There is a problem with database. Please, try again later.");
            objectMapper.writeValue(response.getWriter(), errorResponse);
        }
    }

    private void processUsdBaseExchange(HttpServletResponse response, String baseCurrencyCode, String targetCurrencyCode, double amount) throws IOException, SQLException {
        double usdExchangeRate = exchangeConnection.findRateByUsdBase(baseCurrencyCode, targetCurrencyCode);
        if (usdExchangeRate != 0) {
            processSuccessfulExchange(response, baseCurrencyCode, targetCurrencyCode, amount, usdExchangeRate);
        } else {
            response.setStatus(SC_NOT_FOUND);
            ErrorResponse errorResponse = new ErrorResponse(SC_NOT_FOUND,
                    "There is no such exchange rate for these currencies. Please try again later.");
            objectMapper.writeValue(response.getWriter(), errorResponse);
        }
    }

    private void processSuccessfulExchange(HttpServletResponse response, String baseCurrency, String targetCurrency, double rate, double amount) throws IOException, SQLException {
        double convertedAmount = amount * rate;
        ExchangeResponse exchangeResponse = new ExchangeResponse(
                currencyConnection.findByCode(baseCurrency),
                currencyConnection.findByCode(targetCurrency),
                rate,
                amount,
                convertedAmount
        );
        objectMapper.writeValue(response.getWriter(), exchangeResponse);
    }
}
