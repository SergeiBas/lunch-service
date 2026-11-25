package com.lunch.order.model;

import com.lunch.menu.model.Dish;
import jakarta.persistence.*;
import com.lunch.menu.model.Allergen;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "dish_id")
    private Dish dish;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "unit_price", nullable = false)
    private double unitPrice;

    @Column(name = "participant_name")
    private String participantName;

    @Column(name = "participant_allergens")
    private String participantAllergens;

    @Column(name = "participant_wishes")
    private String participantWishes;

    public double getLineTotal() {
        return Math.round(unitPrice * quantity * 100.0) / 100.0;
    }

    /** Алергени учасника, які містить обрана страва. Порожній список, якщо збігів немає. */
    public List<String> getAllergenConflicts() {
        if (participantAllergens == null || participantAllergens.isBlank()) {
            return List.of();
        }
        Set<String> mine = Arrays.stream(participantAllergens.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
        return dish.getAllergens().stream()
                .map(Allergen::getName)
                .filter(mine::contains)
                .sorted()
                .toList();
    }

    public Long getId() { return id; }
    public Order getOrder() { return order; }
    public void setOrder(Order order) { this.order = order; }
    public Dish getDish() { return dish; }
    public void setDish(Dish dish) { this.dish = dish; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }
    public String getParticipantName() { return participantName; }
    public void setParticipantName(String participantName) { this.participantName = participantName; }
    public String getParticipantAllergens() { return participantAllergens; }
    public void setParticipantAllergens(String participantAllergens) { this.participantAllergens = participantAllergens; }
    public String getParticipantWishes() { return participantWishes; }
    public void setParticipantWishes(String participantWishes) { this.participantWishes = participantWishes; }
}