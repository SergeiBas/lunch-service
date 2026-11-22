package com.lunch.menu.controller;

import com.lunch.menu.model.DishForm;
import com.lunch.menu.service.DuplicateDishNameException;
import com.lunch.menu.service.MenuAdminService;
import com.lunch.menu.service.MenuService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/manager/menu")
public class MenuAdminController {

    private final MenuAdminService adminService;
    private final MenuService menuService;

    public MenuAdminController(MenuAdminService adminService, MenuService menuService) {
        this.adminService = adminService;
        this.menuService = menuService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("dishes", adminService.findAll());
        return "menu/admin-list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("form", new DishForm());
        fillFormModel(model, false);
        return "menu/admin-form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("form", adminService.getForm(id));
        fillFormModel(model, true);
        return "menu/admin-form";
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("form") DishForm form, BindingResult result,
                       Model model, RedirectAttributes redirectAttributes) {
        if (!result.hasErrors()) {
            try {
                adminService.save(form);
                redirectAttributes.addFlashAttribute("message", "Страву збережено.");
                return "redirect:/manager/menu";
            } catch (DuplicateDishNameException e) {
                result.rejectValue("name", "duplicate", "Страва з такою назвою вже існує");
            }
        }
        fillFormModel(model, form.getId() != null);
        return "menu/admin-form";
    }

    @PostMapping("/{id}/archive")
    public String archive(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        adminService.setArchived(id, true);
        redirectAttributes.addFlashAttribute("message", "Страву архівовано.");
        return "redirect:/manager/menu";
    }

    @PostMapping("/{id}/restore")
    public String restore(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        adminService.setArchived(id, false);
        redirectAttributes.addFlashAttribute("message", "Страву відновлено.");
        return "redirect:/manager/menu";
    }

    @PostMapping("/{id}/availability")
    public String availability(@PathVariable Long id, @RequestParam boolean available,
                               RedirectAttributes redirectAttributes) {
        adminService.setAvailable(id, available);
        redirectAttributes.addFlashAttribute("message",
                available ? "Страву знову доступно клієнтам." : "Страву приховано від клієнтів.");
        return "redirect:/manager/menu";
    }

    private void fillFormModel(Model model, boolean editMode) {
        model.addAttribute("categories", menuService.getCategories());
        model.addAttribute("allergens", menuService.getAllergens());
        model.addAttribute("editMode", editMode);
    }
}