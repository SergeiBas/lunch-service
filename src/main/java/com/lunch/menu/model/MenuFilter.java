package com.lunch.menu.model;

import java.util.ArrayList;
import java.util.List;

public class MenuFilter {

    private Long categoryId;
    private Double minPrice;
    private Double maxPrice;
    private List<Long> excludeAllergenIds = new ArrayList<>();
    private String diet;
    private boolean favoritesOnly;

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public Double getMinPrice() { return minPrice; }
    public void setMinPrice(Double minPrice) { this.minPrice = minPrice; }
    public Double getMaxPrice() { return maxPrice; }
    public void setMaxPrice(Double maxPrice) { this.maxPrice = maxPrice; }
    public List<Long> getExcludeAllergenIds() { return excludeAllergenIds; }
    public void setExcludeAllergenIds(List<Long> excludeAllergenIds) { this.excludeAllergenIds = excludeAllergenIds; }
    public String getDiet() { return diet; }
    public void setDiet(String diet) { this.diet = diet; }
    public boolean isFavoritesOnly() { return favoritesOnly; }
    public void setFavoritesOnly(boolean favoritesOnly) { this.favoritesOnly = favoritesOnly; }
}