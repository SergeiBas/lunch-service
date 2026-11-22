package com.lunch.menu.model;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

public class DishForm {

    private Long id;

    @NotBlank(message = "Вкажіть назву страви")
    @Size(max = 100, message = "Назва занадто довга")
    private String name;

    @Size(max = 500, message = "Опис занадто довгий")
    private String description;

    @Size(max = 500, message = "Склад занадто довгий")
    private String composition;

    @NotNull(message = "Вкажіть ціну")
    @DecimalMin(value = "0.0", inclusive = false, message = "Ціна має бути більшою за 0")
    private Double price;

    @NotNull(message = "Оберіть категорію")
    private Long categoryId;

    @Size(max = 255, message = "Занадто довгий текст")
    private String dietLabels;

    private List<Long> allergenIds = new ArrayList<>();

    private boolean available = true;

    @Size(max = 255, message = "Занадто довге посилання")
    private String imageUrl;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getComposition() { return composition; }
    public void setComposition(String composition) { this.composition = composition; }
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public String getDietLabels() { return dietLabels; }
    public void setDietLabels(String dietLabels) { this.dietLabels = dietLabels; }
    public List<Long> getAllergenIds() { return allergenIds; }
    public void setAllergenIds(List<Long> allergenIds) { this.allergenIds = allergenIds; }
    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}