package com.lunch.menu.service;

import com.lunch.auth.model.User;
import com.lunch.auth.repository.UserRepository;
import com.lunch.menu.model.Allergen;
import com.lunch.menu.model.Category;
import com.lunch.menu.model.Dish;
import com.lunch.menu.model.MenuFilter;
import com.lunch.menu.repository.AllergenRepository;
import com.lunch.menu.repository.CategoryRepository;
import com.lunch.menu.repository.DishRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class MenuService {

    private final DishRepository dishRepository;
    private final CategoryRepository categoryRepository;
    private final AllergenRepository allergenRepository;
    private final UserRepository userRepository;

    public MenuService(DishRepository dishRepository, CategoryRepository categoryRepository,
                       AllergenRepository allergenRepository, UserRepository userRepository) {
        this.dishRepository = dishRepository;
        this.categoryRepository = categoryRepository;
        this.allergenRepository = allergenRepository;
        this.userRepository = userRepository;
    }

    /** Доступні та неархівні страви з урахуванням фільтрів. email = null для гостя. */
    @Transactional(readOnly = true)
    public List<Dish> getMenu(MenuFilter filter, String email) {
        Set<Long> favoriteIds = email == null ? Set.of() : getFavoriteIds(email);
        List<Long> excluded = filter.getExcludeAllergenIds() == null
                ? List.of() : filter.getExcludeAllergenIds();

        return dishRepository.findByArchivedFalseAndAvailableTrue().stream()
                .filter(d -> filter.getCategoryId() == null
                        || d.getCategory().getId().equals(filter.getCategoryId()))
                .filter(d -> filter.getMinPrice() == null || d.getPrice() >= filter.getMinPrice())
                .filter(d -> filter.getMaxPrice() == null || d.getPrice() <= filter.getMaxPrice())
                .filter(d -> excluded.isEmpty() || d.getAllergens().stream()
                        .noneMatch(a -> excluded.contains(a.getId())))
                .filter(d -> filter.getDiet() == null || filter.getDiet().isBlank()
                        || d.getDietLabelList().contains(filter.getDiet()))
                .filter(d -> !filter.isFavoritesOnly() || email == null
                        || favoriteIds.contains(d.getId()))
                .sorted(Comparator.comparing((Dish d) -> d.getCategory().getId())
                        .thenComparing(Dish::getName))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Category> getCategories() {
        return categoryRepository.findAllByOrderByIdAsc();
    }

    @Transactional(readOnly = true)
    public List<Allergen> getAllergens() {
        return allergenRepository.findAllByOrderByNameAsc();
    }

    /** Усі мітки, які реально є в меню (для випадаючого списку). */
    @Transactional(readOnly = true)
    public List<String> getDietLabels() {
        return dishRepository.findByArchivedFalseAndAvailableTrue().stream()
                .flatMap(d -> d.getDietLabelList().stream())
                .distinct()
                .sorted()
                .toList();
    }

    @Transactional(readOnly = true)
    public Set<Long> getFavoriteIds(String email) {
        return userRepository.findByEmail(email)
                .map(u -> u.getFavoriteDishes().stream()
                        .map(Dish::getId)
                        .collect(Collectors.toSet()))
                .orElse(Set.of());
    }

    @Transactional
    public void toggleFavorite(String email, Long dishId) {
        User user = userRepository.findByEmail(email).orElseThrow();
        Dish dish = dishRepository.findById(dishId).orElseThrow();
        if (!user.getFavoriteDishes().remove(dish)) {
            user.getFavoriteDishes().add(dish);
        }
        userRepository.save(user);
    }
}