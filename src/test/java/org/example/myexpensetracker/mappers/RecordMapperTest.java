package org.example.myexpensetracker.mappers;

import org.example.myexpensetracker.db.dto.RecordDTO;
import org.example.myexpensetracker.model.Record;
import org.example.myexpensetracker.model.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class RecordMapperTest {
    private static Context ctx;

    @BeforeAll
    static void beforeAll() {
        User testUser = User.create("test");
        ctx = new Context(testUser);
        ctx.setAccounts(List.of(Account.create(testUser.getId(), "test account 1", Account.Type.CASH, null),
                Account.create(testUser.getId(), "test account 2", Account.Type.CASH, null)));
        ctx.setCurrencies(List.of(Currency.create(testUser.getId(), "test currency 1", "CR1", "A"),
                Currency.create(testUser.getId(), "test currency 2", "CR2", "B")));
    }

    @Test
    void entity2dtoExpense() {
        Expense expense = Expense.create(ctx.getUserId(), LocalDate.now(), null);
        Amount amount = new Amount(new BigDecimal(10), ctx.getCurrencies().get(0));
        Entry entry = new Entry(ctx.getAccounts().get(0), amount, null, null, new ArrayList<>());
        expense.addEntry(entry);
        RecordDTO recordDTO = RecordMapper.entity2dto(expense);

        assertEquals(expense.getId(), recordDTO.id());
        assertEquals(expense.getOwnerId(), recordDTO.ownerId());
        assertEquals(recordDTO.entries().size(), 1);
        assertEquals(recordDTO.type(), Record.Type.EXPENSE);

        RecordDTO.Entry entryDTO = recordDTO.entries().get(0);
        assertEquals(entry.getAccount().getId(), entryDTO.accountId());
        assertEquals(amount.getCurrency().getId(), entryDTO.currencyId());
        assertEquals(amount.getValue(), entryDTO.amount().negate());
    }

    @Test
    void dto2entityExpense() {
        RecordDTO.Entry entryDTO = new RecordDTO.Entry(ctx.getAccounts().get(0).getId(), new BigDecimal(10), ctx.getCurrencies().get(0).getId(), null, null, new HashSet<>());
        RecordDTO recordDTO = new RecordDTO(UUID.randomUUID(), ctx.getUserId(), LocalDate.now(), null, Record.Type.EXPENSE, List.of(entryDTO));
        Record record = RecordMapper.dto2entity(recordDTO, ctx);

        assertInstanceOf(Expense.class, record);
        Expense expense = (Expense) record;

        assertEquals(expense.getId(), recordDTO.id());
        assertEquals(expense.getOwnerId(), recordDTO.ownerId());
        assertEquals(expense.getEntries().size(), 1);

        Entry entry = expense.getEntries().get(0);
        assertEquals(entry.getAccount().getId(), entryDTO.accountId());
        assertEquals(entry.getAmount().getCurrency().getId(), entryDTO.currencyId());
        assertEquals(entry.getAmount().getValue(), entryDTO.amount().negate());
    }


    @Test
    void entity2dtoIncome() {
        Income income = Income.create(ctx.getUserId(), LocalDate.now(), null);
        Amount amount = new Amount(new BigDecimal(10), ctx.getCurrencies().get(0));
        Entry entry = new Entry(ctx.getAccounts().get(0), amount, null, null, new ArrayList<>());
        income.addEntry(entry);
        RecordDTO recordDTO = RecordMapper.entity2dto(income);

        assertEquals(income.getId(), recordDTO.id());
        assertEquals(income.getOwnerId(), recordDTO.ownerId());
        assertEquals(recordDTO.entries().size(), 1);
        assertEquals(recordDTO.type(), Record.Type.INCOME);

        RecordDTO.Entry entryDTO = recordDTO.entries().get(0);
        assertEquals(entry.getAccount().getId(), entryDTO.accountId());
        assertEquals(amount.getCurrency().getId(), entryDTO.currencyId());
        assertEquals(amount.getValue(), entryDTO.amount());
    }

    @Test
    void dto2entityIncome() {
        RecordDTO.Entry entryDTO = new RecordDTO.Entry(ctx.getAccounts().get(0).getId(), new BigDecimal(10), ctx.getCurrencies().get(0).getId(), null, null, new HashSet<>());
        RecordDTO recordDTO = new RecordDTO(UUID.randomUUID(), ctx.getUserId(), LocalDate.now(), null, Record.Type.INCOME, List.of(entryDTO));
        Record record = RecordMapper.dto2entity(recordDTO, ctx);

        assertInstanceOf(Income.class, record);
        Income income = (Income) record;

        assertEquals(income.getId(), recordDTO.id());
        assertEquals(income.getOwnerId(), recordDTO.ownerId());
        assertEquals(income.getEntries().size(), 1);

        Entry entry = income.getEntries().get(0);
        assertEquals(entry.getAccount().getId(), entryDTO.accountId());
        assertEquals(entry.getAmount().getCurrency().getId(), entryDTO.currencyId());
        assertEquals(entry.getAmount().getValue(), entryDTO.amount());
    }

    @Test
    void entity2dtoExchange() {
        Exchange exchange = Exchange.create(ctx.getUserId(), LocalDate.now(),
                ctx.getAccounts().get(0),
                new Amount(new BigDecimal(10), ctx.getCurrencies().get(0)),
                new Amount(new BigDecimal(20), ctx.getCurrencies().get(1)),
                null);

        RecordDTO recordDTO = RecordMapper.entity2dto(exchange);

        assertEquals(exchange.getId(), recordDTO.id());
        assertEquals(exchange.getOwnerId(), recordDTO.ownerId());
        assertEquals(recordDTO.entries().size(), 2);
        assertEquals(recordDTO.type(), Record.Type.EXCHANGE);

        RecordDTO.Entry entryDTO0 = recordDTO.entries().get(0);
        RecordDTO.Entry entryDTO1 = recordDTO.entries().get(1);
        assertEquals(exchange.getAccount().getId(), entryDTO0.accountId());
        assertEquals(exchange.getAccount().getId(), entryDTO1.accountId());
        RecordDTO.Entry fromEntry = entryDTO0.amount().compareTo(BigDecimal.ZERO) < 0 ? entryDTO0 : entryDTO1;
        RecordDTO.Entry toEntry = entryDTO0.amount().compareTo(BigDecimal.ZERO) > 0 ? entryDTO0 : entryDTO1;
        assertEquals(fromEntry.amount(), new BigDecimal(-10));
        assertEquals(toEntry.amount(), new BigDecimal(20));
    }

    @Test
    void dto2entityExchange() {
        Account account = ctx.getAccounts().get(0);
        Currency fromCurrency = ctx.getCurrencies().get(0);
        Currency toCurrency = ctx.getCurrencies().get(1);

        RecordDTO.Entry entryDTO1 = new RecordDTO.Entry(account.getId(), new BigDecimal(-10), fromCurrency.getId(), null, null, new HashSet<>());
        RecordDTO.Entry entryDTO2 = new RecordDTO.Entry(account.getId(), new BigDecimal(20), toCurrency.getId(), null, null, new HashSet<>());
        RecordDTO recordDTO = new RecordDTO(UUID.randomUUID(), ctx.getUserId(), LocalDate.now(), null, Record.Type.EXCHANGE, List.of(entryDTO1, entryDTO2));
        Record record = RecordMapper.dto2entity(recordDTO, ctx);

        assertInstanceOf(Exchange.class, record);
        Exchange exchange = (Exchange) record;

        assertEquals(exchange.getId(), recordDTO.id());
        assertEquals(exchange.getOwnerId(), recordDTO.ownerId());
        assertEquals(exchange.getFromAmount().getValue(), new BigDecimal(10));
        assertEquals(exchange.getFromAmount().getCurrency().getId(), fromCurrency.getId());
        assertEquals(exchange.getToAmount().getValue(), new BigDecimal(20));
        assertEquals(exchange.getToAmount().getCurrency().getId(), toCurrency.getId());
    }


    @Test
    void entity2dtoTransfer() {
        Transfer transfer = Transfer.create(ctx.getUserId(), LocalDate.now(),
                ctx.getAccounts().get(0), ctx.getAccounts().get(1),
                new Amount(new BigDecimal(10), ctx.getCurrencies().get(0)),
                null);

        RecordDTO recordDTO = RecordMapper.entity2dto(transfer);

        assertEquals(transfer.getId(), recordDTO.id());
        assertEquals(transfer.getOwnerId(), recordDTO.ownerId());
        assertEquals(recordDTO.entries().size(), 2);
        assertEquals(recordDTO.type(), Record.Type.TRANSFER);

        RecordDTO.Entry entryDTO0 = recordDTO.entries().get(0);
        RecordDTO.Entry entryDTO1 = recordDTO.entries().get(1);
        assertEquals(transfer.getAmount().getCurrency().getId(), entryDTO0.currencyId());
        assertEquals(transfer.getAmount().getCurrency().getId(), entryDTO1.currencyId());
        RecordDTO.Entry fromEntry = entryDTO0.amount().compareTo(BigDecimal.ZERO) < 0 ? entryDTO0 : entryDTO1;
        RecordDTO.Entry toEntry = entryDTO0.amount().compareTo(BigDecimal.ZERO) > 0 ? entryDTO0 : entryDTO1;
        assertEquals(fromEntry.amount(), new BigDecimal(-10));
        assertEquals(toEntry.amount(), new BigDecimal(10));
        assertEquals(fromEntry.accountId(), transfer.getFromAccount().getId());
        assertEquals(toEntry.accountId(), transfer.getToAccount().getId());
        assertEquals(toEntry.amount(), fromEntry.amount().negate());
    }

    @Test
    void dto2entityTransfer() {
        Account fromAccount = ctx.getAccounts().get(0);
        Account toAccount = ctx.getAccounts().get(1);
        Currency currency = ctx.getCurrencies().get(0);

        RecordDTO.Entry entryDTO1 = new RecordDTO.Entry(fromAccount.getId(), new BigDecimal(-10), currency.getId(), null, null, new HashSet<>());
        RecordDTO.Entry entryDTO2 = new RecordDTO.Entry(toAccount.getId(), new BigDecimal(10), currency.getId(), null, null, new HashSet<>());
        RecordDTO recordDTO = new RecordDTO(UUID.randomUUID(), ctx.getUserId(), LocalDate.now(), null, Record.Type.TRANSFER, List.of(entryDTO1, entryDTO2));
        Record record = RecordMapper.dto2entity(recordDTO, ctx);

        assertInstanceOf(Transfer.class, record);
        Transfer transfer = (Transfer) record;

        assertEquals(transfer.getId(), recordDTO.id());
        assertEquals(transfer.getOwnerId(), recordDTO.ownerId());
        assertEquals(transfer.getFromAccount().getId(), fromAccount.getId());
        assertEquals(transfer.getToAccount().getId(), toAccount.getId());
        assertEquals(transfer.getAmount().getCurrency().getId(), currency.getId());
        assertEquals(transfer.getAmount().getValue(), new BigDecimal(10));
    }
}