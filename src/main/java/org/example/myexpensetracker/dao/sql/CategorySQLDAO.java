package org.example.myexpensetracker.dao.sql;

import org.example.myexpensetracker.model.Category;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class CategorySQLDAO extends EntitySQLDAO<Category> {
    private static final String FIND_ALL_SQL = "SELECT * FROM \"category\" WHERE \"user_id\" = ?";
    private static final String ADD_SQL = "INSERT INTO \"category\" (\"id\", \"user_id\", \"is_active\", \"name\", \"parent_id\") VALUES (?, ?, ?, ?, ?)";
    private static final String UPDATE_SQL = "UPDATE \"category\" SET \"is_active\" = ?, \"name\" = ?, \"parent_id\" = ? WHERE \"id\" = ? AND \"user_id\" = ?";
    private static final String DELETE_SQL = "DELETE FROM \"category\" WHERE \"id\" = ? AND \"user_id\" = ?";

    @Override
    protected Category parseResultSet(ResultSet rs) throws SQLException {
        Category category = new Category();
        category.setId(rs.getObject(1, UUID.class));
        category.setOwnerId(rs.getObject(2, UUID.class));
        category.setActive(rs.getBoolean(3));
        category.setParentId(rs.getObject(4, UUID.class));
        category.setName(rs.getString(5));
        return category;
    }

    protected PreparedStatement prepareFindAllStatement(UUID ownerId) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement(FIND_ALL_SQL);
        stmt.setObject(1, ownerId);
        return stmt;
    }

    protected PreparedStatement prepareAddStatement(Category category) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement(ADD_SQL);
        stmt.setObject(1, category.getId());
        stmt.setObject(2, category.getOwnerId());
        stmt.setBoolean(3, category.isActive());
        stmt.setString(4, category.getName());
        stmt.setObject(5, category.getParentId());
        return stmt;
    }

    protected PreparedStatement prepareUpdateStatement(Category category) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement(UPDATE_SQL);
        stmt.setBoolean(1, category.isActive());
        stmt.setString(2, category.getName());
        stmt.setObject(3, category.getParentId());
        stmt.setObject(4, category.getId());
        stmt.setObject(5, category.getOwnerId());
        return stmt;
    }

    protected PreparedStatement prepareDeleteStatement(UUID id, UUID ownerId) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement(DELETE_SQL);
        stmt.setObject(1, id);
        stmt.setObject(2, ownerId);
        return stmt;
    }
}
