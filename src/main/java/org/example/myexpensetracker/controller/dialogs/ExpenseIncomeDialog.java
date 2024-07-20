package org.example.myexpensetracker.controller.dialogs;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import org.controlsfx.control.CheckComboBox;
import org.example.myexpensetracker.model.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class ExpenseIncomeDialog {
    private Context ctx;

    private final ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);

    private DatePicker datePicker = new DatePicker();
    private TextField commentField = new TextField();
    private GridPane grid = new GridPane();

    private List<GridRow> gridRows = new ArrayList<>();

    private Currency defaultCurrency;
    private Account defaultAccount;

    public ExpenseIncomeDialog(Context ctx) {
        this.ctx = ctx;

        if (ctx.getUser().getProfile().getDefaultCurrencyId() != null) {
            ctx.findCurrencyById(ctx.getUser().getProfile().getDefaultCurrencyId()).ifPresent(currency -> {defaultCurrency = currency;});
        }
        if (ctx.getUser().getProfile().getDefaultAccountId() != null) {
            ctx.findAccountById(ctx.getUser().getProfile().getDefaultAccountId()).ifPresent(account -> {defaultAccount = account;});
        }
    }

    class GridRow {
        private int row = -1;

        private ChoiceBox<Account> accountChoiceBox;
        private TextField amountField;
        private ChoiceBox<Currency> currencyChoiceBox;
        private ChoiceBox<Category> categoryChoiceBox;
        private TextField commentField;
        private CheckComboBox<Tag> tagCheckComboBox;
        private ButtonBar buttonBar;
        private Button deleteButton;

        public GridRow() {
            accountChoiceBox = new ChoiceBox<>(FXCollections.observableList(ctx.getAccounts()));
            accountChoiceBox.setConverter(new SimpleStringConverter<>(Account::getName));
            if (defaultAccount != null) {
                accountChoiceBox.getSelectionModel().select(defaultAccount);
            }
            amountField = new TextField("0");
            amountField.setPromptText("Amount");
            currencyChoiceBox = new ChoiceBox<>(FXCollections.observableList(ctx.getCurrencies()));
            currencyChoiceBox.setConverter(new SimpleStringConverter<>(Currency::getCode));
            if (defaultCurrency != null) {
                currencyChoiceBox.getSelectionModel().select(defaultCurrency);
            }
            categoryChoiceBox = new ChoiceBox<>(FXCollections.observableList(ctx.getCategories()));
            categoryChoiceBox.getItems().add(0, null);
            categoryChoiceBox.setConverter(new SimpleStringConverter<>(Category::getName));
            commentField = new TextField();
            commentField.setPromptText("Comment");
            tagCheckComboBox = new CheckComboBox<>(FXCollections.observableList(ctx.getTags()));
            tagCheckComboBox.setConverter(new SimpleStringConverter<>(Tag::getName));

            deleteButton = new Button("del");
            buttonBar = new ButtonBar();
            buttonBar.getButtons().addAll(deleteButton);
        }

        public void putOnGrid(GridPane grid, int row) {
            this.row = row;
            grid.add(accountChoiceBox, 0, row);
            grid.add(amountField, 1, row);
            grid.add(currencyChoiceBox, 2, row);
            grid.add(categoryChoiceBox, 3, row);
            grid.add(tagCheckComboBox, 4, row);
            grid.add(commentField, 5, row);
            grid.add(buttonBar, 6, row);
        }

        public void removeFromGrid(GridPane grid) {
            grid.getChildren().removeAll(accountChoiceBox, amountField, currencyChoiceBox, categoryChoiceBox, commentField, tagCheckComboBox, buttonBar);
        }

        public void setOnDelete(Consumer<Integer> onDelete) {
            deleteButton.setOnAction(event -> {
                if (row != -1) {
                    onDelete.accept(row);
                }
            });
        }

        public boolean isValid() {
            try {
                BigDecimal amount = new BigDecimal(amountField.getText());
                if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                    return false;
                }
            } catch (NumberFormatException e) {
                return false;
            }
            return accountChoiceBox.getValue() != null && currencyChoiceBox.getValue() != null;
        }

        public Entry getEntry() {
            Amount amount = new Amount(new BigDecimal(amountField.getText()), currencyChoiceBox.getValue());
            return new Entry(accountChoiceBox.getValue(),
                    amount,
                    categoryChoiceBox.getValue(),
                    commentField.getText(),
                    tagCheckComboBox.getCheckModel().getCheckedItems());
        }

        public void setEntry(Entry entry) {
            accountChoiceBox.setValue(entry.getAccount());
            amountField.setText(entry.getAmount().getValue().toString());
            currencyChoiceBox.setValue(entry.getAmount().getCurrency());
            categoryChoiceBox.setValue(entry.getCategory());
            commentField.setText(entry.getComment());
            tagCheckComboBox.getCheckModel().clearChecks();
            for (Tag tag : entry.getTags()) {
                tagCheckComboBox.getCheckModel().check(tag);
            }
        }

        public void setAccount(Account account) {
            accountChoiceBox.setValue(account);
        }

        public void setCurrency(Currency currency) {
            currencyChoiceBox.setValue(currency);
        }

    }

    public Optional<Expense> showAddExpense() {
        Dialog<Expense> dialog = new Dialog<>();
        setupDialog(dialog, "Add Expense");

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                Expense expense = Expense.create(ctx.getUserId(), datePicker.getValue(), commentField.getText());
                for (GridRow row : gridRows) {
                    expense.addEntry(row.getEntry());
                }
                return expense;
            }
            return null;
        });

        return dialog.showAndWait();
    }

    public Optional<Income> showAddIncome() {
        Dialog<Income> dialog = new Dialog<>();
        setupDialog(dialog, "Add Income");

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                Income income = Income.create(ctx.getUserId(), datePicker.getValue(), commentField.getText());
                for (GridRow row : gridRows) {
                    income.addEntry(row.getEntry());
                }
                return income;
            }
            return null;
        });

        return dialog.showAndWait();
    }

    public Optional<Expense> showEdit(Expense expense) {
        Dialog<Expense> dialog = new Dialog<>();
        setupDialog(dialog, "Edit Expense");

        datePicker.setValue(expense.getDate());
        commentField.setText(expense.getComment());

        for (Entry entry : expense.getEntries()) {
            addRow(entry);
        }

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                expense.setDate(datePicker.getValue());
                expense.setComment(commentField.getText());
                expense.getEntries().clear();
                for (GridRow row : gridRows) {
                    expense.addEntry(row.getEntry());
                }
                return expense;
            }
            return null;
        });

        return dialog.showAndWait();
    }

    public Optional<Income> showEdit(Income income) {
        Dialog<Income> dialog = new Dialog<>();
        setupDialog(dialog, "Edit Income");

        datePicker.setValue(income.getDate());
        commentField.setText(income.getComment());

        for (Entry entry : income.getEntries()) {
            addRow(entry);
        }

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                income.setDate(datePicker.getValue());
                income.setComment(commentField.getText());
                income.getEntries().clear();
                for (GridRow row : gridRows) {
                    income.addEntry(row.getEntry());
                }
                return income;
            }
            return null;
        });

        return dialog.showAndWait();
    }

    private void setupDialog(Dialog dialog, String title) {
        // Create a custom dialog
        dialog.setTitle(title);
        dialog.setResizable(true);

        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        BorderPane pane = new BorderPane();

        ToolBar toolBar = new ToolBar();
        datePicker.setValue(LocalDate.now());
        commentField.setPromptText("Comment");
        Button addEntry = new Button("Add entry");
        addEntry.setOnAction(event -> addRow());
        toolBar.getItems().addAll(datePicker, commentField, addEntry);

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setPrefHeight(200);
        scrollPane.setMinWidth(400);
        scrollPane.setContent(grid);

        Label messageLabel = new Label();

        pane.setTop(toolBar);
        pane.setCenter(scrollPane);
        pane.setBottom(messageLabel);
        pane.setPrefWidth(800);

        dialog.getDialogPane().setContent(pane);

        Button saveButton = (Button) dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.addEventFilter(ActionEvent.ACTION, event -> {
            for (int i = 0; i < gridRows.size(); i ++) {
                if (!gridRows.get(i).isValid()) {
                    messageLabel.setText("Row " + (i + 1) + " is not valid");
                    event.consume();
                    return;
                }
            }
            if (datePicker.getValue() == null) {
                messageLabel.setText("Pick a date");
                event.consume();
            } else if (gridRows.size() == 0) {
                messageLabel.setText("Enter entries");
                event.consume();
            }
        });
    }

    private GridRow addRow() {
        GridRow newRow = new GridRow();
        newRow.putOnGrid(grid, gridRows.size());
        gridRows.add(newRow);

        newRow.setOnDelete(row -> {
            gridRows.remove(row.intValue());
            newRow.removeFromGrid(grid);
        });
        return newRow;
    }

    private GridRow addRow(Entry entry) {
        GridRow newRow = addRow();
        newRow.setEntry(entry);
        return newRow;
    }
}

