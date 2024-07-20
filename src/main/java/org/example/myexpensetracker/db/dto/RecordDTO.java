package org.example.myexpensetracker.db.dto;

import org.example.myexpensetracker.model.Record;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public record RecordDTO(UUID id,
                        UUID ownerId,
                        LocalDate date,
                        String comment,
                        Record.Type type,
                        List<Entry> entries) {

    public record Entry(UUID accountId,
                        BigDecimal amount,
                        UUID currencyId,
                        UUID categoryId,
                        String comment,
                        Set<UUID> tagIds) {
    }

}
