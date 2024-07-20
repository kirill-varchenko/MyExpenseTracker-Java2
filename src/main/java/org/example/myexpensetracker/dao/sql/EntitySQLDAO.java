package org.example.myexpensetracker.dao.sql;

import org.example.myexpensetracker.dao.EntityDAO;
import org.example.myexpensetracker.db.DBConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class EntitySQLDAO<T> implements EntityDAO<T> {
    protected static final Logger logger = LoggerFactory.getLogger(EntitySQLDAO.class);
    protected final Connection conn = DBConnection.getInstance().connection();

    @Override
    public List<T> findAll(UUID ownerId) {
        List<T> items = new ArrayList<>();

        try (PreparedStatement statement = prepareFindAllStatement(ownerId);
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                items.add(parseResultSet(rs));
            }
        } catch (SQLException e) {
            logger.warn("Error while reading accounts: {}", e.toString());
        }

        return items;
    }

    @Override
    public boolean add(T item) {
        logger.debug("Adding: {}", item);
        try (PreparedStatement statement = prepareAddStatement(item)) {
            return statement.executeUpdate() == 1;
        } catch (SQLException e) {
            logger.error("Error while adding: {}", e.toString());
        }
        return false;
    }

    @Override
    public boolean update(T item) {
        logger.debug("Updating: {}", item);
        try (PreparedStatement statement = prepareUpdateStatement(item)) {
            return statement.executeUpdate() == 1;
        } catch (SQLException e) {
            logger.error("Error while updating: {}", e.toString());
        }
        return false;
    }

    @Override
    public boolean delete(UUID id, UUID ownerId) {
        logger.info("Deleting: {}", id);
        try (PreparedStatement statement = prepareDeleteStatement(id, ownerId)) {
            return statement.executeUpdate() == 1;
        } catch (SQLException e) {
            logger.warn("Error while deleting: {}", e.toString());
        }
        return false;
    }

    protected abstract T parseResultSet(ResultSet rs) throws SQLException;

    protected abstract PreparedStatement prepareFindAllStatement(UUID ownerId) throws SQLException;

    protected abstract PreparedStatement prepareAddStatement(T item) throws SQLException;

    protected abstract PreparedStatement prepareUpdateStatement(T item) throws SQLException;

    protected abstract PreparedStatement prepareDeleteStatement(UUID id, UUID ownerId) throws SQLException;
}
