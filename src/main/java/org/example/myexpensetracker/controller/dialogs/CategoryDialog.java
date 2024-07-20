package org.example.myexpensetracker.controller.dialogs;

import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import org.example.myexpensetracker.model.Category;
import org.example.myexpensetracker.model.Context;

import java.util.Optional;
import java.util.UUID;

public class CategoryDialog {
    private Context ctx;

    private TextField nameField = new TextField();
    private ComboBox<Category> parentComboBox = new ComboBox<>();
    private CheckBox activeCheckBox = new CheckBox();

    private ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);

    public CategoryDialog(Context ctx) {
        this.ctx = ctx;
    }

    public Optional<Category> show() {
        Dialog<Category> dialog = new Dialog<>();
        setupDialog(dialog, "Add Category");

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                String name = nameField.getText();
                Category parentItem = parentComboBox.getValue();
                UUID parentId = parentItem == null ? null : parentItem.getId();
                return Category.create(ctx.getUserId(), name, parentId);
            }
            return null;
        });

        return dialog.showAndWait();
    }

    public Optional<Category> show(Category category) {
        Dialog<Category> dialog = new Dialog<>();
        setupDialog(dialog, "Edit Category");

        nameField.setText(category.getName());
        ctx.findCategoryById(category.getParentId()).ifPresent(parentComboBox.getSelectionModel()::select);
        activeCheckBox.setSelected(category.isActive());

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                category.setName(nameField.getText());
                Category parentItem = parentComboBox.getValue();
                category.setParentId(parentItem == null ? null : parentItem.getId());
                category.setActive(activeCheckBox.isSelected());
                return category;
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

        parentComboBox.getItems().add(null);
        parentComboBox.getItems().addAll(ctx.getCategories());
        parentComboBox.setButtonCell(new ParentListCell());
        parentComboBox.setCellFactory(param -> new ParentListCell());
        parentComboBox.getSelectionModel().selectFirst();

        activeCheckBox.setAllowIndeterminate(false);
        activeCheckBox.setSelected(true);

        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Parent:"), 0, 1);
        grid.add(parentComboBox, 1, 1);
        grid.add(new Label("Active:"), 0, 2);
        grid.add(activeCheckBox, 1, 2);

        dialog.getDialogPane().setContent(grid);
    }

    static class ParentListCell extends ListCell<Category> {
        @Override
        protected void updateItem(Category item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText("None");
            } else {
                setText(item.getName());
            }
        }
    }
}
