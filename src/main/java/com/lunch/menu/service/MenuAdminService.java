package com.lunch.menu.service;

import com.lunch.menu.model.Allergen;
import com.lunch.menu.model.Dish;
import com.lunch.menu.model.DishForm;
import com.lunch.menu.repository.AllergenRepository;
import com.lunch.menu.repository.CategoryRepository;
import com.lunch.menu.repository.DishRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class MenuAdminService {

    private final DishRepository dishRepository;
    private final CategoryRepository categoryRepository;
    private final AllergenRepository allergenRepository;

    public MenuAdminService(DishRepository dishRepository, CategoryRepository categoryRepository,
                            AllergenRepository allergenRepository) {
        this.dishRepository = dishRepository;
        this.categoryRepository = categoryRepository;
        this.allergenRepository = allergenRepository;
    }

    /** Усі страви, зокрема архівні. Активні зверху. */
    @Transactional(readOnly = true)
    public List<Dish> findAll() {
        return dishRepository.findAll().stream()
                .sorted(Comparator.comparing(Dish::isArchived)
                        .thenComparing(d -> d.getCategory().getId())
                        .thenComparing(Dish::getName))
                .toList();
    }

    @Transactional(readOnly = true)
    public DishForm getForm(Long id) {
        Dish dish = dishRepository.findById(id).orElseThrow();
        DishForm form = new DishForm();
        form.setId(dish.getId());
        form.setName(dish.getName());
        form.setDescription(dish.getDescription());
        form.setComposition(dish.getComposition());
        form.setPrice(dish.getPrice());
        form.setCategoryId(dish.getCategory().getId());
        form.setDietLabels(dish.getDietLabels());
        form.setAvailable(dish.isAvailable());
        form.setImageUrl(dish.getImageUrl());
        form.setAllergenIds(new ArrayList<>(
                dish.getAllergens().stream().map(Allergen::getId).toList()));
        return form;
    }

    @Transactional
    public void save(DishForm form) {
        String name = form.getName().trim();

        dishRepository.findByName(name).ifPresent(existing -> {
            if (!existing.getId().equals(form.getId())) {
                throw new DuplicateDishNameException(name);
            }
        });

        Dish dish = form.getId() == null
                ? new Dish()
                : dishRepository.findById(form.getId()).orElseThrow();

        dish.setName(name);
        dish.setDescription(blankToNull(form.getDescription()));
        dish.setComposition(blankToNull(form.getComposition()));
        dish.setPrice(form.getPrice());
        dish.setCategory(categoryRepository.findById(form.getCategoryId()).orElseThrow());
        dish.setDietLabels(blankToNull(form.getDietLabels()));
        dish.setAvailable(form.isAvailable());
        dish.setImageUrl(blankToNull(form.getImageUrl()));

        dish.getAllergens().clear();
        if (form.getAllergenIds() != null && !form.getAllergenIds().isEmpty()) {
            dish.getAllergens().addAll(allergenRepository.findAllById(form.getAllergenIds()));
        }
        dishRepository.save(dish);
    }

    @Transactional
    public void setArchived(Long id, boolean archived) {
        Dish dish = dishRepository.findById(id).orElseThrow();
        dish.setArchived(archived);
        dishRepository.save(dish);
    }

    @Transactional
    public void setAvailable(Long id, boolean available) {
        Dish dish = dishRepository.findById(id).orElseThrow();
        dish.setAvailable(available);
        dishRepository.save(dish);
    }

    private String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }
}