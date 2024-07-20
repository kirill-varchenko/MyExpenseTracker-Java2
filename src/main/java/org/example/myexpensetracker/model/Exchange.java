package org.example.myexpensetracker.model;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public final class Exchange extends Record {
    private Account account;
    private Amount fromAmount;
    private Amount toAmount;

    public Exchange(UUID id, UUID ownerId, LocalDate date, Account account, Amount fromAmount, Amount toAmount, String comment) {
        super(id, ownerId, date, Record.Type.EXCHANGE, comment);
        this.account = account;
        this.fromAmount = fromAmount;
        this.toAmount = toAmount;
    }

    public static Exchange create(UUID ownerId, LocalDate date, Account account, Amount fromAmount, Amount toAmount, String comment) {
        return new Exchange(UUID.randomUUID(), ownerId, date, account, fromAmount, toAmount, comment);
    }

    public BigDecimal getRate() {
        return toAmount.getValue().divide(fromAmount.getValue(), RoundingMode.HALF_DOWN);
    }

    @Override
    public String getDetails() {
        return String.format("%s -> %s (%s)", fromAmount, toAmount, getRate());
    }

    @Override
    public String toString() {
        return String.format("Exchange[id=%s, date=%s, account=%s, fromAmount=%s, toAmount=%s]",
                getId(), getDate(), account, fromAmount, toAmount);
    }
}
