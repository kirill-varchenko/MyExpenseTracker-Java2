package org.example.myexpensetracker.controller.dialogs;

import javafx.collections.FXCollections;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import org.example.myexpensetracker.model.Account;
import org.example.myexpensetracker.model.Context;

import java.util.Optional;
import java.util.UUID;

public class AccountDialog {
    private Context ctx;

    private ChoiceBox<Account.Type> typeChoiceBox = new ChoiceBox<>();
    private ComboBox<Account> parentComboBox = new ComboBox<>();
    private TextField nameField = new TextField();
    private CheckBox activeCheckBox = new CheckBox();
    private ButtonType saveButtonType = new ButtonType("Safe", ButtonBar.ButtonData.OK_DONE);

    public AccountDialog(Context ctx) {
        this.ctx = ctx;
    }

    public Optional<Account> show() {
        Dialog<Account> dialog = new Dialog<>();

        setupDialog(dialog, "Add Account");

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                String name = nameField.getText();
                Account.Type type = typeChoiceBox.getValue();
                Account parentItem = parentComboBox.getValue();
                UUID parentId = parentItem == null ? null : parentItem.getId();
                return Account.create(ctx.getUserId(), name, type, parentId);
            }
            return null;
        });

        return dialog.showAndWait();
    }

    public Optional<Account> show(Account account) {
        Dialog<Account> dialog = new Dialog<>();

        setupDialog(dialog, "Edit Account");

        nameField.setText(account.getName());
        typeChoiceBox.getSelectionModel().select(account.getType());
        ctx.findAccountById(account.getParentId()).ifPresent(parentComboBox.getSelectionModel()::select);
        activeCheckBox.setSelected(account.isActive());

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                account.setName(nameField.getText());
                account.setType(typeChoiceBox.getValue());
                Account parentItem = parentComboBox.getValue();
                account.setParentId(parentItem == null ? null : parentItem.getId());
                account.setActive(activeCheckBox.isSelected());
                return account;
            }
            return null;
        });

        return dialog.showAndWait();
    }

    private void setupDialog(Dialog dialog, String title) {
        dialog.setTitle(title);

        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        nameField.setPromptText("Name");

        typeChoiceBox.setItems(FXCollections.observableArrayList(Account.Type.values()));
        typeChoiceBox.getSelectionModel().selectFirst();

        parentComboBox.getItems().add(null);
        parentComboBox.getItems().addAll(ctx.getAccounts());
        parentComboBox.setButtonCell(new ParentListCell());
        parentComboBox.setCellFactory(param -> new ParentListCell());
        parentComboBox.getSelectionModel().selectFirst();

        activeCheckBox = new CheckBox();
        activeCheckBox.setAllowIndeterminate(false);
        activeCheckBox.setSelected(true);

        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Type:"), 0, 1);
        grid.add(typeChoiceBox, 1, 1);
        grid.add(new Label("Parent:"), 0, 2);
        grid.add(parentComboBox, 1, 2);
        grid.add(new Label("Active:"), 0, 3);
        grid.add(activeCheckBox, 1, 3);

        dialog.getDialogPane().setContent(grid);
    }

    static class ParentListCell extends ListCell<Account> {
        @Override
        protected void updateItem(Account item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText("None");
            } else {
                setText(item.getName());
            }
        }
    }
}
