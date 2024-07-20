package org.example.myexpensetracker.dao.sql;

import org.example.myexpensetracker.model.Account;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class AccountSQLDAO extends EntitySQLDAO<Account> {
    private static final String FIND_ALL_SQL = "SELECT * FROM \"account\" WHERE \"user_id\" = ?";
    private static final String ADD_SQL = "INSERT INTO \"account\" (\"id\", \"user_id\", \"is_active\", \"name\", \"parent_id\", \"type\") VALUES (?, ?, ?, ?, ?, ?)";
    private static final String UPDATE_SQL = "UPDATE \"account\" SET \"is_active\" = ?, \"name\" = ?, \"parent_id\" = ?, \"type\" = ? WHERE \"id\" = ? AND \"user_id\" = ?";
    private static final String DELETE_SQL = "DELETE FROM \"account\" WHERE \"id\" = ? AND \"user_id\" = ?";

    @Override
    protected Account parseResultSet(ResultSet rs) throws SQLException {
        Account account = new Account();
        account.setId(rs.getObject(1, UUID.class));
        account.setOwnerId(rs.getObject(2, UUID.class));
        account.setActive(rs.getBoolean(3));
        account.setName(rs.getString(4));
        account.setParentId(rs.getObject(5, UUID.class));
        account.setType(Account.Type.valueOf(rs.getString(6)));
        return account;
    }

    protected PreparedStatement prepareFindAllStatement(UUID ownerId) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement(FIND_ALL_SQL);
        stmt.setObject(1, ownerId);
        return stmt;
    }

    protected PreparedStatement prepareAddStatement(Account account) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement(ADD_SQL);
        stmt.setObject(1, account.getId());
        stmt.setObject(2, account.getOwnerId());
        stmt.setBoolean(3, account.isActive());
        stmt.setString(4, account.getName());
        stmt.setObject(5, account.getParentId());
        stmt.setObject(6, account.getType().name());
        return stmt;
    }

    protected PreparedStatement prepareUpdateStatement(Account account) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement(UPDATE_SQL);
        stmt.setBoolean(1, account.isActive());
        stmt.setString(2, account.getName());
        stmt.setObject(3, account.getParentId());
        stmt.setObject(4, account.getType().name());
        stmt.setObject(5, account.getId());
        stmt.setObject(6, account.getOwnerId());
        return stmt;
    }

    protected PreparedStatement prepareDeleteStatement(UUID id, UUID ownerId) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement(DELETE_SQL);
        stmt.setObject(1, id);
        stmt.setObject(2, ownerId);
        return stmt;
    }

}
