package org.example.myexpensetracker.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Category implements TreeStructure {
    private UUID id;
    private UUID ownerId;
    private boolean active;
    private UUID parentId;
    private String name;

    public static Category create(UUID ownerId, String name, UUID parentId) {
        return new Category(UUID.randomUUID(), ownerId, true, parentId, name);
    }
}
