package org.example.myexpensetracker.mappers;

import org.example.myexpensetracker.db.dto.RecordDTO;
import org.example.myexpensetracker.model.Record;
import org.example.myexpensetracker.model.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public class RecordMapper {
    public static RecordDTO entity2dto(Record record) {
        List<RecordDTO.Entry> entries = switch (record) {
            case Expense e -> e.getEntries().stream().map(entry -> new RecordDTO.Entry(
                    entry.getAccount().getId(),
                    entry.getAmount().getValue().negate(),
                    entry.getAmount().getCurrency().getId(),
                    entry.getCategory() != null ? entry.getCategory().getId() : null,
                    entry.getComment(),
                    entry.getTags().stream().map(Tag::getId).collect(Collectors.toSet()))
            ).toList();
            case Income i -> i.getEntries().stream().map(entry -> new RecordDTO.Entry(
                    entry.getAccount().getId(),
                    entry.getAmount().getValue(),
                    entry.getAmount().getCurrency().getId(),
                    entry.getCategory() != null ? entry.getCategory().getId() : null,
                    entry.getComment(),
                    entry.getTags().stream().map(Tag::getId).collect(Collectors.toSet()))
            ).toList();
            case Exchange ex ->
                    List.of(new RecordDTO.Entry(ex.getAccount().getId(), ex.getFromAmount().getValue().negate(), ex.getFromAmount().getCurrency().getId(), null, null, new HashSet<>()),
                            new RecordDTO.Entry(ex.getAccount().getId(), ex.getToAmount().getValue(), ex.getToAmount().getCurrency().getId(), null, null, new HashSet<>()));
            case Transfer tr ->
                    List.of(new RecordDTO.Entry(tr.getFromAccount().getId(), tr.getAmount().getValue().negate(), tr.getAmount().getCurrency().getId(), null, null, new HashSet<>()),
                            new RecordDTO.Entry(tr.getToAccount().getId(), tr.getAmount().getValue(), tr.getAmount().getCurrency().getId(), null, null, new HashSet<>()));
        };
        return new RecordDTO(record.getId(), record.getOwnerId(), record.getDate(), record.getComment(), record.getType(), entries);
    }

    public static Record dto2entity(RecordDTO recordDTO, Context ctx) {
        List<Entry> entries;

        switch (recordDTO.type()) {
            case EXPENSE:
                entries = recordDTO.entries().stream().map(entry ->
                        new Entry(
                                ctx.findAccountById(entry.accountId()).get(),
                                new Amount(entry.amount().negate(), ctx.findCurrencyById(entry.currencyId()).get()),
                                entry.categoryId() != null ? ctx.findCategoryById(entry.categoryId()).get() : null,
                                entry.comment(),
                                entry.tagIds().stream().map(tagId -> ctx.findTagById(tagId).get()).collect(Collectors.toCollection(ArrayList::new))
                        )).collect(Collectors.toCollection(ArrayList::new));
                return new Expense(recordDTO.id(), recordDTO.ownerId(), recordDTO.date(), recordDTO.comment(), entries);
            case INCOME:
                entries = recordDTO.entries().stream().map(entry ->
                        new Entry(
                                ctx.findAccountById(entry.accountId()).get(),
                                new Amount(entry.amount(), ctx.findCurrencyById(entry.currencyId()).get()),
                                entry.categoryId() != null ? ctx.findCategoryById(entry.categoryId()).get() : null,
                                entry.comment(),
                                entry.tagIds().stream().map(tagId -> ctx.findTagById(tagId).get()).collect(Collectors.toCollection(ArrayList::new))
                        )).collect(Collectors.toCollection(ArrayList::new));
                return new Income(recordDTO.id(), recordDTO.ownerId(), recordDTO.date(), recordDTO.comment(), entries);
            case EXCHANGE:
                RecordDTO.Entry fromEntry, toEntry;
                if (recordDTO.entries().get(0).amount().compareTo(BigDecimal.ZERO) > 0) {
                    toEntry = recordDTO.entries().get(0);
                    fromEntry = recordDTO.entries().get(1);
                } else {
                    toEntry = recordDTO.entries().get(1);
                    fromEntry = recordDTO.entries().get(0);
                }
                return new Exchange(recordDTO.id(), recordDTO.ownerId(), recordDTO.date(),
                        ctx.findAccountById(fromEntry.accountId()).get(),
                        new Amount(fromEntry.amount().negate(), ctx.findCurrencyById(fromEntry.currencyId()).get()),
                        new Amount(toEntry.amount(), ctx.findCurrencyById(toEntry.currencyId()).get()),
                        recordDTO.comment());
            case TRANSFER:
                RecordDTO.Entry fromEntryTr, toEntryTr;
                if (recordDTO.entries().get(0).amount().compareTo(BigDecimal.ZERO) > 0) {
                    toEntryTr = recordDTO.entries().get(0);
                    fromEntryTr = recordDTO.entries().get(1);
                } else {
                    toEntryTr = recordDTO.entries().get(1);
                    fromEntryTr = recordDTO.entries().get(0);
                }
                return new Transfer(recordDTO.id(), recordDTO.ownerId(), recordDTO.date(),
                        ctx.findAccountById(fromEntryTr.accountId()).get(), ctx.findAccountById(toEntryTr.accountId()).get(),
                        new Amount(toEntryTr.amount(), ctx.findCurrencyById(toEntryTr.currencyId()).get()),
                        recordDTO.comment());
            default:
                return null;
        }
    }
}
