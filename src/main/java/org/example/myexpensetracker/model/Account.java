package org.example.myexpensetracker.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Account implements TreeStructure {
    private UUID id;
    private UUID ownerId;
    private UUID parentId;
    private boolean active;
    private String name;
    private Type type;

    public static Account create(UUID ownerId, String name, Type type, UUID parentId) {
        return new Account(UUID.randomUUID(), ownerId, parentId, true, name, type);
    }

    public enum Type {
        CASH, DEBT, BANK, CRYPTO
    }
}
