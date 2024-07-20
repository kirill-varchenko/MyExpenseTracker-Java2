package org.example.myexpensetracker.export.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;

import java.util.UUID;

@Data
public class CurrencyXml {
    @JacksonXmlProperty(isAttribute = true)
    private UUID id;

    @JacksonXmlProperty(isAttribute = true)
    private boolean active;

    @JacksonXmlProperty
    private String name;
    @JacksonXmlProperty
    private String code;
    @JacksonXmlProperty
    private String symbol;
}
