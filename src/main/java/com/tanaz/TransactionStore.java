package com.tanaz;

import java.util.ArrayList;
import java.util.List;

public class TransactionStore<T> {
    private List<T> items = new ArrayList<>();

    public void add(T item) {
        items.add(item);
    }
    public List<T> getAll() {
        return new ArrayList<>(items);
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public int size() {
        return items.size();
    }
}
