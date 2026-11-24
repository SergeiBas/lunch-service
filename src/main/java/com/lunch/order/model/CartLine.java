package com.lunch.order.model;

import com.lunch.menu.model.Dish;

public class CartLine {

    private final Dish dish;
    private final int quantity;

    public CartLine(Dish dish, int quantity) {
        this.dish = dish;
        this.quantity = quantity;
    }

    public Dish getDish() { return dish; }
    public int getQuantity() { return quantity; }

    public double getLineTotal() {
        return Math.round(dish.getPrice() * quantity * 100.0) / 100.0;
    }
}