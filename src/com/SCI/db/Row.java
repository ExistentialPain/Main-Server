package com.SCI.db;

import java.util.HashMap;
import java.util.Map;

public class Row {
    Row() {
        items = new HashMap<>();
    }

    void add(String key, Object item) {
        items.put(key, item);
    }

    public Object get(String key) {
        return items.get(key);
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (Map.Entry<String, Object> item : items.entrySet()) {
            stringBuilder.append(item.getKey()).append(": ").append(item.getValue()).append('\n');
        }
        return stringBuilder.toString();
    }

    public String format(String format) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Object> item : items.entrySet()) {
            sb.append(format.replace("@0", item.getKey()).replace("@1", String.valueOf(item.getValue())));
        }
        return sb.toString();
    }

    private HashMap<String, Object> items;
}
