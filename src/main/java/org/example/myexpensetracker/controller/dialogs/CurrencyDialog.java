package org.example.myexpensetracker.controller.dialogs;

import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import org.example.myexpensetracker.model.Context;
import org.example.myexpensetracker.model.Currency;

import java.util.Optional;

public class CurrencyDialog {
    private Context ctx;

    private ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);

    private TextField nameField = new TextField();
    private TextField codeField = new TextField();
    private TextField symbolField = new TextField();
    private CheckBox activeCheckBox = new CheckBox();

    public CurrencyDialog(Context ctx) {
        this.ctx = ctx;
    }

    public Optional<Currency> show() {
        Dialog<Currency> dialog = new Dialog<>();
        setupDialog(dialog, "Add Currency");

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                String name = nameField.getText();
                String code = codeField.getText();
                String symbol = symbolField.getText();
                return Currency.create(ctx.getUserId(), name, code, symbol);
            }
            return null;
        });

        return dialog.showAndWait();
    }

    public Optional<Currency> show(Currency currency) {
        Dialog<Currency> dialog = new Dialog<>();
        setupDialog(dialog, "Edit Currency");

        nameField.setText(currency.getName());
        codeField.setText(currency.getCode());
        symbolField.setText(currency.getSymbol());
        activeCheckBox.setSelected(currency.isActive());

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                currency.setName(nameField.getText());
                currency.setCode(codeField.getText());
                currency.setSymbol(symbolField.getText());
                currency.setActive(activeCheckBox.isSelected());
                return currency;
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
        codeField.setPromptText("Code");
        symbolField.setPromptText("Symbol");
        activeCheckBox.setAllowIndeterminate(false);
        activeCheckBox.setSelected(true);

        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Code:"), 0, 1);
        grid.add(codeField, 1, 1);
        grid.add(new Label("Symbol:"), 0, 2);
        grid.add(symbolField, 1, 2);
        grid.add(new Label("Active:"), 0, 3);
        grid.add(activeCheckBox, 1, 3);

        dialog.getDialogPane().setContent(grid);
    }
}
