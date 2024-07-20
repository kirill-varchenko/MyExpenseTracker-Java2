package org.example.myexpensetracker.dao.sql;

import org.example.myexpensetracker.dao.EntityDAO;
import org.example.myexpensetracker.db.DBConnection;
import org.example.myexpensetracker.db.dto.RecordDTO;
import org.example.myexpensetracker.model.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;

public class RecordSQLDAO implements EntityDAO<RecordDTO> {
    protected static final Logger logger = LoggerFactory.getLogger(RecordSQLDAO.class);
    protected final Connection conn = DBConnection.getInstance().connection();

    private static final String ADD_RECORD_SQL = "INSERT INTO \"record\" (\"id\", \"user_id\", \"date\", \"type\", \"comment\") VALUES (?, ?, ?, ?, ?)";
    private static final String ADD_ENTRY_SQL = "INSERT INTO \"entry\" (\"record_id\", \"account_id\", \"amount\", \"currency_id\", \"category_id\", \"comment\") VALUES (?, ?, ?, ?, ?, ?)";
    private static final String ADD_ENTRY_TAG_SQL = "INSERT INTO \"entry_tag\" (\"entry_id\", \"tag_id\") VALUES (?, ?)";
    private static final String FIND_ALL_SQL = """
            SELECT r."id" as "record_id", r."date", r."comment" AS "record_comment", r."type",
                   e."id" as "entry_id", e."account_id", e."amount", e."currency_id", e."category_id", e."comment" AS "entry_comment",
                   et."tag_id"
            FROM "record" r
            LEFT JOIN "entry" e ON r."id" = e."record_id"
            LEFT JOIN "entry_tag" et ON e."id" = et."entry_id"
            WHERE r."user_id" = ?
            ORDER BY r."date" DESC, r."id", e."id";
            """;
    private static final String DELETE_ENTRY_SQL = "DELETE FROM \"entry\" WHERE \"record_id\" = ?";
    private static final String UPDATE_RECORD_SQL =  "UPDATE \"record\" SET \"date\" = ?, \"comment\" = ? WHERE \"id\" = ? AND \"user_id\" = ?";
    private static final String DELETE_SQL = "DELETE FROM \"record\" WHERE \"id\" = ? AND \"user_id\" = ?";

    @Override
    public List<RecordDTO> findAll(UUID ownerId) {
        List<RecordDTO> records = new ArrayList<>();
        Map<UUID, RecordDTO> recordMap = new HashMap<>();
        Map<Integer, RecordDTO.Entry> entryMap = new HashMap<>();

        try (PreparedStatement statement = conn.prepareStatement(FIND_ALL_SQL)) {
            statement.setObject(1, ownerId);
            ResultSet resultSet = statement.executeQuery();

            RecordDTO record;
            RecordDTO.Entry entry;
            while (resultSet.next()) {
                UUID recordId = UUID.fromString(resultSet.getString("record_id"));

                if (!recordMap.containsKey(recordId)) {
                    LocalDate date = resultSet.getDate("date").toLocalDate();
                    String recordComment = resultSet.getString("record_comment");
                    Record.Type type = Record.Type.valueOf(resultSet.getString("type"));
                    record = new RecordDTO(recordId, ownerId, date, recordComment, type, new ArrayList<>());
                    recordMap.put(recordId, record);
                    records.add(record);
                } else {
                    record = recordMap.get(recordId);
                }

                int entryId = resultSet.getInt("entry_id");

                if (!entryMap.containsKey(entryId)) {
                    UUID accountId = resultSet.getObject("account_id", UUID.class);
                    BigDecimal amount = resultSet.getBigDecimal("amount");
                    UUID currencyId = resultSet.getObject("currency_id", UUID.class);
                    UUID categoryId = resultSet.getObject("category_id", UUID.class);
                    String entryComment = resultSet.getString("entry_comment");
                    entry = new RecordDTO.Entry(accountId, amount, currencyId, categoryId, entryComment, new HashSet<>());
                    entryMap.put(entryId, entry);
                    record.entries().add(entry);
                } else {
                    entry = entryMap.get(entryId);
                }

                UUID tagId = resultSet.getObject("tag_id", UUID.class);
                if (tagId != null) {
                    entry.tagIds().add(tagId);
                }
            }
        } catch (SQLException e) {
            logger.error("Error while finding all Records: {}", e.toString());
        }
        return records;

    }

