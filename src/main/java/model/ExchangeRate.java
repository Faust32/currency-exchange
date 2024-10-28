package model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"id", "baseCurrencyID", "targetCurrencyID", "rate"})
public class ExchangeRate {
    private int ID;
    private final Currency baseCurrency;
    private final Currency targetCurrency;
    private double rate;

    public ExchangeRate(int ID, Currency baseCurrency, Currency targetCurrency, double rate) {
        this.ID = ID;
        this.baseCurrency = baseCurrency;
        this.targetCurrency = targetCurrency;
        this.rate = rate;
    }

    public int getID() {
        return ID;
    }

    public Currency getBaseCurrency() {
        return baseCurrency;
    }

    public Currency getTargetCurrency() {
        return targetCurrency;
    }

    public double getRate() {
        return rate;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }

}
