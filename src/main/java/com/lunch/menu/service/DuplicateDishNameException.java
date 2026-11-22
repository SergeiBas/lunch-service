package com.lunch.menu.service;

public class DuplicateDishNameException extends RuntimeException {
    public DuplicateDishNameException(String name) {
        super("Dish already exists: " + name);
    }
}