package org.example.myexpensetracker.export.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class CategoryXml implements HasChildren<CategoryXml> {
    @JacksonXmlProperty(isAttribute = true)
    private UUID id;

    @JacksonXmlProperty(isAttribute = true)
    private boolean active;

    @JacksonXmlProperty
    private String name;

    @JacksonXmlElementWrapper(localName = "children")
    @JacksonXmlProperty(localName = "category")
    private List<CategoryXml> children;
}
