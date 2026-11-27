package com.lunch.order.controller;

import com.lunch.order.model.Order;
import com.lunch.order.model.OrderItem;
import com.lunch.order.model.OrderStatus;
import com.lunch.order.service.OrderManagementService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/manager/orders")
public class OrderManagementController {

    private final OrderManagementService service;

    public OrderManagementController(OrderManagementService service) {
        this.service = service;
    }

    @GetMapping
    public String list(Model model) {
        List<Order> orders = service.findOrders();
        model.addAttribute("orders", orders);
        model.addAttribute("newCount", orders.stream()
                .filter(o -> o.getStatus() == OrderStatus.NEW).count());
        return "manager/orders";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Order order = service.getOrder(id);
        long conflictCount = order.getItems().stream()
                .filter(i -> !i.getAllergenConflicts().isEmpty()).count();

        model.addAttribute("order", order);
        model.addAttribute("customer", service.getCustomer(order));
        model.addAttribute("isGroup", order.getGroupOrderId() != null);
        model.addAttribute("conflictCount", conflictCount);
        return "manager/order-detail";
    }

    @PostMapping("/{id}/accept")
    public String accept(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        return act(id, redirectAttributes, "Замовлення підтверджено.", () -> service.accept(id));
    }

    @PostMapping("/{id}/reject")
    public String reject(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        return act(id, redirectAttributes, "Замовлення відхилено.", () -> service.reject(id));
    }

    @PostMapping("/{id}/prepare")
    public String prepare(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        return act(id, redirectAttributes, "Замовлення передано на виконання.", () -> service.startPreparing(id));
    }

    private String act(Long id, RedirectAttributes redirectAttributes, String okMessage, Runnable action) {
        try {
            action.run();
            redirectAttributes.addFlashAttribute("message", okMessage);
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/manager/orders/" + id;
    }
}