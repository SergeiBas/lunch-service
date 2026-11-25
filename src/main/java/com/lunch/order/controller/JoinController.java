package com.lunch.order.controller;

import com.lunch.menu.model.MenuFilter;
import com.lunch.menu.service.MenuService;
import com.lunch.order.model.GroupOrder;
import com.lunch.order.model.Order;
import com.lunch.order.model.OrderItem;
import com.lunch.order.model.ParticipantItemForm;
import com.lunch.order.service.DishUnavailableException;
import com.lunch.order.service.GroupOrderService;
import com.lunch.order.service.RoomClosedException;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/join")
public class JoinController {

    private static final String NAME_ATTR = "participantName";

    private final GroupOrderService groupOrderService;
    private final MenuService menuService;

    public JoinController(GroupOrderService groupOrderService, MenuService menuService) {
        this.groupOrderService = groupOrderService;
        this.menuService = menuService;
    }

    @GetMapping
    public String enterCode() {
        return "join/join";
    }

    @PostMapping
    public String goToRoom(@RequestParam String code, RedirectAttributes redirectAttributes) {
        String normalized = code == null ? "" : code.trim().toUpperCase();
        if (!normalized.matches("[A-Z0-9]{4,12}")) {
            redirectAttributes.addFlashAttribute("error", "Введіть коректний код кімнати.");
            return "redirect:/join";
        }
        return "redirect:/join/" + normalized;
    }

    @GetMapping("/{code}")
    public String room(@PathVariable String code, HttpSession session, Model model) {
        GroupOrder group = groupOrderService.getRoomByCode(code);
        String name = (String) session.getAttribute(NAME_ATTR);

        ParticipantItemForm form = new ParticipantItemForm();
        form.setParticipantName(name);
        model.addAttribute("form", form);
        fillRoomModel(model, group, name);
        return "join/room";
    }

    @PostMapping("/{code}/items")
    public String addItem(@PathVariable String code,
                          @Valid @ModelAttribute("form") ParticipantItemForm form, BindingResult result,
                          HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        GroupOrder group = groupOrderService.getRoomByCode(code);
        if (!result.hasErrors()) {
            try {
                List<String> conflicts = groupOrderService.addItem(code, form);
                session.setAttribute(NAME_ATTR, form.getParticipantName().trim());
                redirectAttributes.addFlashAttribute("message", "Позицію додано.");
                if (!conflicts.isEmpty()) {
                    redirectAttributes.addFlashAttribute("warning",
                            "Увага: ця страва містить ваші алергени: " + String.join(", ", conflicts)
                                    + ". Менеджер побачить попередження.");
                }
                return "redirect:/join/" + group.getRoomCode();
            } catch (RoomClosedException | DishUnavailableException e) {
                result.reject("failed", e.getMessage());
            }
        }
        fillRoomModel(model, group, (String) session.getAttribute(NAME_ATTR));
        return "join/room";
    }

    @PostMapping("/{code}/items/{itemId}/remove")
    public String removeItem(@PathVariable String code, @PathVariable Long itemId,
                             HttpSession session, RedirectAttributes redirectAttributes) {
        GroupOrder group = groupOrderService.getRoomByCode(code);
        String name = (String) session.getAttribute(NAME_ATTR);
        try {
            if (name != null) {
                groupOrderService.removeItem(code, itemId, name);
                redirectAttributes.addFlashAttribute("message", "Позицію видалено.");
            }
        } catch (RoomClosedException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/join/" + group.getRoomCode();
    }

    private void fillRoomModel(Model model, GroupOrder group, String participantName) {
        Order order = groupOrderService.getOrder(group);
        List<OrderItem> myItems = participantName == null ? List.of()
                : order.getItems().stream()
                .filter(i -> i.getParticipantName() != null
                             && i.getParticipantName().equalsIgnoreCase(participantName))
                .toList();

        model.addAttribute("group", group);
        model.addAttribute("dishes", menuService.getMenu(new MenuFilter(), null));
        model.addAttribute("allergens", menuService.getAllergens());
        model.addAttribute("myItems", myItems);
    }
}