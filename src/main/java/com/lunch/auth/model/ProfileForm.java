package com.lunch.auth.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

public class ProfileForm {

    @NotBlank(message = "Вкажіть ім'я")
    @Size(max = 100, message = "Ім'я занадто довге")
    private String fullName;

    @Pattern(regexp = "^$|^[+0-9()\\-\\s]{7,20}$", message = "Некоректний номер телефону")
    private String phone;

    @Size(max = 255, message = "Адреса занадто довга")
    private String address;

    @Size(max = 255, message = "Занадто довгий текст")
    private String dietaryPreferences;

    private List<Long> allergenIds = new ArrayList<>();

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getDietaryPreferences() { return dietaryPreferences; }
    public void setDietaryPreferences(String dietaryPreferences) { this.dietaryPreferences = dietaryPreferences; }
    public List<Long> getAllergenIds() { return allergenIds; }
    public void setAllergenIds(List<Long> allergenIds) { this.allergenIds = allergenIds; }
}