package com.lunch.order.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class GroupRoomForm {

    @NotBlank(message = "Вкажіть назву")
    @Size(max = 100, message = "Назва занадто довга")
    private String title;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
}