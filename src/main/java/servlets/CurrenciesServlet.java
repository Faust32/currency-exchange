package servlets;

import com.fasterxml.jackson.databind.ObjectMapper;
import model.Currency;
import model.response.ErrorResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import service.JdbcCurrencyConnection;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import static jakarta.servlet.http.HttpServletResponse.*;

@WebServlet("/currencies")
public class CurrenciesServlet extends HttpServlet {
    private final JdbcCurrencyConnection currencyConnection = new JdbcCurrencyConnection();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public CurrenciesServlet() throws SQLException {
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            List<Currency> currencies = currencyConnection.findAll();
            objectMapper.writeValue(response.getWriter(), currencies);
        }
        catch (SQLException e) {
            response.setStatus(SC_INTERNAL_SERVER_ERROR);
            ErrorResponse errorResponse = new ErrorResponse(SC_INTERNAL_SERVER_ERROR,
                    "There is a problem with database. Please, try again later.");
            objectMapper.writeValue(response.getWriter(), errorResponse);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String fullName = request.getParameter("name");
        String code = request.getParameter("code");
        String sign = request.getParameter("sign");
        if (fullName == null || fullName.isBlank()) {
            response.setStatus(SC_BAD_REQUEST);
            ErrorResponse errorResponse = new ErrorResponse(SC_BAD_REQUEST,
                    "A Name parameter is missing in your request. Please repeat your request.");
            objectMapper.writeValue(response.getWriter(), errorResponse);
            return;
        }
        if (code == null || code.isBlank()) {
            response.setStatus(SC_BAD_REQUEST);
            ErrorResponse errorResponse = new ErrorResponse(SC_BAD_REQUEST,
                    "A Code parameter is missing in your request. Please repeat your request.");
            objectMapper.writeValue(response.getWriter(), errorResponse);
            return;
        }
        if (sign == null || sign.isBlank()) {
            response.setStatus(SC_BAD_REQUEST);
            ErrorResponse errorResponse = new ErrorResponse(SC_BAD_REQUEST,
                    "A Sign parameter is missing in your request. Please repeat your request.");
            objectMapper.writeValue(response.getWriter(), errorResponse);
            return;
        }
        Currency currency = new Currency(0, fullName, code, sign);
        try {
            int newID = currencyConnection.create(currency);
            currency.setID(newID);
            objectMapper.writeValue(response.getWriter(), currency);
        } catch (SQLException e) {
            if (e.getErrorCode() == SC_CONFLICT) {
                response.setStatus(SC_CONFLICT);
                ErrorResponse errorResponse = new ErrorResponse(SC_CONFLICT,
                        "A currency with this code already exists. Please try again.");
                objectMapper.writeValue(response.getWriter(), errorResponse);
                return;
            }
            response.setStatus(SC_INTERNAL_SERVER_ERROR);
            ErrorResponse errorResponse = new ErrorResponse(SC_INTERNAL_SERVER_ERROR,
                    "There is a problem with database. Please try again later.");
            objectMapper.writeValue(response.getWriter(), errorResponse);
        }
    }
}
