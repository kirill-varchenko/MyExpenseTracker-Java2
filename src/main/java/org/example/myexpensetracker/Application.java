package org.example.myexpensetracker;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.myexpensetracker.common.Config;
import org.example.myexpensetracker.common.IOUtils;
import org.example.myexpensetracker.db.DBConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class Application extends javafx.application.Application {
    private static final Logger logger = LoggerFactory.getLogger(Application.class);

    @Override
    public void start(Stage stage) throws IOException {
        logger.info("Application starts");
        initDb();
        FXMLLoader fxmlLoader = new FXMLLoader(Application.class.getResource("/login-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("MyExpenseTracker");
        stage.setScene(scene);
        stage.show();
    }

    private void initDb() throws IOException {
        logger.info("DB init");
        Config config = Config.getInstance();
        String initScriptPath = config.getProperty("initSql");
        InputStream stream = Application.class.getResourceAsStream(initScriptPath);
        String initScript = IOUtils.readStream(stream);
        Connection conn = DBConnection.getInstance().connection();
        try {
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(initScript);
        } catch (SQLException e) {
            logger.error("Error while DB init: {}", e.toString());
            throw new RuntimeException(e);
        }
        logger.info("DB init done");
    }

    public static void main(String[] args) {
        launch();
    }
}