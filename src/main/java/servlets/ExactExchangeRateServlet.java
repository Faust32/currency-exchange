package servlets;

import com.fasterxml.jackson.databind.ObjectMapper;
import model.ExchangeRate;
import model.response.ErrorResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import service.JdbcExchangeConnection;
import service.RequestHandler;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

import static jakarta.servlet.http.HttpServletResponse.*;

@WebServlet("/exchangeRate/*")
public class ExactExchangeRateServlet extends HttpServlet{
    private final JdbcExchangeConnection exchangeConnection = new JdbcExchangeConnection();
    private final RequestHandler requestHandler = new RequestHandler();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ExactExchangeRateServlet() throws SQLException {
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
        String[] temp = getCodes(request, response);
        String baseCurrencyCode = temp[0];
        String targetCurrencyCode = temp[1];
        try {
            Optional<ExchangeRate> exchangeRateOptional = exchangeConnection.findByCodes(baseCurrencyCode, targetCurrencyCode);
            if (exchangeRateOptional.isPresent()) {
                objectMapper.writeValue(response.getWriter(), exchangeRateOptional.get());
            }
            else {
                response.setStatus(SC_NOT_FOUND);
                ErrorResponse errorResponse = new ErrorResponse(SC_NOT_FOUND,
                        "There is no such exchange rate for these currencies. Please try again later.");
                objectMapper.writeValue(response.getWriter(), errorResponse);
            }
        }
        catch (SQLException e) {
            response.setStatus(SC_INTERNAL_SERVER_ERROR);
            ErrorResponse errorResponse = new ErrorResponse(SC_INTERNAL_SERVER_ERROR,
                    "There is a problem with database. Please try again later.");
            objectMapper.writeValue(response.getWriter(), errorResponse);
        }
    }

    protected void doPatch(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String[] temp = getCodes(request, response);
        String baseCurrencyCode = temp[0];
        String targetCurrencyCode = temp[1];
        String stringRate = request.getParameter("rate");
        if (stringRate == null) {
            response.setStatus(SC_BAD_REQUEST);
            ErrorResponse errorResponse = new ErrorResponse(SC_BAD_REQUEST,
                    "There is no rate parameter in the request. Please try again later.");
            objectMapper.writeValue(response.getWriter(), errorResponse);
            return;
        }
        double rate = Double.parseDouble(stringRate);
        try {
            Optional<ExchangeRate> exchangeRateOptional = exchangeConnection.findByCodes(baseCurrencyCode, targetCurrencyCode);
            if (exchangeRateOptional.isPresent()) {
                ExchangeRate exchangeRate = exchangeRateOptional.get();
                exchangeRate.setRate(rate);
                exchangeConnection.update(exchangeRate);
                objectMapper.writeValue(response.getWriter(), exchangeRate);
            } else {
                response.setStatus(SC_NOT_FOUND);
                ErrorResponse errorResponse = new ErrorResponse(SC_NOT_FOUND,
                        "There is no such exchange rate for these currencies. Please try again later.");
                objectMapper.writeValue(response.getWriter(), errorResponse);
            }
        } catch (SQLException e) {
            response.setStatus(SC_INTERNAL_SERVER_ERROR);
            ErrorResponse errorResponse = new ErrorResponse(SC_INTERNAL_SERVER_ERROR,
                    "There is a problem with database. Please try again later.");
            objectMapper.writeValue(response.getWriter(), errorResponse);
        }
    }

    private String[] getCodes(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String requestURI = request.getRequestURI();
        String code = requestHandler.trim(requestURI);
        if (requestHandler.isRequestValid(code)) {
            response.setStatus(SC_BAD_REQUEST);
            ErrorResponse errorResponse = new ErrorResponse(SC_BAD_REQUEST,
                    "You most likely entered the currency codes incorrectly. Please try again later.");
            objectMapper.writeValue(response.getWriter(), errorResponse);
        }
        int mid = code.length() / 2;
        String[] temp = new String[2];
        temp[0] = code.substring(0, mid);
        temp[1] = code.substring(mid);
        return temp;
    }
}
