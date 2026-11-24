package com.lunch.order.service;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
@SessionScope
public class Cart implements Serializable {

    private static final int MAX_QUANTITY = 99;

    private final Map<Long, Integer> items = new LinkedHashMap<>();

    public void add(Long dishId, int quantity) {
        int current = items.getOrDefault(dishId, 0);
        items.put(dishId, clamp(current + Math.max(quantity, 1)));
    }

    public void setQuantity(Long dishId, int quantity) {
        if (!items.containsKey(dishId)) {
            return;
        }
        if (quantity <= 0) {
            items.remove(dishId);
        } else {
            items.put(dishId, clamp(quantity));
        }
    }

    public void remove(Long dishId) {
        items.remove(dishId);
    }

    public void clear() {
        items.clear();
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public int totalCount() {
        return items.values().stream().mapToInt(Integer::intValue).sum();
    }

    /** Копія, щоб можна було безпечно змінювати кошик під час перебору. */
    public Map<Long, Integer> getItems() {
        return new LinkedHashMap<>(items);
    }

    private int clamp(int value) {
        return Math.min(value, MAX_QUANTITY);
    }
}