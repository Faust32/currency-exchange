package model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
@JsonPropertyOrder({"id", "baseCurrencyID", "targetCurrencyID", "rate"})
public class ExchangeRate {
    @Setter
    private int ID;
    private final Currency baseCurrency;
    private final Currency targetCurrency;
    @Setter
    private BigDecimal rate;
}

