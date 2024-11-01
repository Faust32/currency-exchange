package servlets;

import exceptions.DatabaseException;
import exceptions.NotFoundException;
import model.ExchangeRate;
import dao.JdbcExchangeConnection;
import utils.ParametersValidity;
import utils.RequestHandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Optional;


@WebServlet("/exchangeRate/*")
public class ExchangeRateServlet extends HttpServlet {
    private final JdbcExchangeConnection exchangeConnection = new JdbcExchangeConnection();
    private final RequestHandler requestHandler = new RequestHandler();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ParametersValidity parametersValidity = new ParametersValidity();

    public ExchangeRateServlet() throws SQLException {
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (request.getMethod().equalsIgnoreCase("PATCH")) {
            doPatch(request, response);
        }
        else {
            super.service(request, response);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String[] temp = getCodes(request);
        String baseCurrencyCode = temp[0];
        String targetCurrencyCode = temp[1];
        try {
            Optional<ExchangeRate> exchangeRateOptional = exchangeConnection.findByCodes(baseCurrencyCode, targetCurrencyCode);
            if (exchangeRateOptional.isPresent()) {
                objectMapper.writeValue(response.getWriter(), exchangeRateOptional.get());
            }
            else {
                throw new NotFoundException("There is no such exchange rate for these currencies.");
            }
        }
        catch (SQLException e) {
            throw new DatabaseException("There is a problem with database.");
        }
    }

    protected void doPatch(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String[] temp = getCodes(request);
        String baseCurrencyCode = temp[0];
        String targetCurrencyCode = temp[1];
        String parameter = request.getReader().readLine();
        String stringRate = parameter.replace("rate=", "");
        if (stringRate.isEmpty()) {
            throw new NotFoundException("There is no rate parameter in the request.");
        }
        BigDecimal rate = BigDecimal.valueOf(Double.parseDouble(stringRate));
        parametersValidity.validateRate(rate);
        try {
            Optional<ExchangeRate> exchangeRateOptional = exchangeConnection.findByCodes(baseCurrencyCode, targetCurrencyCode);
            if (exchangeRateOptional.isPresent()) {
                ExchangeRate exchangeRate = exchangeRateOptional.get();
                exchangeRate.setRate(rate);
                exchangeConnection.update(exchangeRate);
                objectMapper.writeValue(response.getWriter(), exchangeRate);
            } else {
                throw new NotFoundException("There is no such exchange rate for these currencies.");
            }
        } catch (SQLException e) {
            throw new DatabaseException("There is a problem with database.");
        }
    }

    private String[] getCodes(HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        String code = requestHandler.trim(requestURI);
        int mid = code.length() / 2;
        String[] temp = new String[2];
        temp[0] = code.substring(0, mid);
        temp[1] = code.substring(mid);
        parametersValidity.validateCurrencies(temp[0], temp[1]);
        return temp;
    }
}
