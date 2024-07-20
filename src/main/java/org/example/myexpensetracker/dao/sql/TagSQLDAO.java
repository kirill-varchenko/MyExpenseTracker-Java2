package org.example.myexpensetracker.dao.sql;

import org.example.myexpensetracker.model.Tag;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class TagSQLDAO extends EntitySQLDAO<Tag> {
    private static final String FIND_ALL_SQL = "SELECT * FROM \"tag\" WHERE \"user_id\" = ?";
    private static final String ADD_SQL = "INSERT INTO \"tag\" (\"id\", \"user_id\", \"is_active\", \"name\") VALUES (?, ?, ?, ?)";
    private static final String UPDATE_SQL = "UPDATE \"tag\" SET \"is_active\" = ?, \"name\" = ? WHERE \"id\" = ? AND \"user_id\" = ?";
    private static final String DELETE_SQL = "DELETE FROM \"tag\" WHERE \"id\" = ? AND \"user_id\" = ?";


    @Override
    protected Tag parseResultSet(ResultSet rs) throws SQLException {
        Tag tag = new Tag();
        tag.setId(rs.getObject(1, UUID.class));
        tag.setOwnerId(rs.getObject(2, UUID.class));
        tag.setActive(rs.getBoolean(3));
        tag.setName(rs.getString(4));
        return tag;
    }

    protected PreparedStatement prepareFindAllStatement(UUID ownerId) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement(FIND_ALL_SQL);
        stmt.setObject(1, ownerId);
        return stmt;
    }

    protected PreparedStatement prepareAddStatement(Tag tag) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement(ADD_SQL);
        stmt.setObject(1, tag.getId());
        stmt.setObject(2, tag.getOwnerId());
        stmt.setBoolean(3, tag.isActive());
        stmt.setString(4, tag.getName());
        return stmt;
    }

    protected PreparedStatement prepareUpdateStatement(Tag tag) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement(UPDATE_SQL);
        stmt.setBoolean(1, tag.isActive());
        stmt.setString(2, tag.getName());
        stmt.setObject(3, tag.getId());
        stmt.setObject(4, tag.getOwnerId());
        return stmt;
    }

    protected PreparedStatement prepareDeleteStatement(UUID id, UUID ownerId) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement(DELETE_SQL);
        stmt.setObject(1, id);
        stmt.setObject(2, ownerId);
        return stmt;
    }
}
