package com.lunch.common.config;

import com.lunch.order.service.Cart;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAttributes {

    @ModelAttribute("currentUser")
    public String currentUser(Authentication auth) {
        return isLoggedIn(auth) ? auth.getName() : null;
    }

    @ModelAttribute("userRole")
    public String userRole(Authentication auth) {
        if (!isLoggedIn(auth)) {
            return null;
        }
        return auth.getAuthorities().stream()
                .findFirst()
                .map(a -> a.getAuthority().replace("ROLE_", ""))
                .orElse(null);
    }

    private boolean isLoggedIn(Authentication auth) {
        return auth != null && auth.isAuthenticated()
                && !(auth instanceof AnonymousAuthenticationToken);
    }

    private final Cart cart;

    public GlobalModelAttributes(Cart cart) {
        this.cart = cart;
    }

    @ModelAttribute("cartCount")
    public int cartCount() {
        return cart.totalCount();
    }
}