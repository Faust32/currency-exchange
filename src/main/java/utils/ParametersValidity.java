package utils;

import exceptions.InvalidInputException;
import model.Currency;

import java.math.BigDecimal;
import java.util.regex.Pattern;

public class ParametersValidity {
    private static final Pattern CURRENCY_CODE_PATTERN = Pattern.compile("^[A-Za-z]{3}$");
    private static final Pattern CURRENCY_FULLNAME_PATTERN = Pattern.compile("^[A-Za-z\\s()]+$");
        private static final Pattern CURRENCY_SIGN_PATTERN = Pattern.compile("^[\\p{Sc}A-Za-z]+$");

    public void validateCurrency(Currency currency) {
        validateFullName(currency.getName());
        validateCode(currency.getCode());
        validateSymbol(currency.getSign());
    }

    public void validateCurrencies(String baseCode, String targetCode) {
        validateCode(baseCode);
        validateCode(targetCode);
        if (baseCode.equals(targetCode)) {
            throw new InvalidInputException("You cannot add an exchange rate for the same currency.");
        }
    }

    private void validateFullName(String fullName) {
        if (fullName == null || fullName.isEmpty() || !CURRENCY_FULLNAME_PATTERN.matcher(fullName).matches()) {
            throw new InvalidInputException("Invalid full name.");
        }
    }

    public void validateCode(String code) {
        if (code == null || code.isEmpty() || !CURRENCY_CODE_PATTERN.matcher(code).matches()) {
            throw new InvalidInputException("Invalid code.");
        }
    }

    private void validateSymbol(String sign) {
        if (sign == null || sign.isEmpty() || !CURRENCY_SIGN_PATTERN.matcher(sign).matches()) {
            throw new InvalidInputException("Invalid sign.");
        }
    }

    private void validatePositive(BigDecimal value, String fieldName) {
        if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidInputException("Invalid " + fieldName + ".");
        }
    }

    public void validateRate(BigDecimal rate) {
        validatePositive(rate, "rate");
    }

    public void validateAmount(BigDecimal amount) {
        validatePositive(amount, "amount");
    }

}
