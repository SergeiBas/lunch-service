package com.lunch.auth.service;

import com.lunch.auth.model.RegistrationForm;
import com.lunch.auth.model.Role;
import com.lunch.auth.model.User;
import com.lunch.auth.repository.RoleRepository;
import com.lunch.auth.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
public class UserService {

    private static final Set<String> ALLOWED_ROLES =
            Set.of("CLIENT", "COLLECTOR_MANAGER", "ORDER_MANAGER", "COURIER");

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User register(RegistrationForm form) {
        String email = form.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyUsedException(email);
        }
        if (!ALLOWED_ROLES.contains(form.getRole())) {
            throw new IllegalArgumentException("Unknown role: " + form.getRole());
        }

        Role role = roleRepository.findByName(form.getRole()).orElseThrow();

        User user = new User();
        user.setEmail(email);
        user.setFullName(form.getFullName().trim());
        user.setPasswordHash(passwordEncoder.encode(form.getPassword()));
        user.setRole(role);
        return userRepository.save(user);
    }
}