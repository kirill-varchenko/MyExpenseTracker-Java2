package org.example.myexpensetracker.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
    private UUID id;
    private String username;
    private Profile profile = new Profile();

    public static User create(String username) {
        return new User(UUID.randomUUID(), username, new Profile());
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Profile {
        private UUID baseCurrencyId;
        private UUID defaultCurrencyId;
        private UUID defaultAccountId;
    }
}
