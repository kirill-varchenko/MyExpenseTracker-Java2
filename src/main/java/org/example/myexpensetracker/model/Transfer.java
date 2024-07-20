package org.example.myexpensetracker.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public final class Transfer extends Record {
    private Account fromAccount;
    private Account toAccount;
    private Amount amount;

    public Transfer(UUID id, UUID ownerId, LocalDate date, Account fromAccount, Account toAccount, Amount amount, String comment) {
        super(id, ownerId, date, Type.TRANSFER, comment);
        this.fromAccount = fromAccount;
        this.toAccount = toAccount;
        this.amount = amount;
    }

    public static Transfer create(UUID ownerId, LocalDate date, Account fromAccount, Account toAccount, Amount amount, String comment) {
        return new Transfer(UUID.randomUUID(), ownerId, date, fromAccount, toAccount, amount, comment);
    }

    @Override
    public String getDetails() {
        return String.format("%s -> %s (%s)", fromAccount.getName(), toAccount.getName(), amount);
    }

    @Override
    public String toString() {
        return String.format("Transfer[id=%s, date=%s, fromAccount=%s, toAccount=%s, amount=%s]",
                getId(), getDate(), fromAccount, toAccount, amount);
    }
}
