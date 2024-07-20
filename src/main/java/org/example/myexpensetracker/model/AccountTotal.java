package org.example.myexpensetracker.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountTotal implements TreeStructure {
    private Account account;
    private Map<Currency, BigDecimal> totals;

    @Override
    public UUID getId() {
        return account.getId();
    }

    @Override
    public UUID getParentId() {
        return account.getParentId();
    }
}
