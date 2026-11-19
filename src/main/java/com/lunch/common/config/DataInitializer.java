package com.lunch.common.config;

import com.lunch.auth.model.Role;
import com.lunch.auth.model.User;
import com.lunch.auth.repository.RoleRepository;
import com.lunch.auth.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, RoleRepository roleRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        createIfMissing("client@lunch.test", "Test Client", "CLIENT");
        createIfMissing("collector@lunch.test", "Test Collector", "COLLECTOR_MANAGER");
        createIfMissing("manager@lunch.test", "Test Manager", "ORDER_MANAGER");
        createIfMissing("courier@lunch.test", "Test Courier", "COURIER");
    }

    private void createIfMissing(String email, String fullName, String roleName) {
        if (userRepository.existsByEmail(email)) {
            return;
        }
        Role role = roleRepository.findByName(roleName).orElseThrow();
        User user = new User();
        user.setEmail(email);
        user.setFullName(fullName);
        user.setPasswordHash(passwordEncoder.encode("password123"));
        user.setRole(role);
        userRepository.save(user);
    }
}