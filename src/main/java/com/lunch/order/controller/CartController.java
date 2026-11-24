package com.lunch.order.controller;

import com.lunch.auth.service.ProfileService;
import com.lunch.order.model.CartLine;
import com.lunch.order.model.CheckoutForm;
import com.lunch.order.model.Order;
import com.lunch.order.service.CartService;
import com.lunch.order.service.DishUnavailableException;
import com.lunch.order.service.OrderService;
import com.lunch.order.service.PriceCalculationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.URI;
import java.util.List;

@Controller
@RequestMapping("/cart")
public class CartController {

    private static final String LAST_ORDER_ATTR = "lastGuestOrderId";

    private final CartService cartService;
    private final OrderService orderService;
    private final PriceCalculationService priceCalculationService;
    private final ProfileService profileService;

    public CartController(CartService cartService, OrderService orderService,
                          PriceCalculationService priceCalculationService,
                          ProfileService profileService) {
        this.cartService = cartService;
        this.orderService = orderService;
        this.priceCalculationService = priceCalculationService;
        this.profileService = profileService;
    }

    @GetMapping
    public String cart(Model model) {
        fillCartModel(model);
        return "cart/cart";
    }

    @PostMapping("/add/{dishId}")
    public String add(@PathVariable Long dishId, @RequestParam(defaultValue = "1") int quantity,
                      RedirectAttributes redirectAttributes, HttpServletRequest request) {
        try {
            cartService.add(dishId, quantity);
            redirectAttributes.addFlashAttribute("message", "Страву додано в кошик.");
        } catch (DishUnavailableException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return backToMenu(request);
    }

    @PostMapping("/update/{dishId}")
    public String update(@PathVariable Long dishId, @RequestParam int quantity) {
        cartService.setQuantity(dishId, quantity);
        return "redirect:/cart";
    }

    @PostMapping("/remove/{dishId}")
    public String remove(@PathVariable Long dishId) {
        cartService.remove(dishId);
        return "redirect:/cart";
    }

    @GetMapping("/checkout")
    public String checkoutForm(Authentication auth, Model model) {
        if (cartService.isEmpty()) {
            return "redirect:/cart";
        }
        CheckoutForm form = new CheckoutForm();
        boolean guest = !isLoggedIn(auth);
        if (!guest) {
            form.setDeliveryAddress(profileService.getUser(auth.getName()).getAddress());
        }
        model.addAttribute("form", form);
        fillCheckoutModel(model, guest);
        return "cart/checkout";
    }

    @PostMapping("/checkout")
    public String placeOrder(@Valid @ModelAttribute("form") CheckoutForm form, BindingResult result,
                             Authentication auth, HttpSession session, Model model) {
        if (cartService.isEmpty()) {
            return "redirect:/cart";
        }
        boolean guest = !isLoggedIn(auth);
        if (guest) {
            validateGuest(form, result);
        }
        if (!result.hasErrors()) {
            try {
                Order order = orderService.placeOrder(form, guest ? null : auth.getName());
                if (guest) {
                    session.setAttribute(LAST_ORDER_ATTR, order.getId());
                }
                return "redirect:/cart/confirmation/" + order.getId();
            } catch (DishUnavailableException e) {
                result.reject("unavailable", e.getMessage());
            }
        }
        fillCheckoutModel(model, guest);
        return "cart/checkout";
    }

    @GetMapping("/confirmation/{id}")
    public String confirmation(@PathVariable Long id, Authentication auth,
                               HttpSession session, Model model) {
        String email = isLoggedIn(auth) ? auth.getName() : null;
        Order order = orderService.getOrderForViewer(id, email, session.getAttribute(LAST_ORDER_ATTR));
        model.addAttribute("order", order);
        return "cart/confirmation";
    }

    // ---------- допоміжні методи ----------

    private void fillCartModel(Model model) {
        List<CartLine> lines = cartService.getLines();
        model.addAttribute("lines", lines);
        model.addAttribute("subtotal", priceCalculationService.subtotal(lines));
    }

    private void fillCheckoutModel(Model model, boolean guest) {
        fillCartModel(model);
        model.addAttribute("isGuest", guest);
    }

    private void validateGuest(CheckoutForm form, BindingResult result) {
        if (isBlank(form.getGuestName())) {
            result.rejectValue("guestName", "required", "Вкажіть ім'я");
        }
        if (isBlank(form.getGuestPhone()) || !form.getGuestPhone().matches("^[+0-9()\\-\\s]{7,20}$")) {
            result.rejectValue("guestPhone", "invalid", "Вкажіть коректний номер телефону");
        }
        if (isBlank(form.getGuestEmail()) || !form.getGuestEmail().matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            result.rejectValue("guestEmail", "invalid", "Вкажіть коректний email");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private boolean isLoggedIn(Authentication auth) {
        return auth != null && auth.isAuthenticated()
                && !(auth instanceof AnonymousAuthenticationToken);
    }

    /** Повертає на меню з тими самими фільтрами; зовнішні адреси ігноруються. */
    private String backToMenu(HttpServletRequest request) {
        String referer = request.getHeader("Referer");
        if (referer != null) {
            try {
                URI uri = URI.create(referer);
                if (uri.getRawPath() != null && uri.getRawPath().startsWith("/menu")) {
                    return "redirect:" + uri.getRawPath()
                            + (uri.getRawQuery() != null ? "?" + uri.getRawQuery() : "");
                }
            } catch (IllegalArgumentException ignored) {
                // некоректний Referer, повертаємо на меню без фільтрів
            }
        }
        return "redirect:/menu";
    }
}