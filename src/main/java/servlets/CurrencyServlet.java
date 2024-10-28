package servlets;

import com.fasterxml.jackson.databind.ObjectMapper;
import model.Currency;
import model.response.ErrorResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import service.JdbcCurrencyConnection;
import service.RequestHandler;

import java.io.IOException;
import java.sql.SQLException;

import static jakarta.servlet.http.HttpServletResponse.*;

@WebServlet("/currency/*")
public class CurrencyServlet extends HttpServlet {
    private final RequestHandler requestHandler = new RequestHandler();
    private final JdbcCurrencyConnection currencyConnection = new JdbcCurrencyConnection();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public CurrencyServlet() throws SQLException {
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String requestURI = request.getRequestURI();
        String code = requestHandler.trim(requestURI);
        if (requestHandler.isRequestValid(code)) {
            response.setStatus(SC_BAD_REQUEST);
            ErrorResponse errorResponse = new ErrorResponse(SC_BAD_REQUEST,
                    "You have not specified the currency in the address or made it incorrectly. " +
                            "Please repeat your request.");
            objectMapper.writeValue(response.getWriter(), errorResponse);
            return;
        }
        try {
            Currency currency = currencyConnection.findByCode(code);
            if (currency == null) {
                response.setStatus(SC_NOT_FOUND);
                ErrorResponse errorResponse = new ErrorResponse(SC_NOT_FOUND,
                        "There is no such currency with this code in database. Please, try again.");
                objectMapper.writeValue(response.getWriter(), errorResponse);
                return;
            }
            objectMapper.writeValue(response.getWriter(), currency);
        } catch (SQLException e) {
            response.setStatus(SC_INTERNAL_SERVER_ERROR);
            ErrorResponse errorResponse = new ErrorResponse(SC_INTERNAL_SERVER_ERROR,
                    "There is a problem with database. Please, try again later.");
            objectMapper.writeValue(response.getWriter(), errorResponse);
        }
    }
}
