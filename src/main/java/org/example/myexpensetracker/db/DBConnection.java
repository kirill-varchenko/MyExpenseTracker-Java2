package org.example.myexpensetracker.db;

import org.example.myexpensetracker.common.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static final Logger logger = LoggerFactory.getLogger(DBConnection.class);

    private static DBConnection instance;

    private Connection conn;

    private DBConnection() {
        try {
            String connectionStr = Config.getInstance().getProperty("dbConnection");
            conn = DriverManager.getConnection(connectionStr);
            logger.info("Connected to db: {}", connectionStr);
        } catch (SQLException e) {
            logger.error("Error while getting connection: {}", e.toString());
        }
    }

    public static DBConnection getInstance() {
        if (instance == null) {
            instance = new DBConnection();
        }
        return instance;
    }

    public Connection connection() {
        return conn;
    }
}
