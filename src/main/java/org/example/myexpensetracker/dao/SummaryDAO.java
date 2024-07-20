package org.example.myexpensetracker.dao;

import org.example.myexpensetracker.db.dto.AccountTotalDTO;
import org.example.myexpensetracker.db.dto.ExpenseIncomeDTO;

import java.util.List;
import java.util.UUID;

public interface SummaryDAO {
    List<AccountTotalDTO> getAccountTotals(UUID userId);
    List<ExpenseIncomeDTO> getExpenseIncome(UUID userId);
}
