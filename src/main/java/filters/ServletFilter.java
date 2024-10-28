package filters;

import exceptions.AlreadyExistsException;
import exceptions.DatabaseException;
import exceptions.InvalidInputException;
import exceptions.NotFoundException;
import model.response.ErrorResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebFilter(value = {"/currencies", "/currency/*", "/exchangeRates", "/exchangeRate/*", "/exchange"})
public class ServletFilter implements Filter {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Filter.super.init(filterConfig);
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;
        httpServletResponse.setContentType("application/json");
        httpServletResponse.setCharacterEncoding("UTF-8");
        try {
            filterChain.doFilter(servletRequest, servletResponse);
        }
        catch (DatabaseException message) {
            handle(httpServletResponse, message.getMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        catch (AlreadyExistsException message) {
            handle(httpServletResponse, message.getMessage(), HttpServletResponse.SC_CONFLICT);
        }
        catch (NotFoundException message) {
            handle(httpServletResponse, message.getMessage(), HttpServletResponse.SC_NOT_FOUND);
        }
        catch (InvalidInputException message) {
            handle(httpServletResponse, message.getMessage(), HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    private void handle(HttpServletResponse response, String message, int statusCode) throws IOException {
        response.setStatus(statusCode);
        ErrorResponse errorResponse = new ErrorResponse(statusCode, message);
        objectMapper.writeValue(response.getOutputStream(), errorResponse);
    }

    @Override
    public void destroy() {
        Filter.super.destroy();
    }
}
