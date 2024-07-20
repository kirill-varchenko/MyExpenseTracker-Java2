package org.example.myexpensetracker.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Data
@AllArgsConstructor
public class ExchangeRate {
    private Currency fromCurrency;
    private Currency toCurrency;
    private BigDecimal rate;

    public ExchangeRate invert() {
        return new ExchangeRate(toCurrency, fromCurrency, BigDecimal.ONE.divide(rate, 6, RoundingMode.HALF_DOWN));
    }
}
