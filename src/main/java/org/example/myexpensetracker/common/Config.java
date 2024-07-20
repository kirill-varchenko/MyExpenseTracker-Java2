package org.example.myexpensetracker.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Config {
    private static final Logger logger = LoggerFactory.getLogger(Config.class);
    private static Config instance;
    private static final String propertiesFile = "/config.xml";
    private final Properties properties;

    private Config() {
        properties = new Properties();
        InputStream inputStream = Config.class.getResourceAsStream(propertiesFile);
        try {
            properties.loadFromXML(inputStream);
            logger.info("Config loaded from: {}", propertiesFile);
        } catch (Exception e) {
            logger.error("Error while loading config: {}", propertiesFile);
            throw new RuntimeException(e);
        }
    }

    public static Config getInstance() {
        if (instance == null) {
            instance = new Config();
        }
        return instance;
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    public void setProperty(String key, String value) {
        properties.setProperty(key, value);
        save();
    }

    private void save() {
        try {
            properties.storeToXML(new FileOutputStream(Config.class.getResource(propertiesFile).getPath()), null);
            logger.debug("Config saved to: {}", propertiesFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
