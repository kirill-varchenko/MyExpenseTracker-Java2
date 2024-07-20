package org.example.myexpensetracker.export.model;

import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonRootName(value = "context")
public class ContextXml {
    @JacksonXmlProperty
    private UserXml user;

    @JacksonXmlElementWrapper(localName = "currencies")
    @JacksonXmlProperty(localName = "currency")
    private List<CurrencyXml> currencies;

    @JacksonXmlElementWrapper(localName = "accounts")
    @JacksonXmlProperty(localName = "account")
    private List<AccountXml> accounts;

    @JacksonXmlElementWrapper(localName = "categories")
    @JacksonXmlProperty(localName = "category")
    private List<CategoryXml> categories;

    @JacksonXmlElementWrapper(localName = "tags")
    @JacksonXmlProperty(localName = "tag")
    private List<TagXml> tags;
}
