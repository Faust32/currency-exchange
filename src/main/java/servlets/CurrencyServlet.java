package servlets;

import exceptions.DatabaseException;
import model.Currency;
import dao.JdbcCurrencyConnection;
import utils.ParametersValidity;
import utils.RequestHandler;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/currency/*")
public class CurrencyServlet extends HttpServlet {
    private final RequestHandler requestHandler = new RequestHandler();
    private final ParametersValidity parametersValidity = new ParametersValidity();
    private final JdbcCurrencyConnection currencyConnection = new JdbcCurrencyConnection();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public CurrencyServlet() throws SQLException {
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String requestURI = request.getRequestURI();
        String code = requestHandler.trim(requestURI);
        parametersValidity.validateCode(code);
        try {
            Currency currency = currencyConnection.findByCode(code);
            parametersValidity.validateCurrency(currency);
            objectMapper.writeValue(response.getWriter(), currency);
        } catch (SQLException e) {
            throw new DatabaseException("There is a problem with database.");
        }
    }
}
