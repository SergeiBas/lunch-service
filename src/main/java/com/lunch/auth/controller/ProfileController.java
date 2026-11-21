package com.lunch.auth.controller;

import com.lunch.auth.model.ProfileForm;
import com.lunch.auth.model.User;
import com.lunch.auth.service.BonusService;
import com.lunch.auth.service.ProfileService;
import com.lunch.menu.repository.AllergenRepository;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ProfileController {

    private final ProfileService profileService;
    private final BonusService bonusService;
    private final AllergenRepository allergenRepository;

    public ProfileController(ProfileService profileService, BonusService bonusService,
                             AllergenRepository allergenRepository) {
        this.profileService = profileService;
        this.bonusService = bonusService;
        this.allergenRepository = allergenRepository;
    }

    @GetMapping("/profile")
    public String profile(Authentication auth, Model model) {
        model.addAttribute("form", profileService.getForm(auth.getName()));
        fillModel(model, auth.getName());
        return "profile/profile";
    }

    @PostMapping("/profile")
    public String update(Authentication auth,
                         @Valid @ModelAttribute("form") ProfileForm form,
                         BindingResult result, Model model,
                         RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            fillModel(model, auth.getName());
            return "profile/profile";
        }
        profileService.update(auth.getName(), form);
        redirectAttributes.addFlashAttribute("saved", true);
        return "redirect:/profile";
    }

    private void fillModel(Model model, String email) {
        User user = profileService.getUser(email);
        model.addAttribute("email", user.getEmail());
        model.addAttribute("bonusBalance", user.getBonusBalance());
        model.addAttribute("bonusHistory", bonusService.getHistory(user.getId()));
        model.addAttribute("allAllergens", allergenRepository.findAllByOrderByNameAsc());
    }
}