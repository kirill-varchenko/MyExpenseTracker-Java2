package org.example.myexpensetracker.mappers;

import org.example.myexpensetracker.db.dto.AccountTotalDTO;
import org.example.myexpensetracker.db.dto.ExpenseIncomeDTO;
import org.example.myexpensetracker.model.AccountTotal;
import org.example.myexpensetracker.model.Context;
import org.example.myexpensetracker.model.MonthlyAmount;
import org.example.myexpensetracker.model.Record;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class SummaryMapper {
    public static List<AccountTotal> mapAccountTotal(List<AccountTotalDTO> accountTotalDTOs, Context ctx) {
        Map<UUID, AccountTotal> map = new HashMap<>();

        AccountTotal accountTotal;
        for (AccountTotalDTO dto : accountTotalDTOs) {
            if (!map.containsKey(dto.accountId())) {
                accountTotal = new AccountTotal(ctx.findAccountById(dto.accountId()).get(), new HashMap<>());
                map.put(dto.accountId(), accountTotal);
            } else {
                accountTotal = map.get(dto.accountId());
            }
            accountTotal.getTotals().put(ctx.findCurrencyById(dto.currencyId()).get(), dto.amount());
        }

        return map.values().stream().toList();
    }

    public static Map<String, List<MonthlyAmount>> mapMonthlyExpenseIncome(List<ExpenseIncomeDTO> expenseIncomeDTOs, Context ctx) {
        return expenseIncomeDTOs.stream().map(dto -> new MonthlyAmount(
                dto.amount(),
                ctx.findCurrencyById(dto.currencyId()).get(),
                String.format("%d-%02d", dto.year(), dto.month()),
                Record.Type.valueOf(dto.type()))).collect(Collectors.groupingBy(monthlyAmount -> String.format("%s (%s)", monthlyAmount.getType().toString(), monthlyAmount.getCurrency().getSymbol())));
    }
}
