package org.example.myexpensetracker.controller.dialogs;

import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import org.example.myexpensetracker.model.Context;
import org.example.myexpensetracker.model.Tag;

import java.util.Optional;

public class TagDialog {
    private Context ctx;

    private ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);

    private TextField nameField = new TextField();
    private CheckBox activeCheckBox = new CheckBox();

    public TagDialog(Context ctx) {
        this.ctx = ctx;
    }

    public Optional<Tag> show() {
        Dialog<Tag> dialog = new Dialog<>();
        setupDialog(dialog, "Add Tag");

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                String name = nameField.getText();
                return Tag.create(ctx.getUserId(), name);
            }
            return null;
        });

        return dialog.showAndWait();
    }

    public Optional<Tag> show(Tag tag) {
        Dialog<Tag> dialog = new Dialog<>();
        setupDialog(dialog, "Edit Tag");

        nameField.setText(tag.getName());
        activeCheckBox.setSelected(tag.isActive());

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                tag.setName(nameField.getText());
                tag.setActive(activeCheckBox.isSelected());
                return tag;
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
        activeCheckBox.setAllowIndeterminate(false);
        activeCheckBox.setSelected(true);

        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Active:"), 0, 1);
        grid.add(activeCheckBox, 1, 1);

        dialog.getDialogPane().setContent(grid);
    }
}
