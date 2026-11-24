package com.lunch.order.service;

import com.lunch.auth.model.User;
import com.lunch.auth.repository.UserRepository;
import com.lunch.order.model.CartLine;
import com.lunch.order.model.CheckoutForm;
import com.lunch.order.model.Order;
import com.lunch.order.model.OrderItem;
import com.lunch.order.model.OrderStatus;
import com.lunch.order.repository.OrderRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final CartService cartService;
    private final PriceCalculationService priceCalculationService;

    public OrderService(OrderRepository orderRepository, UserRepository userRepository,
                        CartService cartService, PriceCalculationService priceCalculationService) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.cartService = cartService;
        this.priceCalculationService = priceCalculationService;
    }

    /** Оформлює замовлення з поточного кошика. userEmail = null для гостя. */
    @Transactional
    public Order placeOrder(CheckoutForm form, String userEmail) {
        List<CartLine> lines = cartService.getLines();
        if (lines.isEmpty()) {
            throw new IllegalStateException("Кошик порожній");
        }

        Order order = new Order();
        order.setStatus(OrderStatus.NEW);
        order.setDeliveryAddress(form.getDeliveryAddress().trim());
        order.setComment(blankToNull(form.getComment()));

        if (userEmail != null) {
            User user = userRepository.findByEmail(userEmail).orElseThrow();
            order.setUserId(user.getId());
        } else {
            order.setGuestName(form.getGuestName().trim());
            order.setGuestPhone(form.getGuestPhone().trim());
            order.setGuestEmail(form.getGuestEmail().trim().toLowerCase());
        }

        double subtotal = priceCalculationService.subtotal(lines);
        order.setSubtotal(subtotal);
        order.setDiscount(0);
        order.setBonusUsed(0);
        order.setTotal(subtotal);

        for (CartLine line : lines) {
            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setDish(line.getDish());
            item.setQuantity(line.getQuantity());
            item.setUnitPrice(line.getDish().getPrice()); // ціна фіксується на момент замовлення
            order.getItems().add(item);
        }

        Order saved = orderRepository.save(order);
        cartService.clear();
        return saved;
    }

    /**
     * Замовлення для перегляду: клієнт бачить лише свої, гість лише те, яке щойно оформив
     * у цій сесії (lastGuestOrderId).
     */
    @Transactional(readOnly = true)
    public Order getOrderForViewer(Long id, String email, Object lastGuestOrderId) {
        Order order = orderRepository.findById(id).orElseThrow();
        boolean allowed;
        if (email != null) {
            Long userId = userRepository.findByEmail(email).map(User::getId).orElse(null);
            allowed = order.getUserId() != null && order.getUserId().equals(userId);
        } else {
            allowed = order.getUserId() == null && id.equals(lastGuestOrderId);
        }
        if (!allowed) {
            throw new AccessDeniedException("Немає доступу до замовлення");
        }
        return order;
    }

    private String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }
}