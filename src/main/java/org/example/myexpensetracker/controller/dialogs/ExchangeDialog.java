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

public class ExchangeDialog {
    private Context ctx;

    private final ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
    private DatePicker datePicker = new DatePicker();
    private TextField commentField = new TextField();
    private ChoiceBox<Account> accountChoiceBox;
    private TextField fromAmountField;
    private ChoiceBox<Currency> fromCurrencyChoiceBox;
    private TextField toAmountField;
    private ChoiceBox<Currency> toCurrencyChoiceBox;

    public ExchangeDialog(Context ctx) {
        this.ctx = ctx;

        accountChoiceBox = new ChoiceBox<>(FXCollections.observableList(ctx.getAccounts()));
        accountChoiceBox.setConverter(new SimpleStringConverter<>(Account::getName));
        fromAmountField = new TextField("0");
        fromAmountField.setPromptText("Amount");
        toAmountField = new TextField("0");
        toAmountField.setPromptText("Amount");
        fromCurrencyChoiceBox = new ChoiceBox<>(FXCollections.observableList(ctx.getCurrencies()));
        fromCurrencyChoiceBox.setConverter(new SimpleStringConverter<>(Currency::getCode));
        toCurrencyChoiceBox = new ChoiceBox<>(FXCollections.observableList(ctx.getCurrencies()));
        toCurrencyChoiceBox.setConverter(new SimpleStringConverter<>(Currency::getCode));
        commentField.setPromptText("Comment");
    }

    public Optional<Exchange> showAdd() {
        Dialog<Exchange> dialog = new Dialog<>();
        setupDialog(dialog, "Add Exchange");

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                Amount fromAmount = new Amount(new BigDecimal(fromAmountField.getText()), fromCurrencyChoiceBox.getValue());
                Amount toAmount = new Amount(new BigDecimal(toAmountField.getText()), toCurrencyChoiceBox.getValue());
                Exchange exchange = Exchange.create(ctx.getUserId(), datePicker.getValue(), accountChoiceBox.getValue(),
                        fromAmount, toAmount,
                        commentField.getText());
                return exchange;
            }
            return null;
        });

        return dialog.showAndWait();
    }

    public Optional<Exchange> showEdit(Exchange exchange) {
        Dialog<Exchange> dialog = new Dialog<>();
        setupDialog(dialog, "Edit Exchange");

        datePicker.setValue(exchange.getDate());
        commentField.setText(exchange.getComment());
        accountChoiceBox.setValue(exchange.getAccount());
        fromAmountField.setText(exchange.getFromAmount().getValue().toString());
        fromCurrencyChoiceBox.setValue(exchange.getFromAmount().getCurrency());
        toAmountField.setText(exchange.getToAmount().getValue().toString());
        toCurrencyChoiceBox.setValue(exchange.getToAmount().getCurrency());

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                Amount fromAmount = new Amount(new BigDecimal(fromAmountField.getText()), fromCurrencyChoiceBox.getValue());
                Amount toAmount = new Amount(new BigDecimal(toAmountField.getText()), toCurrencyChoiceBox.getValue());
                exchange.setDate(datePicker.getValue());
                exchange.setComment(commentField.getText());
                exchange.setAccount(accountChoiceBox.getValue());
                exchange.setFromAmount(fromAmount);
                exchange.setToAmount(toAmount);
                return exchange;
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

        grid.add(new Label("Account"), 0, 0);
        grid.add(accountChoiceBox, 1, 0);
        grid.add(new Label("From"), 0, 1);
        grid.add(fromAmountField, 1, 1);
        grid.add(fromCurrencyChoiceBox, 2, 1);
        grid.add(new Label("To"), 0, 2);
        grid.add(toAmountField, 1, 2);
        grid.add(toCurrencyChoiceBox, 2, 2);

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
            BigDecimal fromAmount = new BigDecimal(fromAmountField.getText());
            BigDecimal toAmount = new BigDecimal(toAmountField.getText());
            if (fromAmount.compareTo(BigDecimal.ZERO) <= 0 || toAmount.compareTo(BigDecimal.ZERO) <= 0) {
                return false;
            }
        } catch (NumberFormatException e) {
            return false;
        }
        return datePicker.getValue() != null
                && accountChoiceBox.getValue() != null
                && fromCurrencyChoiceBox.getValue() != null
                && toCurrencyChoiceBox.getValue() != null
                && fromCurrencyChoiceBox.getValue() != toCurrencyChoiceBox.getValue();
    }
}
