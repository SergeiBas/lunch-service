package com.lunch.auth.service;

import com.lunch.auth.model.ProfileForm;
import com.lunch.auth.model.User;
import com.lunch.auth.repository.UserRepository;
import com.lunch.menu.model.Allergen;
import com.lunch.menu.repository.AllergenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

@Service
public class ProfileService {

    private final UserRepository userRepository;
    private final AllergenRepository allergenRepository;

    public ProfileService(UserRepository userRepository, AllergenRepository allergenRepository) {
        this.userRepository = userRepository;
        this.allergenRepository = allergenRepository;
    }

    @Transactional(readOnly = true)
    public User getUser(String email) {
        return userRepository.findByEmail(email).orElseThrow();
    }

    @Transactional(readOnly = true)
    public ProfileForm getForm(String email) {
        User user = getUser(email);
        ProfileForm form = new ProfileForm();
        form.setFullName(user.getFullName());
        form.setPhone(user.getPhone());
        form.setAddress(user.getAddress());
        form.setDietaryPreferences(user.getDietaryPreferences());
        form.setAllergenIds(new ArrayList<>(
                user.getAllergens().stream().map(Allergen::getId).toList()));
        return form;
    }

    @Transactional
    public void update(String email, ProfileForm form) {
        User user = getUser(email);
        user.setFullName(form.getFullName().trim());
        user.setPhone(blankToNull(form.getPhone()));
        user.setAddress(blankToNull(form.getAddress()));
        user.setDietaryPreferences(blankToNull(form.getDietaryPreferences()));

        user.getAllergens().clear();
        if (form.getAllergenIds() != null && !form.getAllergenIds().isEmpty()) {
            user.getAllergens().addAll(allergenRepository.findAllById(form.getAllergenIds()));
        }
        userRepository.save(user);
    }

    private String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }
}