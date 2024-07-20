package org.example.myexpensetracker.export.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;

import java.util.UUID;

@Data
public class UserXml {
    @JacksonXmlProperty(isAttribute = true)
    private UUID id;

    @JacksonXmlProperty
    private String username;
}
