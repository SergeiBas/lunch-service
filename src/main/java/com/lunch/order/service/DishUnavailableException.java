package com.lunch.order.service;

public class DishUnavailableException extends RuntimeException {
    public DishUnavailableException(String message) {
        super(message);
    }
}