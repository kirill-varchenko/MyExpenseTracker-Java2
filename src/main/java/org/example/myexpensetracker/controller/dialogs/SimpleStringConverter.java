package org.example.myexpensetracker.controller.dialogs;

import javafx.util.StringConverter;

import java.util.function.Function;

public class SimpleStringConverter<T> extends StringConverter<T> {
    Function<T, String> stringifier;

    public SimpleStringConverter(Function<T, String> stringifier) {
        this.stringifier = stringifier;
    }

    @Override
    public String toString(T object) {
        if (object == null) {
            return "<null>";
        }
        return stringifier.apply(object);
    }

    @Override
    public T fromString(String string) {
        return null;
    }
}