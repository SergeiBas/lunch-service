package com.lunch.order.controller;

import com.lunch.order.service.OrderService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MyOrdersController {

    private final OrderService orderService;

    public MyOrdersController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/orders")
    public String list(Authentication auth, Model model) {
        model.addAttribute("orders", orderService.getOrdersForUser(auth.getName()));
        return "orders/my-orders";
    }
}