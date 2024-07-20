package org.example.myexpensetracker.db.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record AccountTotalDTO(UUID accountId, BigDecimal amount, UUID currencyId) {

}
