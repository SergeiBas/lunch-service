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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MenuServiceTest {

    @Mock
    private DishRepository dishRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private AllergenRepository allergenRepository;
    @Mock
    private UserRepository userRepository;

    private MenuService menuService;

    private Category soups;
    private Category salads;
    private Allergen lactose;
    private Allergen gluten;

    private Dish borscht;   // суп, 95 грн, лактоза
    private Dish broth;     // суп, 85 грн, глютен
    private Dish greek;     // салат, 120 грн, лактоза, вегетаріанське+безглютенове
    private Dish vegSalad;  // салат, 90 грн, без алергенів, веганське

    @BeforeEach
    void setUp() {
        menuService = new MenuService(dishRepository, categoryRepository,
                allergenRepository, userRepository);

        soups = category(1, "Soups");
        salads = category(2, "Salads");
        lactose = allergen(1, "Lactose");
        gluten = allergen(2, "Gluten");

        borscht = dish(1, "Borscht", 95, soups, null, lactose);
        broth = dish(2, "Broth", 85, soups, null, gluten);
        greek = dish(3, "Greek", 120, salads, "vegetarian,gluten-free", lactose);
        vegSalad = dish(4, "Veggie", 90, salads, "vegan");

        lenient().when(dishRepository.findByArchivedFalseAndAvailableTrue())
                .thenReturn(List.of(vegSalad, greek, broth, borscht));
    }

    // ---------- допоміжні методи ----------

    private Category category(long id, String name) {
        Category c = new Category();
        ReflectionTestUtils.setField(c, "id", id);
        c.setName(name);
        return c;
    }

    private Allergen allergen(long id, String name) {
        Allergen a = new Allergen();
        ReflectionTestUtils.setField(a, "id", id);
        a.setName(name);
        return a;
    }

    private Dish dish(long id, String name, double price, Category category,
                      String dietLabels, Allergen... allergens) {
        Dish d = new Dish();
        ReflectionTestUtils.setField(d, "id", id);
        d.setName(name);
        d.setPrice(price);
        d.setCategory(category);
        d.setDietLabels(dietLabels);
        d.setAllergens(new HashSet<>(List.of(allergens)));
        return d;
    }

    private List<String> names(List<Dish> dishes) {
        return dishes.stream().map(Dish::getName).toList();
    }

    // ---------- фільтрація ----------

    @Test
    void getMenu_withoutFilters_returnsAllSortedByCategoryThenName() {
        List<Dish> result = menuService.getMenu(new MenuFilter(), null);

        assertThat(names(result)).containsExactly("Borscht", "Broth", "Greek", "Veggie");
    }

    @Test
    void getMenu_filtersByCategory() {
        MenuFilter filter = new MenuFilter();
        filter.setCategoryId(2L);

        assertThat(names(menuService.getMenu(filter, null)))
                .containsExactly("Greek", "Veggie");
    }

    @Test
    void getMenu_filtersByPriceRange_inclusiveBounds() {
        MenuFilter filter = new MenuFilter();
        filter.setMinPrice(90.0);
        filter.setMaxPrice(120.0);

        assertThat(names(menuService.getMenu(filter, null)))
                .containsExactly("Borscht", "Greek", "Veggie");
    }

    @Test
    void getMenu_excludesDishesWithSelectedAllergens() {
        MenuFilter filter = new MenuFilter();
        filter.setExcludeAllergenIds(List.of(1L)); // лактоза

        assertThat(names(menuService.getMenu(filter, null)))
                .containsExactly("Broth", "Veggie");
    }

    @Test
    void getMenu_excludesSeveralAllergensAtOnce() {
        MenuFilter filter = new MenuFilter();
        filter.setExcludeAllergenIds(List.of(1L, 2L)); // лактоза і глютен

        assertThat(names(menuService.getMenu(filter, null)))
                .containsExactly("Veggie");
    }

    @Test
    void getMenu_filtersByDietLabel() {
        MenuFilter filter = new MenuFilter();
        filter.setDiet("gluten-free");

        assertThat(names(menuService.getMenu(filter, null)))
                .containsExactly("Greek");
    }

    @Test
    void getMenu_combinesAllFilters() {
        MenuFilter filter = new MenuFilter();
        filter.setCategoryId(2L);
        filter.setMaxPrice(100.0);
        filter.setExcludeAllergenIds(List.of(1L));
        filter.setDiet("vegan");

        assertThat(names(menuService.getMenu(filter, null)))
                .containsExactly("Veggie");
    }

    @Test
    void getMenu_returnsEmptyListWhenNothingMatches() {
        MenuFilter filter = new MenuFilter();
        filter.setMinPrice(1000.0);

        assertThat(menuService.getMenu(filter, null)).isEmpty();
    }

    // ---------- улюблені ----------

    @Test
    void getMenu_favoritesOnly_returnsOnlyUsersFavorites() {
        User user = new User();
        user.setFavoriteDishes(new HashSet<>(Set.of(greek, broth)));
        when(userRepository.findByEmail("anna@test.com")).thenReturn(Optional.of(user));

        MenuFilter filter = new MenuFilter();
        filter.setFavoritesOnly(true);

        assertThat(names(menuService.getMenu(filter, "anna@test.com")))
                .containsExactly("Broth", "Greek");
    }

    @Test
    void getMenu_favoritesOnly_isIgnoredForGuests() {
        MenuFilter filter = new MenuFilter();
        filter.setFavoritesOnly(true);

        assertThat(menuService.getMenu(filter, null)).hasSize(4);
    }

    @Test
    void toggleFavorite_addsThenRemovesDish() {
        User user = new User();
        when(userRepository.findByEmail("anna@test.com")).thenReturn(Optional.of(user));
        when(dishRepository.findById(3L)).thenReturn(Optional.of(greek));

        menuService.toggleFavorite("anna@test.com", 3L);
        assertThat(user.getFavoriteDishes()).containsExactly(greek);

        menuService.toggleFavorite("anna@test.com", 3L);
        assertThat(user.getFavoriteDishes()).isEmpty();

        verify(userRepository, times(2)).save(any(User.class));
    }
}