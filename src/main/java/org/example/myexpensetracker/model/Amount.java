package org.example.myexpensetracker.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class Amount {
    @NonNull
    private BigDecimal value;
    @NonNull
    private Currency currency;

    public Amount(Currency currency) {
        value = BigDecimal.ZERO;
        this.currency = currency;
    }

    public Amount negate() {
        return new Amount(value.negate(), currency);
    }

    public boolean isPositive() {
        return value.compareTo(BigDecimal.ZERO) > 0;
    }

    public Amount add(Amount other) {
        if (other.getCurrency() != currency) {
            throw new IllegalArgumentException("Can add only amounts with same currency");
        }
        return new Amount(value.add(other.getValue()), currency);
    }

    @Override
    public String toString() {
        return String.format("%s %s", value, currency.getSymbol());
    }
}
