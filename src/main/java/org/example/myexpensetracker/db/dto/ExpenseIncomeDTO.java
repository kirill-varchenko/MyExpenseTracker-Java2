package org.example.myexpensetracker.db.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record ExpenseIncomeDTO(BigDecimal amount, UUID currencyId, String type, int year, int month) {
}
