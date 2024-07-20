package org.example.myexpensetracker.exchangerateproviders;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface ExchangeRateProvider {
    Map<String, BigDecimal> fetch(String base, List<String> currencies);
}
