package com.lunch.auth.model;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    private String phone;
    private String address;

    @Column(name = "dietary_preferences")
    private String dietaryPreferences;

    @Column(name = "bonus_balance", nullable = false)
    private int bonusBalance = 0;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "role_id")
    private Role role;

    public Long getId() { return id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getDietaryPreferences() { return dietaryPreferences; }
    public void setDietaryPreferences(String d) { this.dietaryPreferences = d; }
    public int getBonusBalance() { return bonusBalance; }
    public void setBonusBalance(int bonusBalance) { this.bonusBalance = bonusBalance; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
}