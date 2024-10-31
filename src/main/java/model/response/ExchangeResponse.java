package model.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import model.Currency;

import java.math.BigDecimal;

@Setter
@Getter
@AllArgsConstructor
@JsonPropertyOrder({"baseCurrencyID", "targetCurrencyID", "rate", "amount", "convertedAmount"})
public class ExchangeResponse {
    private Currency baseCurrency;
    private Currency targetCurrency;
    private BigDecimal rate;
    private BigDecimal amount;
    private BigDecimal convertedAmount;

}
