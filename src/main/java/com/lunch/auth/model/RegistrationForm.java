package com.lunch.auth.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegistrationForm {

    @NotBlank(message = "Вкажіть ім'я")
    @Size(max = 100, message = "Ім'я занадто довге")
    private String fullName;

    @NotBlank(message = "Вкажіть email")
    @Email(message = "Некоректний email")
    private String email;

    @NotBlank(message = "Вкажіть пароль")
    @Size(min = 8, max = 72, message = "Пароль має містити від 8 до 72 символів")
    private String password;

    @NotBlank(message = "Підтвердіть пароль")
    private String confirmPassword;

    @NotBlank(message = "Оберіть роль")
    private String role = "CLIENT";

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getConfirmPassword() { return confirmPassword; }
    public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}