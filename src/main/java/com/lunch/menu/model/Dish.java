package com.lunch.menu.model;

import jakarta.persistence.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "dishes")
public class Dish {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;
    private String composition;

    @Column(nullable = false)
    private double price;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(name = "diet_labels")
    private String dietLabels;

    @Column(nullable = false)
    private boolean available = true;

    @Column(nullable = false)
    private boolean archived = false;

    @Column(name = "image_url")
    private String imageUrl;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "dish_allergens",
            joinColumns = @JoinColumn(name = "dish_id"),
            inverseJoinColumns = @JoinColumn(name = "allergen_id"))
    private Set<Allergen> allergens = new HashSet<>();

    /** Мітки з рядка «веганське,безглютенове» у список (для шаблонів і фільтрів). */
    public List<String> getDietLabelList() {
        if (dietLabels == null || dietLabels.isBlank()) {
            return List.of();
        }
        return Arrays.stream(dietLabels.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getComposition() { return composition; }
    public void setComposition(String composition) { this.composition = composition; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }
    public String getDietLabels() { return dietLabels; }
    public void setDietLabels(String dietLabels) { this.dietLabels = dietLabels; }
    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }
    public boolean isArchived() { return archived; }
    public void setArchived(boolean archived) { this.archived = archived; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public Set<Allergen> getAllergens() { return allergens; }
    public void setAllergens(Set<Allergen> allergens) { this.allergens = allergens; }

    // потрібно для Set<Dish> в улюблених користувача
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Dish other)) return false;
        return id != null && id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return 31;
    }
}