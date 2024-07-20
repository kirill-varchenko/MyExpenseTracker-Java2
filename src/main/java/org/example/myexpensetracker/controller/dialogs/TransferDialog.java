package org.example.myexpensetracker.controller.dialogs;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import org.example.myexpensetracker.model.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

public class TransferDialog {
    private Context ctx;

    private final ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
    private DatePicker datePicker = new DatePicker();
    private TextField commentField = new TextField();
    private ChoiceBox<Account> fromAccountChoiceBox;
    private ChoiceBox<Account> toAccountChoiceBox;
    private TextField amountField;
    private ChoiceBox<Currency> currencyChoiceBox;

    public TransferDialog(Context ctx) {
        this.ctx = ctx;

        fromAccountChoiceBox = new ChoiceBox<>(FXCollections.observableList(ctx.getAccounts()));
        fromAccountChoiceBox.setConverter(new SimpleStringConverter<>(Account::getName));
        toAccountChoiceBox = new ChoiceBox<>(FXCollections.observableList(ctx.getAccounts()));
        toAccountChoiceBox.setConverter(new SimpleStringConverter<>(Account::getName));
        amountField = new TextField("0");
        amountField.setPromptText("Amount");
        currencyChoiceBox = new ChoiceBox<>(FXCollections.observableList(ctx.getCurrencies()));
        currencyChoiceBox.setConverter(new SimpleStringConverter<>(Currency::getCode));
        commentField.setPromptText("Comment");
    }

    public Optional<Transfer> showAdd() {
        Dialog<Transfer> dialog = new Dialog<>();
        setupDialog(dialog, "Add Transfer");

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                Amount amount = new Amount(new BigDecimal(amountField.getText()), currencyChoiceBox.getValue());
                Transfer transfer = Transfer.create(ctx.getUserId(), datePicker.getValue(),
                        fromAccountChoiceBox.getValue(), toAccountChoiceBox.getValue(),
                        amount, commentField.getText());
                return transfer;
            }
            return null;
        });

        return dialog.showAndWait();
    }

    public Optional<Transfer> showEdit(Transfer transfer) {
        Dialog<Transfer> dialog = new Dialog<>();
        setupDialog(dialog, "Edit Transfer");

        datePicker.setValue(transfer.getDate());
        commentField.setText(transfer.getComment());
        fromAccountChoiceBox.setValue(transfer.getFromAccount());
        toAccountChoiceBox.setValue(transfer.getToAccount());
        amountField.setText(transfer.getAmount().getValue().toString());
        currencyChoiceBox.setValue(transfer.getAmount().getCurrency());

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                Amount amount = new Amount(new BigDecimal(amountField.getText()), currencyChoiceBox.getValue());
                transfer.setDate(datePicker.getValue());
                transfer.setComment(commentField.getText());
                transfer.setFromAccount(fromAccountChoiceBox.getValue());
                transfer.setToAccount(toAccountChoiceBox.getValue());
                transfer.setAmount(amount);
                return transfer;
            }
            return null;
        });

        return dialog.showAndWait();
    }

    private void setupDialog(Dialog dialog, String title) {
        dialog.setTitle(title);
        dialog.setResizable(true);

        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        BorderPane pane = new BorderPane();

        ToolBar toolBar = new ToolBar();
        datePicker.setValue(LocalDate.now());
        commentField.setPromptText("Comment");
        toolBar.getItems().addAll(datePicker, commentField);

        GridPane grid = new GridPane();

        grid.add(amountField, 0, 0);
        grid.add(currencyChoiceBox, 1, 0);
        grid.add(new Label("From"), 0, 1);
        grid.add(fromAccountChoiceBox, 1, 1);
        grid.add(new Label("To"), 0, 2);
        grid.add(toAccountChoiceBox, 1, 2);

        Label messageLabel = new Label();

        pane.setTop(toolBar);
        pane.setCenter(grid);
        pane.setBottom(messageLabel);
        pane.setPrefWidth(800);

        dialog.getDialogPane().setContent(pane);

        Button saveButton = (Button) dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.addEventFilter(ActionEvent.ACTION, event -> {
            if (!isValid()) {
                messageLabel.setText("Invalid input");
                event.consume();
            }
        });
    }

    private boolean isValid() {
        try {
            BigDecimal amount = new BigDecimal(amountField.getText());
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                return false;
            }
        } catch (NumberFormatException e) {
            return false;
        }
        return datePicker.getValue() != null
                && fromAccountChoiceBox.getValue() != null
                && toAccountChoiceBox.getValue() != null
                && currencyChoiceBox.getValue() != null
                && fromAccountChoiceBox.getValue() != toAccountChoiceBox.getValue();
    }
}
