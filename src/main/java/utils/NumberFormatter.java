package utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class NumberFormatter {
    public BigDecimal roundToTwoDecimalPlaces(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}

