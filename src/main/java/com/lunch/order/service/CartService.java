package com.lunch.order.service;

import com.lunch.menu.model.Dish;
import com.lunch.menu.repository.DishRepository;
import com.lunch.order.model.CartLine;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class CartService {

    private final Cart cart;
    private final DishRepository dishRepository;

    public CartService(Cart cart, DishRepository dishRepository) {
        this.cart = cart;
        this.dishRepository = dishRepository;
    }

    @Transactional(readOnly = true)
    public void add(Long dishId, int quantity) {
        Dish dish = dishRepository.findById(dishId).orElseThrow();
        if (dish.isArchived() || !dish.isAvailable()) {
            throw new DishUnavailableException("Страва «" + dish.getName() + "» зараз недоступна");
        }
        cart.add(dishId, quantity);
    }

    public void setQuantity(Long dishId, int quantity) {
        cart.setQuantity(dishId, quantity);
    }

    public void remove(Long dishId) {
        cart.remove(dishId);
    }

    public void clear() {
        cart.clear();
    }

    public boolean isEmpty() {
        return cart.isEmpty();
    }

    /** Рядки кошика. Страви, що стали недоступними чи архівними, прибираються з кошика. */
    @Transactional(readOnly = true)
    public List<CartLine> getLines() {
        List<CartLine> lines = new ArrayList<>();
        for (Map.Entry<Long, Integer> entry : cart.getItems().entrySet()) {
            Dish dish = dishRepository.findById(entry.getKey()).orElse(null);
            if (dish == null || dish.isArchived() || !dish.isAvailable()) {
                cart.remove(entry.getKey());
                continue;
            }
            lines.add(new CartLine(dish, entry.getValue()));
        }
        return lines;
    }
}