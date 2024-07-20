package org.example.myexpensetracker.controller.dialogs;

import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import org.example.myexpensetracker.model.Account;
import org.example.myexpensetracker.model.Context;
import org.example.myexpensetracker.model.Currency;
import org.example.myexpensetracker.model.User;

import java.util.Optional;

public class ProfileDialog {
    private Context ctx;

    public ProfileDialog(Context ctx) {
        this.ctx = ctx;
    }

    public Optional<User.Profile> show(User.Profile profile) {
        Dialog<User.Profile> dialog = new Dialog<>();
        dialog.setTitle("Edit profile");

        ButtonType saveButtonType = new ButtonType("Safe", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        ChoiceBox<Currency> baseCurrencyChoiceBox = new ChoiceBox<>();
        baseCurrencyChoiceBox.setConverter(new SimpleStringConverter<>(Currency::getCode));
        baseCurrencyChoiceBox.getItems().add(null);
        baseCurrencyChoiceBox.getItems().addAll(ctx.getCurrencies());
        if (profile.getBaseCurrencyId() != null) {
            ctx.findCurrencyById(profile.getBaseCurrencyId()).ifPresent(currency -> baseCurrencyChoiceBox.getSelectionModel().select(currency));
        } else {
            baseCurrencyChoiceBox.getSelectionModel().selectFirst();
        }

        ChoiceBox<Currency> defaultCurrencyChoiceBox = new ChoiceBox<>();
        defaultCurrencyChoiceBox.setConverter(new SimpleStringConverter<>(Currency::getCode));
        defaultCurrencyChoiceBox.getItems().add(null);
        defaultCurrencyChoiceBox.getItems().addAll(ctx.getCurrencies());
        if (profile.getDefaultCurrencyId() != null) {
            ctx.findCurrencyById(profile.getDefaultCurrencyId()).ifPresent(currency -> defaultCurrencyChoiceBox.getSelectionModel().select(currency));
        } else {
            defaultCurrencyChoiceBox.getSelectionModel().selectFirst();
        }

        ChoiceBox<Account> defaultAccountChoiceBox = new ChoiceBox<>();
        defaultAccountChoiceBox.setConverter(new SimpleStringConverter<>(Account::getName));
        defaultAccountChoiceBox.getItems().add(null);
        defaultAccountChoiceBox.getItems().addAll(ctx.getAccounts());
        if (profile.getDefaultAccountId() != null) {
            ctx.findAccountById(profile.getDefaultAccountId()).ifPresent(currency -> defaultAccountChoiceBox.getSelectionModel().select(currency));
        } else {
            defaultAccountChoiceBox.getSelectionModel().selectFirst();
        }

        grid.add(new Label("Base Currency:"), 0, 0);
        grid.add(baseCurrencyChoiceBox, 1, 0);
        grid.add(new Label("Default Currency:"), 0, 1);
        grid.add(defaultCurrencyChoiceBox, 1, 1);
        grid.add(new Label("Default Account:"), 0, 2);
        grid.add(defaultAccountChoiceBox, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                profile.setBaseCurrencyId(baseCurrencyChoiceBox.getValue().getId());
                profile.setDefaultCurrencyId(defaultCurrencyChoiceBox.getValue().getId());
                profile.setDefaultAccountId(defaultAccountChoiceBox.getValue().getId());
                return profile;
            }
            return null;
        });

        return dialog.showAndWait();
    }
}