    @Override
    public boolean add(RecordDTO item) {
        logger.debug("To add: {}", item);
        try {
            conn.setAutoCommit(false);
            PreparedStatement recordStatement = conn.prepareStatement(ADD_RECORD_SQL);
            recordStatement.setObject(1, item.id());
            recordStatement.setObject(2, item.ownerId());
            recordStatement.setObject(3, item.date());
            recordStatement.setString(4, item.type().name());
            recordStatement.setString(5, item.comment());
            recordStatement.executeUpdate();

            PreparedStatement entryStatement = conn.prepareStatement(ADD_ENTRY_SQL, PreparedStatement.RETURN_GENERATED_KEYS);
            PreparedStatement entryTagStatement = conn.prepareStatement(ADD_ENTRY_TAG_SQL);
            for (RecordDTO.Entry entry : item.entries()) {
                entryStatement.setObject(1, item.id());
                entryStatement.setObject(2, entry.accountId());
                entryStatement.setBigDecimal(3, entry.amount());
                entryStatement.setObject(4, entry.currencyId());
                entryStatement.setObject(5, entry.categoryId());
                entryStatement.setString(6, entry.comment());
                entryStatement.executeUpdate();

                ResultSet generatedKeys = entryStatement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int entryId = generatedKeys.getInt(1);

                    for (UUID tagId : entry.tagIds()) {
                        entryTagStatement.setInt(1, entryId);
                        entryTagStatement.setObject(2, tagId);
                        entryTagStatement.addBatch();
                    }
                }
            }
            entryTagStatement.executeBatch();
            conn.commit();
            conn.setAutoCommit(true);
            return true;
        } catch (SQLException e) {
            logger.error("Error while adding Record: {}", e.toString());
            return false;
        }
    }

    @Override
    public boolean update(RecordDTO item) {
        logger.debug("To update: {}", item);
        try {
            conn.setAutoCommit(false);
            PreparedStatement recordStatement = conn.prepareStatement(UPDATE_RECORD_SQL);
            recordStatement.setObject(1, item.date());
            recordStatement.setString(2, item.comment());
            recordStatement.setObject(3, item.id());
            recordStatement.setObject(4, item.ownerId());
            recordStatement.executeUpdate();

            PreparedStatement deleteOldEntriesStatement = conn.prepareStatement(DELETE_ENTRY_SQL);
            deleteOldEntriesStatement.setObject(1, item.id());
            deleteOldEntriesStatement.executeUpdate();

            PreparedStatement entryStatement = conn.prepareStatement(ADD_ENTRY_SQL, PreparedStatement.RETURN_GENERATED_KEYS);
            PreparedStatement entryTagStatement = conn.prepareStatement(ADD_ENTRY_TAG_SQL);
            for (RecordDTO.Entry entry : item.entries()) {
                entryStatement.setObject(1, item.id());
                entryStatement.setObject(2, entry.accountId());
                entryStatement.setBigDecimal(3, entry.amount());
                entryStatement.setObject(4, entry.currencyId());
                entryStatement.setObject(5, entry.categoryId());
                entryStatement.setString(6, entry.comment());
                entryStatement.executeUpdate();

                ResultSet generatedKeys = entryStatement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int entryId = generatedKeys.getInt(1);

                    for (UUID tagId : entry.tagIds()) {
                        entryTagStatement.setInt(1, entryId);
                        entryTagStatement.setObject(2, tagId);
                        entryTagStatement.addBatch();
                    }
                }
            }
            entryTagStatement.executeBatch();
            conn.commit();
            conn.setAutoCommit(true);
            return true;
        } catch (SQLException e) {
            logger.error("Error while updating Record: {}", e.toString());
            return false;
        }
    }

    @Override
    public boolean delete(UUID id, UUID ownerId) {
        logger.debug("To delete Record #{}", id);
        try (PreparedStatement deleteStatement = conn.prepareStatement(DELETE_SQL)) {
            deleteStatement.setObject(1, id);
            deleteStatement.setObject(2, ownerId);
            deleteStatement.executeUpdate();
            return deleteStatement.executeUpdate() == 1;
        } catch (SQLException e) {
            logger.error("Error while deleting Record #{}: {}", id, e.toString());
        }
        return false;
    }
}
