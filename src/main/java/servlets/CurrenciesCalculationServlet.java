package servlets;

import exceptions.DatabaseException;
import exceptions.NotFoundException;
import model.ExchangeRate;
import model.response.ExchangeResponse;
import dao.JdbcCurrencyConnection;
import dao.JdbcExchangeConnection;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import utils.ParametersValidity;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.SQLException;
import java.util.Optional;


@WebServlet("/exchange")
public class CurrenciesCalculationServlet extends HttpServlet {
    private final JdbcCurrencyConnection currencyConnection = new JdbcCurrencyConnection();
    private final JdbcExchangeConnection exchangeConnection = new JdbcExchangeConnection();
    private final ParametersValidity parametersValidity = new ParametersValidity();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public CurrenciesCalculationServlet() throws SQLException {
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String baseCurrencyCode = request.getParameter("from");
        String targetCurrencyCode = request.getParameter("to");
        BigDecimal amount = BigDecimal.valueOf(Double.parseDouble(request.getParameter("amount")));
        parametersValidity.validateCurrencies(baseCurrencyCode, targetCurrencyCode);
        parametersValidity.validateAmount(amount);
        try {
            Optional<ExchangeRate> exchangeRateOptional = exchangeConnection.findByCodes(baseCurrencyCode, targetCurrencyCode);
            Optional<ExchangeRate> reversedExchangeRateOptional = exchangeConnection.findByCodes(targetCurrencyCode, baseCurrencyCode);
            if (exchangeRateOptional.isEmpty() && reversedExchangeRateOptional.isEmpty()) {
                processUsdBaseExchange(response, baseCurrencyCode, targetCurrencyCode, amount);
            } else {
                BigDecimal rate = BigDecimal.ZERO;
                if (exchangeRateOptional.isPresent()) {
                    rate = exchangeRateOptional.get().getRate();
                }
                if (reversedExchangeRateOptional.isPresent()){
                    rate = BigDecimal.ONE.divide(reversedExchangeRateOptional.get().getRate(), MathContext.DECIMAL128);
                }
                processSuccessfulExchange(response, baseCurrencyCode, targetCurrencyCode, amount, rate);
            }
        } catch (SQLException e) {
            throw new DatabaseException("There is a problem with database.");
        }
    }

    private void processUsdBaseExchange(HttpServletResponse response, String baseCurrencyCode, String targetCurrencyCode, BigDecimal amount) throws IOException, SQLException {
        BigDecimal usdExchangeRate = exchangeConnection.findRateByUsdBase(baseCurrencyCode, targetCurrencyCode);
        if (!usdExchangeRate.equals(BigDecimal.ZERO)) {
            processSuccessfulExchange(response, baseCurrencyCode, targetCurrencyCode, amount, usdExchangeRate);
        } else {
            throw new NotFoundException("There is no such exchange rate for these currencies.");
        }
    }

    private void processSuccessfulExchange(HttpServletResponse response, String baseCurrency, String targetCurrency, BigDecimal rate, BigDecimal amount) throws IOException, SQLException {
        BigDecimal convertedAmount = amount.multiply(rate, MathContext.DECIMAL128);
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
