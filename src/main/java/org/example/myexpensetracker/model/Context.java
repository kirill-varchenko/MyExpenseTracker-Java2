package org.example.myexpensetracker.model;

import lombok.Getter;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Context {
    @Getter
    private final User user;

    @Getter
    private List<Account> accounts = new ArrayList<>();
    @Getter
    private List<Currency> currencies = new ArrayList<>();
    @Getter
    private List<Category> categories = new ArrayList<>();
    @Getter
    private List<Tag> tags = new ArrayList<>();

    private Map<UUID, Account> accountMap;
    private Map<UUID, Currency> currencyMap;
    private Map<UUID, Category> categoryMap;
    private Map<UUID, Tag> tagMap;

    public Context(User user) {
        this.user = user;
    }

    public UUID getUserId() {
        return user.getId();
    }

    public void setAccounts(List<Account> accounts) {
        this.accounts = accounts;
        accountMap = accounts.stream().collect(Collectors.toMap(Account::getId, Function.identity()));
    }

    public void setCurrencies(List<Currency> currencies) {
        this.currencies = currencies;
        currencyMap = currencies.stream().collect(Collectors.toMap(Currency::getId, Function.identity()));
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
        categoryMap = categories.stream().collect(Collectors.toMap(Category::getId, Function.identity()));
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
        tagMap = tags.stream().collect(Collectors.toMap(Tag::getId, Function.identity()));
    }

    public Optional<Account> findAccountById(UUID id) {
        return Optional.ofNullable(accountMap.get(id));
    }

    public Optional<Category> findCategoryById(UUID id) {
        return Optional.ofNullable(categoryMap.get(id));
    }

    public Optional<Currency> findCurrencyById(UUID id) {
        return Optional.ofNullable(currencyMap.get(id));
    }

    public Optional<Tag> findTagById(UUID id) {
        return Optional.ofNullable(tagMap.get(id));
    }
}
