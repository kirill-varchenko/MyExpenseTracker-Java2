package org.example.myexpensetracker.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public abstract sealed class Record permits  Expense, Income, Transfer, Exchange {
    @NonNull
    private UUID id;
    @NonNull
    private UUID ownerId;
    @NonNull
    private LocalDate date;
    @NonNull
    private Type type;
    private String comment;

    public enum Type {
        EXPENSE, INCOME, TRANSFER, EXCHANGE
    }

    public abstract String getDetails();
}
