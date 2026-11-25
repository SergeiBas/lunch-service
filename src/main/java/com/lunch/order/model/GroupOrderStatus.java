package com.lunch.order.model;

public enum GroupOrderStatus {
    OPEN("Збір відкрито"),
    CLOSED("Збір закрито");

    private final String label;

    GroupOrderStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}