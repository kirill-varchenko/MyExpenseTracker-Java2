package org.example.myexpensetracker.controller;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import lombok.Getter;
import lombok.Setter;
import org.example.myexpensetracker.exchangerateproviders.ExchangeRateProvider;
import org.example.myexpensetracker.model.Context;
import org.example.myexpensetracker.model.Currency;
import org.example.myexpensetracker.model.ExchangeRate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
@Setter
public class ExchangeRateService extends Service<List<ExchangeRate>> {
    private static final Logger logger = LoggerFactory.getLogger(ExchangeRateService.class);

    private Context ctx;
    private ExchangeRateProvider exchangeRateProvider;

    @Override
    protected Task<List<ExchangeRate>> createTask() {
        return new Task<List<ExchangeRate>>() {
            @Override
            protected List<ExchangeRate> call() throws Exception {
                if (ctx == null || exchangeRateProvider == null) {
                    logger.warn("Context or ExchangeRateProvider is null");
                    return null;
                }
                UUID baseCurrencyId = ctx.getUser().getProfile().getBaseCurrencyId();
                if (baseCurrencyId == null) {
                    logger.info("Base currency is not set.");
                    return null;
                }
                Currency fromCurrency = ctx.findCurrencyById(baseCurrencyId).get();
                List<String> currencies = ctx.getCurrencies().stream().filter(currency -> !currency.getId().equals(baseCurrencyId)).map(Currency::getCode).toList();
                Map<String, Currency> currencyMap = ctx.getCurrencies().stream().collect(Collectors.toMap(Currency::getCode, Function.identity()));

                Map<String, BigDecimal> fetchedRates = exchangeRateProvider.fetch(fromCurrency.getCode(), currencies);

                return fetchedRates.entrySet().stream().map(entry -> new ExchangeRate(
                        fromCurrency,
                        currencyMap.get(entry.getKey()),
                        entry.getValue())
                ).toList();
            }
        };
    }
}
