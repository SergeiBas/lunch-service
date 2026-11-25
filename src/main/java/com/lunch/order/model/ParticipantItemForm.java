package com.lunch.order.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

public class ParticipantItemForm {

    @NotBlank(message = "Вкажіть ваше ім'я")
    @Size(max = 60, message = "Ім'я занадто довге")
    private String participantName;

    @NotNull(message = "Оберіть страву")
    private Long dishId;

    @NotNull(message = "Вкажіть кількість")
    @Min(value = 1, message = "Мінімум 1")
    @Max(value = 20, message = "Максимум 20")
    private Integer quantity = 1;

    private List<Long> allergenIds = new ArrayList<>();

    @Size(max = 255, message = "Побажання занадто довгі")
    private String wishes;

    public String getParticipantName() { return participantName; }
    public void setParticipantName(String participantName) { this.participantName = participantName; }
    public Long getDishId() { return dishId; }
    public void setDishId(Long dishId) { this.dishId = dishId; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public List<Long> getAllergenIds() { return allergenIds; }
    public void setAllergenIds(List<Long> allergenIds) { this.allergenIds = allergenIds; }
    public String getWishes() { return wishes; }
    public void setWishes(String wishes) { this.wishes = wishes; }
}