package org.example.myexpensetracker.dao;

import org.example.myexpensetracker.model.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserDAO {
    List<User> findAll();
    Optional<User> getById(UUID id);
    Optional<User> getByUsername(String username);
    boolean add(User user);
    boolean update(User user);
    boolean delete(UUID id);
}
