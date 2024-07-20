package org.example.myexpensetracker.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.example.myexpensetracker.dao.UserDAO;
import org.example.myexpensetracker.dao.sql.UserSQLDAO;
import org.example.myexpensetracker.model.Context;
import org.example.myexpensetracker.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;

public class LoginController {
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    private final UserDAO userDAO = new UserSQLDAO();

    @FXML
    private ListView<User> userList;

    @FXML
    public void initialize() {
        logger.info("Init LoginController");
        userList.setItems(FXCollections.observableList(userDAO.findAll()));
        userList.getSelectionModel().selectFirst();
        userList.setCellFactory(new Callback<ListView<User>, ListCell<User>>() {
            @Override
            public ListCell<User> call(ListView<User> param) {
                return new ListCell<User>() {
                    @Override
                    protected void updateItem(User user, boolean empty) {
                        super.updateItem(user, empty);
                        if (empty || user == null) {
                            setText(null);
                        } else {
                            setText(user.getUsername());
                        }
                    }
                };
            }
        });
    }

    @FXML
    void onCancel(ActionEvent event) {
        logger.info("Login cancelled");
        Platform.exit();
    }

    @FXML
    void onLogin(ActionEvent event) {
        switchToMainView(userList.getSelectionModel().getSelectedItem());
    }

    @FXML
    void onRegister(ActionEvent event) {
        Dialog<String> dialog = createRegisterDialog();
        Optional<String> maybeUsername = dialog.showAndWait();
        maybeUsername.ifPresent(username -> {
            User user = User.create(username);
            userDAO.add(user);
            switchToMainView(user);
        });
    }

    private void switchToMainView(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/main-view.fxml"));
            Parent mainView = loader.load();

            MainController mainController = loader.getController();

            Optional<User> maybeUser = userDAO.getById(user.getId());

            maybeUser.ifPresent(fullUser -> {
                mainController.setContext(new Context(fullUser));

                Stage stage = (Stage) userList.getScene().getWindow();
                stage.setScene(new Scene(mainView));
            });
        } catch (IOException e) {
            logger.error("Failed to switch to main view: {}", e.toString());
        }
    }

    private Dialog<String> createRegisterDialog() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Register");

        ButtonType registerButtonType = new ButtonType("Register", ButtonBar.ButtonData.APPLY);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, registerButtonType);

        TextField newUsernameField = new TextField();
        newUsernameField.setPromptText("New username");

        dialog.getDialogPane().setContent(newUsernameField);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == registerButtonType) {
                String newUsername = newUsernameField.getText();
                return newUsername.isBlank() ? null : newUsername;
            }
            return null;
        });

        return dialog;
    }
}
