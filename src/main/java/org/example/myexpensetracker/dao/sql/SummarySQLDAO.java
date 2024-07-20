package org.example.myexpensetracker.dao.sql;

import org.example.myexpensetracker.dao.SummaryDAO;
import org.example.myexpensetracker.db.DBConnection;
import org.example.myexpensetracker.db.dto.AccountTotalDTO;
import org.example.myexpensetracker.db.dto.ExpenseIncomeDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SummarySQLDAO implements SummaryDAO {
    protected static final Logger logger = LoggerFactory.getLogger(SummarySQLDAO.class);
    protected final Connection conn = DBConnection.getInstance().connection();

    private final static String ACCOUNT_TOTAL_SQL = """
            SELECT
            	e."account_id",
            	SUM(e."amount") AS "amount",
            	e."currency_id"
            FROM
            	"record" r
            JOIN "entry" e ON
            	r."id" = e."record_id"
            WHERE
                r."user_id" = ?
            GROUP BY
            	e."currency_id",
            	e."account_id";
            """;
    private final static String EXPENSE_INCOME_SQL = """
            SELECT
            	SUM(e."amount") AS "amount",
            	e."currency_id",
            	r."type",
            	YEAR(
            		r."date"
            	) AS "year",
            	MONTH(
            		r."date"
            	) AS "month"
            FROM
            	"record" r
            JOIN "entry" e ON
            	r."id" = e."record_id"
            WHERE
                r."user_id" = ? AND
            	r."type" IN (
            		'EXPENSE', 'INCOME'
            	)
            GROUP BY
            	e."currency_id",
            	r."type",
            	"year",
            	"month"
            ORDER BY
            	"year" DESC,
            	"month" DESC;
            """;

    @Override
    public List<AccountTotalDTO> getAccountTotals(UUID userId) {
        List<AccountTotalDTO> accountTotals = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(ACCOUNT_TOTAL_SQL)) {
            stmt.setObject(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                accountTotals.add(new AccountTotalDTO(
                        rs.getObject(1, UUID.class),
                        rs.getBigDecimal(2),
                        rs.getObject(3, UUID.class)
                ));
            }
        } catch (SQLException e) {
            logger.warn("Error while querying account totals: {}", e.toString());
        }
        return accountTotals;
    }

    @Override
    public List<ExpenseIncomeDTO> getExpenseIncome(UUID userId) {
        List<ExpenseIncomeDTO> expenseIncomeDTOS = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(EXPENSE_INCOME_SQL)) {
            stmt.setObject(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                expenseIncomeDTOS.add(new ExpenseIncomeDTO(
                        rs.getBigDecimal(1),
                        rs.getObject(2, UUID.class),
                        rs.getString(3),
                        rs.getInt(4),
                        rs.getInt(5)
                ));
            }
        } catch (SQLException e) {
            logger.warn("Error while querying expense incomes: {}", e.toString());
        }
        return expenseIncomeDTOS;
    }
}
