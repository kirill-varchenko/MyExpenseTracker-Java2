package org.example.myexpensetracker.export;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.example.myexpensetracker.export.model.*;
import org.example.myexpensetracker.model.Currency;
import org.example.myexpensetracker.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;

public class ContextXmlExporter {
    private static final Logger logger = LoggerFactory.getLogger(ContextXmlExporter.class);

    public static void export(Context ctx, File file) {
        XmlMapper xmlMapper = new XmlMapper();
        xmlMapper.enable(SerializationFeature.INDENT_OUTPUT);

        ContextXml contextXml = map(ctx);

        try {
            xmlMapper.writeValue(file, contextXml);
            logger.info("Context saved to: {}", file);
        } catch (IOException e) {
            logger.warn("Error while saving context: {}", e.toString());
        }
    }

    private static ContextXml map(Context ctx) {
        ContextXml contextXml = new ContextXml();
        contextXml.setUser(map(ctx.getUser()));
        contextXml.setCurrencies(mapCurrencies(ctx.getCurrencies()));
        contextXml.setAccounts(mapAccounts(ctx.getAccounts()));
        contextXml.setCategories(mapCategories(ctx.getCategories()));
        contextXml.setTags(mapTags(ctx.getTags()));
        return contextXml;
    }

    private static UserXml map(User user) {
        UserXml userXml = new UserXml();
        userXml.setId(user.getId());
        userXml.setUsername(user.getUsername());
        return userXml;
    }

    private static List<CurrencyXml> mapCurrencies(List<Currency> currencies) {
        return currencies.stream().map(ContextXmlExporter::map).toList();
    }

    private static CurrencyXml map(Currency currency) {
        CurrencyXml currencyXml = new CurrencyXml();
        currencyXml.setId(currency.getId());
        currencyXml.setActive(currency.isActive());
        currencyXml.setName(currency.getName());
        currencyXml.setCode(currency.getCode());
        currencyXml.setSymbol(currency.getSymbol());
        return  currencyXml;
    }

    private static AccountXml map(Account account) {
        AccountXml accountXml = new AccountXml();
        accountXml.setId(account.getId());
        accountXml.setActive(account.isActive());
        accountXml.setName(account.getName());
        return accountXml;
    }

    private static CategoryXml map(Category category) {
        CategoryXml categoryXml = new CategoryXml();
        categoryXml.setId(category.getId());
        categoryXml.setActive(category.isActive());
        categoryXml.setName(category.getName());
        return categoryXml;
    }

    private static List<TagXml> mapTags(List<Tag> tags) {
        return tags.stream().map(ContextXmlExporter::map).toList();
    }

    private static TagXml map(Tag tag) {
        TagXml tagXml = new TagXml();
        tagXml.setId(tag.getId());
        tagXml.setActive(tag.isActive());
        tagXml.setName(tag.getName());
        return tagXml;
    }

    private static List<AccountXml> mapAccounts(List<Account> accounts) {
        return mapTree(accounts, ContextXmlExporter::map);
    }

    private static List<CategoryXml> mapCategories(List<Category> categories) {
        return mapTree(categories, ContextXmlExporter::map);
    }

    private static <T extends HasChildren<T>, U extends TreeStructure> List<T> mapTree(List<U> items, Function<U, T> mapper) {
        List<T> roots = new ArrayList<>();
        Map<UUID, UUID> parents = new HashMap<>();
        Map<UUID, T> tree = new HashMap<>();

        for (U item : items) {
            T mappedItem = mapper.apply(item);
            if (item.getParentId() != null) {
                parents.put(item.getId(), item.getParentId());
            } else {
                roots.add(mappedItem);
            }
            tree.put(item.getId(), mappedItem);
        }

        for (Map.Entry<UUID, UUID> entry : parents.entrySet()) {
            T item = tree.get(entry.getKey());
            T parent = tree.get(entry.getValue());
            if (parent.getChildren() == null) {
                parent.setChildren(new ArrayList<>());
            }
            parent.getChildren().add(item);
        }

        return roots;
    }
}
