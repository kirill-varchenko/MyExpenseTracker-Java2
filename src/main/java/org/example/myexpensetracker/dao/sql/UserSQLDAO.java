package org.example.myexpensetracker.dao.sql;

import org.example.myexpensetracker.dao.UserDAO;
import org.example.myexpensetracker.db.DBConnection;
import org.example.myexpensetracker.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class UserSQLDAO implements UserDAO {
    private static final Logger logger = LoggerFactory.getLogger(UserSQLDAO.class);
    private final Connection conn = DBConnection.getInstance().connection();

    private static final String FIND_ALL_SQL = "SELECT * FROM \"user\"";
    private static final String ADD_SQL = "INSERT INTO \"user\" (\"id\", \"username\") VALUES (?, ?); INSERT INTO \"profile\" (\"id\", \"base_currency_id\", \"default_currency_id\", \"default_account_id\") VALUES (?, ?, ?, ?);";
    private static final String GET_BY_ID_SQL = "SELECT u.\"id\", u.\"username\", p.\"base_currency_id\", p.\"default_currency_id\", p.\"default_account_id\" FROM \"user\" u LEFT JOIN \"profile\" p ON u.\"id\" = p.\"id\" WHERE u.\"id\" = ?";
    private static final String GET_BY_USERNAME_SQL = "SELECT u.\"id\", u.\"username\", p.\"base_currency_id\", p.\"default_currency_id\", p.\"default_account_id\" FROM \"user\" u LEFT JOIN \"profile\" p ON u.\"id\" = p.\"id\" WHERE u.\"username\" = ?";
    private static final String UPDATE_SQL = "UPDATE \"user\" SET \"username\" = ? WHERE \"id\" = ?; UPDATE \"profile\" SET \"base_currency_id\" = ?, \"default_currency_id\" = ?, \"default_account_id\" = ? WHERE \"id\" = ?;";
    private static final String DELETE_SQL = "DELETE FROM \"user\" WHERE \"id\" = ?";


    @Override
    public List<User> findAll() {
        List<User> users = new ArrayList<>();

        try (PreparedStatement statement = conn.prepareStatement(FIND_ALL_SQL);
             ResultSet rs = statement.executeQuery()) {
            fillUserListFromResultSet(users, rs);
        } catch (SQLException e) {
            logger.warn("Error while reading users: {}", e.toString());
        }

        return users;
    }


    @Override
    public Optional<User> getById(UUID id) {
        try (PreparedStatement statement = makeGetByIdStatement(id); ResultSet rs = statement.executeQuery()) {
            if (rs.next()) {
                return Optional.of(parseResultSet(rs));
            }
        } catch (SQLException e) {
            logger.info("Failed getting user by id {}: {}", id, e.toString());
        }
        return Optional.empty();
    }

    @Override
    public Optional<User> getByUsername(String username) {
        try (PreparedStatement statement = makeGetByUsernameStatement(username); ResultSet rs = statement.executeQuery()) {
            if (rs.next()) {
                return Optional.of(parseResultSet(rs));
            }
        } catch (SQLException e) {
            logger.info("Failed getting user by username {}: {}", username, e.toString());
        }
        return Optional.empty();
    }

    @Override
    public boolean add(User user) {
        logger.debug("Adding: {}", user);
        try (PreparedStatement statement = makeAddStatement(user)) {
            return statement.executeUpdate() == 1;
        } catch (SQLException e) {
            logger.error("Error while adding: {}", e.toString());
        }
        return false;
    }

    @Override
    public boolean update(User user) {
        logger.debug("Updating: {}", user);
        try (PreparedStatement statement = makeUpdateStatement(user)) {
            return statement.executeUpdate() == 1;
        } catch (SQLException e) {
            logger.error("Error while updating: {}", e.toString());
        }
        return false;
    }

    @Override
    public boolean delete(UUID id) {
        logger.info("Deleting: {}", id);
        try (PreparedStatement statement = makeDeleteStatement(id)) {
            return statement.executeUpdate() == 1;
        } catch (SQLException e) {
            logger.warn("Error while deleting: {}", e.toString());
        }
        return false;
    }

    private void fillUserListFromResultSet(List<User> users, ResultSet rs) throws SQLException {
        while (rs.next()) {
            User user = new User();
            user.setId(rs.getObject(1, UUID.class));
            user.setUsername(rs.getString(2));
            users.add(user);
        }
    }

    private PreparedStatement makeAddStatement(User user) throws SQLException {
        PreparedStatement addStatement = conn.prepareStatement(ADD_SQL);
        addStatement.setObject(1, user.getId());
        addStatement.setString(2, user.getUsername());
        addStatement.setObject(3, user.getId());
        addStatement.setObject(4, user.getProfile().getBaseCurrencyId());
        addStatement.setObject(5, user.getProfile().getDefaultCurrencyId());
        addStatement.setObject(6, user.getProfile().getDefaultAccountId());
        return addStatement;
    }

    private PreparedStatement makeUpdateStatement(User user) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement(UPDATE_SQL);
        stmt.setString(1, user.getUsername());
        stmt.setObject(2, user.getId());
        stmt.setObject(3, user.getProfile().getBaseCurrencyId());
        stmt.setObject(4, user.getProfile().getDefaultCurrencyId());
        stmt.setObject(5, user.getProfile().getDefaultAccountId());
        stmt.setObject(6, user.getId());
        return stmt;
    }

    private PreparedStatement makeGetByIdStatement(UUID id) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement(GET_BY_ID_SQL);
        stmt.setObject(1, id);
        return stmt;
    }

    private PreparedStatement makeGetByUsernameStatement(String username) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement(GET_BY_USERNAME_SQL);
        stmt.setString(1, username);
        return stmt;
    }

    private PreparedStatement makeDeleteStatement(UUID id) throws SQLException {
        PreparedStatement deleteStatement = conn.prepareStatement(DELETE_SQL);
        deleteStatement.setObject(1, id);
        return deleteStatement;
    }

    private User parseResultSet(ResultSet rs) throws SQLException {
        UUID id = rs.getObject(1, UUID.class);
        String username = rs.getString(2);
        UUID baseCurrencyId = rs.getObject(3, UUID.class);
        UUID defaultCurrencyId = rs.getObject(4, UUID.class);
        UUID defaultAccountId = rs.getObject(5, UUID.class);
        return new User(id, username, new User.Profile(baseCurrencyId, defaultCurrencyId, defaultAccountId));
    }
}
