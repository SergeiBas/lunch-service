package com.lunch.order.service;

public class RoomClosedException extends RuntimeException {
    public RoomClosedException() {
        super("Збір замовлень у цій кімнаті вже закрито");
    }
}