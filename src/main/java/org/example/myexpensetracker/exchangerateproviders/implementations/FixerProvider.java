package org.example.myexpensetracker.exchangerateproviders.implementations;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.example.myexpensetracker.exchangerateproviders.ExchangeRateProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.example.myexpensetracker.common.IOUtils.readStream;


public class FixerProvider implements ExchangeRateProvider {
    private static final Logger logger = LoggerFactory.getLogger(FixerProvider.class);
    private String apiKey;
    private final ObjectMapper objectMapper;

    public FixerProvider(String apiKey) {
        this.apiKey = apiKey;
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    public Map<String, BigDecimal> fetch(String baseCurrency, List<String> currencies) {
        logger.info("Fetching exchange rates from fixer: {} - {}", baseCurrency, currencies);
        try {
            String json = fetchJson(baseCurrency, currencies);
            FixerResponse response = parseJson(json);
            return response.getRates();
        } catch (JsonProcessingException e) {
            logger.warn("Json processing error: {}", e.toString());
        } catch (IOException e) {
            logger.warn("IO error: {}", e.toString());
        }
        return new HashMap<>();
    }

    private String fetchJson(String baseCurrency, List<String> currencies) throws IOException {
        String apiUrl = String.format("https://api.apilayer.com/fixer/latest?base=%s&symbols=%s", baseCurrency, String.join(",", currencies));
        URL url = URI.create(apiUrl).toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("apikey", apiKey);
        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            logger.debug("Response code is {}: {}", responseCode, readStream(conn.getErrorStream()));
            throw new IOException("HttpResponseCode: " + responseCode);
        }

        return readStream(conn.getInputStream());
    }

    private FixerResponse parseJson(String json) throws JsonProcessingException {
        return objectMapper.readValue(json, FixerResponse.class);
    }

}
