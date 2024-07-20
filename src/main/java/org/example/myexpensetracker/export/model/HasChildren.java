package org.example.myexpensetracker.export.model;

import java.util.List;

public interface HasChildren<T> {
    List<T> getChildren();
    void setChildren(List<T> children);
}
