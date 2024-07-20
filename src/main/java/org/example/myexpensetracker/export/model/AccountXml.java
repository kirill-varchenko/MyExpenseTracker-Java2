package org.example.myexpensetracker.export.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;
import org.example.myexpensetracker.model.Account;

import java.util.List;
import java.util.UUID;

@Data
public class AccountXml implements HasChildren<AccountXml> {
    @JacksonXmlProperty(isAttribute = true)
    private UUID id;

    @JacksonXmlProperty(isAttribute = true)
    private boolean active;

    @JacksonXmlProperty(isAttribute = true)
    private Account.Type type;

    @JacksonXmlProperty
    private String name;

    @JacksonXmlElementWrapper(localName = "children")
    @JacksonXmlProperty(localName = "account")
    private List<AccountXml> children;
}
