package org.example.myexpensetracker.dao.sql;

import org.example.myexpensetracker.model.Currency;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class CurrencySQLDAO extends EntitySQLDAO<Currency> {
    private static final String FIND_ALL_SQL = "SELECT * FROM \"currency\" WHERE \"user_id\" = ? ORDER BY \"order\"";
    private static final String ADD_SQL = "INSERT INTO \"currency\" (\"id\", \"user_id\", \"is_active\", \"name\", \"code\", \"symbol\", \"order\") VALUES (?, ?, ?, ?, ?, ?, ?)";
    private static final String UPDATE_SQL = "UPDATE \"currency\" SET \"is_active\" = ?, \"name\" = ?, \"code\" = ?, \"symbol\" = ?, \"order\" = ? WHERE \"id\" = ? AND \"user_id\" = ?";
    private static final String DELETE_SQL = "DELETE FROM \"currency\" WHERE \"id\" = ? AND \"user_id\" = ?";


    @Override
    protected Currency parseResultSet(ResultSet rs) throws SQLException {
        Currency currency = new Currency();
        currency.setId(rs.getObject(1, UUID.class));
        currency.setOwnerId(rs.getObject(2, UUID.class));
        currency.setActive(rs.getBoolean(3));
        currency.setName(rs.getString(4));
        currency.setCode(rs.getString(5));
        currency.setSymbol(rs.getString(6));
        currency.setOrder(rs.getInt(7));
        return currency;
    }

    protected PreparedStatement prepareFindAllStatement(UUID ownerId) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement(FIND_ALL_SQL);
        stmt.setObject(1, ownerId);
        return stmt;
    }

    protected PreparedStatement prepareAddStatement(Currency currency) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement(ADD_SQL);
        stmt.setObject(1, currency.getId());
        stmt.setObject(2, currency.getOwnerId());
        stmt.setBoolean(3, currency.isActive());
        stmt.setString(4, currency.getName());
        stmt.setString(5, currency.getCode());
        stmt.setString(6, currency.getSymbol());
        stmt.setInt(7, currency.getOrder());
        return stmt;
    }

    protected PreparedStatement prepareUpdateStatement(Currency currency) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement(UPDATE_SQL);
        stmt.setBoolean(1, currency.isActive());
        stmt.setString(2, currency.getName());
        stmt.setString(3, currency.getCode());
        stmt.setString(4, currency.getSymbol());
        stmt.setInt(5, currency.getOrder());
        stmt.setObject(6, currency.getId());
        stmt.setObject(7, currency.getOwnerId());
        return stmt;
    }

    protected PreparedStatement prepareDeleteStatement(UUID id, UUID ownerId) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement(DELETE_SQL);
        stmt.setObject(1, id);
        stmt.setObject(2, ownerId);
        return stmt;
    }
}
