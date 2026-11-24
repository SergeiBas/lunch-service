package com.lunch.order.model;

public enum OrderStatus {
    COLLECTING("Збір замовлень"),
    NEW("Нове"),
    ACCEPTED("Прийнято"),
    PREPARING("Готується"),
    ON_THE_WAY("В дорозі"),
    ARRIVED("Прибув"),
    COMPLETED("Виконано"),
    REJECTED("Відхилено");

    private final String label;

    OrderStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}