package org.example.myexpensetracker.dao;

import java.util.List;
import java.util.UUID;

public interface EntityDAO<T> {
    List<T> findAll(UUID ownerId);
    boolean add(T item);
    boolean update(T item);
    boolean delete(UUID id, UUID ownerId);
}
