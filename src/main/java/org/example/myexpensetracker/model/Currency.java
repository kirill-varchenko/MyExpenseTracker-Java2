package org.example.myexpensetracker.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Currency {
    private UUID id;
    private UUID ownerId;
    private boolean active;
    private String name;
    private String code;
    private String symbol;
    private int order;

    public static Currency create(UUID ownerId, String name, String code, String symbol) {
        return new Currency(UUID.randomUUID(), ownerId, true, name, code, symbol, -1);
    }
}
