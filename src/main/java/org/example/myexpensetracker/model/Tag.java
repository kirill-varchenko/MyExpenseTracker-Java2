package org.example.myexpensetracker.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Tag {
    private UUID id;
    private UUID ownerId;
    private boolean active;
    private String name;

    public static Tag create(UUID ownerId, String name) {
        return new Tag(UUID.randomUUID(), ownerId, true, name);
    }
}
