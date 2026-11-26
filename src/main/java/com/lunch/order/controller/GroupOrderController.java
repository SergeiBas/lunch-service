package com.lunch.order.controller;

import com.lunch.order.model.CloseGroupForm;
import com.lunch.order.model.GroupOrder;
import com.lunch.order.model.GroupRoomForm;
import com.lunch.order.model.Order;
import com.lunch.order.model.OrderItem;
import com.lunch.order.service.GroupOrderService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/collector/rooms")
public class GroupOrderController {

    private final GroupOrderService groupOrderService;

    public GroupOrderController(GroupOrderService groupOrderService) {
        this.groupOrderService = groupOrderService;
    }

    @GetMapping
    public String list(Authentication auth, Model model) {
        model.addAttribute("rooms", groupOrderService.getRooms(auth.getName()));
        model.addAttribute("form", new GroupRoomForm());
        return "collector/rooms";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("form") GroupRoomForm form, BindingResult result,
                         Authentication auth, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("rooms", groupOrderService.getRooms(auth.getName()));
            return "collector/rooms";
        }
        GroupOrder group = groupOrderService.createRoom(form.getTitle(), auth.getName());
        return "redirect:/collector/rooms/" + group.getId();
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Authentication auth, Model model) {
        Order order = fillDetailModel(model, id, auth.getName());
        CloseGroupForm form = new CloseGroupForm();
        if (!GroupOrderService.ADDRESS_PLACEHOLDER.equals(order.getDeliveryAddress())) {
            form.setDeliveryAddress(order.getDeliveryAddress());
        }
        model.addAttribute("form", form);
        return "collector/room-detail";
    }

    @PostMapping("/{id}/close")
    public String close(@PathVariable Long id, @Valid @ModelAttribute("form") CloseGroupForm form,
                        BindingResult result, Authentication auth, Model model,
                        RedirectAttributes redirectAttributes) {
        if (!result.hasErrors()) {
            try {
                Order order = groupOrderService.closeRoom(id, auth.getName(), form);
                redirectAttributes.addFlashAttribute("message",
                        "Збір закрито. Замовлення №" + order.getId() + " передано менеджеру прийому.");
                return "redirect:/collector/rooms/" + id;
            } catch (IllegalStateException e) {
                result.reject("close", e.getMessage());
            }
        }
        fillDetailModel(model, id, auth.getName());
        return "collector/room-detail";
    }

    private Order fillDetailModel(Model model, Long id, String email) {
        GroupOrder group = groupOrderService.getRoomForCollector(id, email);
        Order order = groupOrderService.getOrder(group);

        Map<String, List<OrderItem>> byParticipant = order.getItems().stream()
                .collect(Collectors.groupingBy(
                        i -> i.getParticipantName() == null ? "—" : i.getParticipantName(),
                        LinkedHashMap::new, Collectors.toList()));
        Map<String, Double> participantTotals = new LinkedHashMap<>();
        byParticipant.forEach((name, items) -> participantTotals.put(name,
                Math.round(items.stream().mapToDouble(OrderItem::getLineTotal).sum() * 100.0) / 100.0));
        model.addAttribute("participantTotals", participantTotals);
        long conflictCount = order.getItems().stream()
                .filter(i -> !i.getAllergenConflicts().isEmpty()).count();

        model.addAttribute("group", group);
        model.addAttribute("order", order);
        model.addAttribute("discountPercent", groupOrderService.discountPercent(order));
        model.addAttribute("byParticipant", byParticipant);
        model.addAttribute("conflictCount", conflictCount);
        model.addAttribute("joinUrl", ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/join/" + group.getRoomCode()).toUriString());
        return order;
    }
}