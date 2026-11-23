package com.lunch.auth.service;

import com.lunch.auth.model.RegistrationForm;
import com.lunch.auth.model.Role;
import com.lunch.auth.model.User;
import com.lunch.auth.repository.RoleRepository;
import com.lunch.auth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, roleRepository, passwordEncoder);
    }

    private RegistrationForm form(String email, String role) {
        RegistrationForm form = new RegistrationForm();
        form.setFullName("  Анна Коваль ");
        form.setEmail(email);
        form.setPassword("password123");
        form.setConfirmPassword("password123");
        form.setRole(role);
        return form;
    }

    private Role role(String name) {
        Role role = new Role();
        role.setName(name);
        return role;
    }

    @Test
    void register_savesUserWithNormalizedDataAndEncodedPassword() {
        Role clientRole = role("CLIENT");
        when(roleRepository.findByName("CLIENT")).thenReturn(Optional.of(clientRole));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User saved = userService.register(form("  Anna@Test.com ", "CLIENT"));

        assertThat(saved.getEmail()).isEqualTo("anna@test.com");
        assertThat(saved.getFullName()).isEqualTo("Анна Коваль");
        assertThat(saved.getRole()).isSameAs(clientRole);
    }

    @Test
    void register_neverStoresPasswordInPlainText() {
        when(roleRepository.findByName("CLIENT")).thenReturn(Optional.of(role("CLIENT")));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User saved = userService.register(form("anna@test.com", "CLIENT"));

        assertThat(saved.getPasswordHash()).isNotEqualTo("password123");
        assertThat(saved.getPasswordHash()).startsWith("$2");
        assertThat(passwordEncoder.matches("password123", saved.getPasswordHash())).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"CLIENT", "COLLECTOR_MANAGER", "ORDER_MANAGER", "COURIER"})
    void register_acceptsAllFourRoles(String roleName) {
        when(roleRepository.findByName(roleName)).thenReturn(Optional.of(role(roleName)));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User saved = userService.register(form("user@test.com", roleName));

        assertThat(saved.getRole().getName()).isEqualTo(roleName);
    }

    @Test
    void register_rejectsDuplicateEmail() {
        when(userRepository.existsByEmail("anna@test.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.register(form("ANNA@test.com", "CLIENT")))
                .isInstanceOf(EmailAlreadyUsedException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void register_rejectsUnknownRole() {
        assertThatThrownBy(() -> userService.register(form("anna@test.com", "ADMIN")))
                .isInstanceOf(IllegalArgumentException.class);

        verify(roleRepository, never()).findByName(any());
        verify(userRepository, never()).save(any());
    }
}