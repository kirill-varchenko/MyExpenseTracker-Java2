package org.example.myexpensetracker.model;

import java.time.LocalDate;
import java.util.*;

public final class Expense extends Record {
    private List<Entry> entries;

    public Expense(UUID id, UUID ownerId, LocalDate date, String comment, List<Entry> entries) {
        super(id, ownerId, date, Record.Type.EXPENSE, comment);
        this.entries = entries;
    }

    public static Expense create(UUID ownerId, LocalDate date, String comment) {
        return new Expense(UUID.randomUUID(), ownerId, date, comment, new ArrayList<>());
    }

    public void addEntry(Entry entry) {
        if (!entry.isAmountPositive()) {
            throw new IllegalArgumentException("Amount must be > 0");
        }
        entries.add(entry);
    }

    public List<Entry> getEntries() {
        return entries;
    }

    @Override
    public String getDetails() {
        Map<Currency, Amount> totals = new HashMap<>();
        for (Entry entry : entries) {
            Currency currency = entry.getAmount().getCurrency();
            if (!totals.containsKey(currency)) {
                totals.put(currency, new Amount(currency));
            }
            Amount newAmount = totals.get(currency).add(entry.getAmount());
            totals.put(currency, newAmount);
        }

        return String.join(", ",
                totals.values().stream().map(Amount::toString).toList());
    }

    @Override
    public String toString() {
        return String.format("Expense[id=%s, date=%s, entries=%s]", getId(), getDate(), entries);
    }
}
