package com.lunch.order.service;

import com.lunch.auth.model.User;
import com.lunch.auth.repository.UserRepository;
import com.lunch.auth.service.BonusService;
import com.lunch.order.model.Order;
import com.lunch.order.model.OrderStatus;
import com.lunch.order.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class OrderManagementService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final BonusService bonusService;

    public OrderManagementService(OrderRepository orderRepository, UserRepository userRepository,
                                  BonusService bonusService) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.bonusService = bonusService;
    }

    /** Усі замовлення, крім тих, що ще збираються в групових кімнатах. Нові зверху. */
    @Transactional(readOnly = true)
    public List<Order> findOrders() {
        return orderRepository.findAll().stream()
                .filter(o -> o.getStatus() != OrderStatus.COLLECTING)
                .sorted(Comparator.comparing((Order o) -> o.getStatus() != OrderStatus.NEW)
                        .thenComparing(Order::getId, Comparator.reverseOrder()))
                .toList();
    }

    @Transactional(readOnly = true)
    public Order getOrder(Long id) {
        Order order = orderRepository.findById(id).orElseThrow();
        if (order.getStatus() == OrderStatus.COLLECTING) {
            throw new NoSuchElementException("Замовлення ще збирається");
        }
        return order;
    }

    /** Зареєстрований клієнт замовлення; null, якщо замовлення гостьове. */
    @Transactional(readOnly = true)
    public User getCustomer(Order order) {
        return order.getUserId() == null
                ? null : userRepository.findById(order.getUserId()).orElse(null);
    }

    @Transactional
    public Order accept(Long id) {
        return changeStatus(id, OrderStatus.NEW, OrderStatus.ACCEPTED);
    }

    /** Відхиляє нове замовлення й повертає клієнту списані бонуси. */
    @Transactional
    public Order reject(Long id) {
        Order order = changeStatus(id, OrderStatus.NEW, OrderStatus.REJECTED);
        if (order.getUserId() != null && order.getBonusUsed() > 0) {
            bonusService.changeBalance(order.getUserId(), order.getBonusUsed(),
                    "Повернення бонусів за відхилене замовлення №" + id, id);
        }
        return order;
    }

    @Transactional
    public Order startPreparing(Long id) {
        return changeStatus(id, OrderStatus.ACCEPTED, OrderStatus.PREPARING);
    }

    /** Єдина точка зміни статусу (сюди підключимо сповіщення в SCRUM-19). */
    private Order changeStatus(Long id, OrderStatus expected, OrderStatus target) {
        Order order = getOrder(id);
        if (order.getStatus() != expected) {
            throw new IllegalStateException("Замовлення №" + id + " має статус «"
                    + order.getStatus().getLabel() + "», цю дію виконати неможливо");
        }
        order.setStatus(target);
        orderRepository.save(order);
        return order;
    }
}