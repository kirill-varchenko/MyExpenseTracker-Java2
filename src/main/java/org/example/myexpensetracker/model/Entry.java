package org.example.myexpensetracker.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Entry {
    private Account account;
    private Amount amount;
    private Category category;
    private String comment;
    private List<Tag> tags = new ArrayList<>();

    public boolean isAmountPositive() {
        return amount.isPositive();
    }
}
