package org.example.myexpensetracker.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class MonthlyAmount {
    private BigDecimal amount;
    private Currency currency;
    private String month;
    private Record.Type type;
}
