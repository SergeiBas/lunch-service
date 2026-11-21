package com.lunch.menu.controller;

import com.lunch.menu.service.MenuService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class FavoriteController {

    private final MenuService menuService;

    public FavoriteController(MenuService menuService) {
        this.menuService = menuService;
    }

    @PostMapping("/favorites/{dishId}/toggle")
    public String toggle(@PathVariable Long dishId, Authentication auth,
                         HttpServletRequest request) {
        menuService.toggleFavorite(auth.getName(), dishId);
        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null && referer.contains("/menu") ? referer : "/menu");
    }
}