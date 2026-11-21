package com.lunch.menu.controller;

import com.lunch.menu.model.MenuFilter;
import com.lunch.menu.service.MenuService;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

@Controller
public class MenuController {

    private final MenuService menuService;

    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    @GetMapping("/menu")
    public String menu(@ModelAttribute("filter") MenuFilter filter,
                       Authentication auth, Model model) {
        boolean loggedIn = auth != null && auth.isAuthenticated()
                && !(auth instanceof AnonymousAuthenticationToken);
        String email = loggedIn ? auth.getName() : null;

        model.addAttribute("dishes", menuService.getMenu(filter, email));
        model.addAttribute("categories", menuService.getCategories());
        model.addAttribute("allergens", menuService.getAllergens());
        model.addAttribute("dietLabels", menuService.getDietLabels());
        model.addAttribute("favoriteIds", loggedIn ? menuService.getFavoriteIds(email) : java.util.Set.of());
        return "menu/menu";
    }
}