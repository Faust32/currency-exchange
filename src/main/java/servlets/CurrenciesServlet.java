package servlets;

import exceptions.AlreadyExistsException;
import exceptions.DatabaseException;
import model.Currency;
import dao.JdbcCurrencyConnection;
import utils.ParametersValidity;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import static jakarta.servlet.http.HttpServletResponse.SC_CONFLICT;

@WebServlet("/currencies")
public class CurrenciesServlet extends HttpServlet {
    private final JdbcCurrencyConnection currencyConnection = new JdbcCurrencyConnection();
    private final ParametersValidity parametersValidity = new ParametersValidity();
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
            throw new DatabaseException("There is a problem with database.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String fullName = request.getParameter("name");
        String code = request.getParameter("code");
        String sign = request.getParameter("sign");
        Currency currency = new Currency(0, code, fullName, sign);
        parametersValidity.validateCurrency(currency);
        try {
            int newID = currencyConnection.create(currency);
            currency.setID(newID);
            objectMapper.writeValue(response.getWriter(), currency);
        } catch (SQLException e) {
            if (e.getErrorCode() == SC_CONFLICT) {
                throw new AlreadyExistsException("A currency with this code already exists.");
            }
            throw new DatabaseException("There is a problem with database.");
        }
    }
}
