package com.lunch.order.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

// Ім'я сутності змінено, щоб не конфліктувати з ключовим словом ORDER у запитах
@Entity(name = "LunchOrder")
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "guest_name")
    private String guestName;

    @Column(name = "guest_phone")
    private String guestPhone;

    @Column(name = "guest_email")
    private String guestEmail;

    @Column(name = "group_order_id")
    private Long groupOrderId;

    @Column(name = "delivery_address", nullable = false)
    private String deliveryAddress;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.NEW;

    @Column(nullable = false)
    private double subtotal;

    @Column(nullable = false)
    private double discount;

    @Column(name = "bonus_used", nullable = false)
    private int bonusUsed;

    @Column(nullable = false)
    private double total;

    private String comment;

    // заповнюється самою БД (DEFAULT CURRENT_TIMESTAMP)
    @Column(name = "created_at", insertable = false, updatable = false)
    private String createdAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<OrderItem> items = new ArrayList<>();

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getGuestName() { return guestName; }
    public void setGuestName(String guestName) { this.guestName = guestName; }
    public String getGuestPhone() { return guestPhone; }
    public void setGuestPhone(String guestPhone) { this.guestPhone = guestPhone; }
    public String getGuestEmail() { return guestEmail; }
    public void setGuestEmail(String guestEmail) { this.guestEmail = guestEmail; }
    public Long getGroupOrderId() { return groupOrderId; }
    public void setGroupOrderId(Long groupOrderId) { this.groupOrderId = groupOrderId; }
    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }
    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
    public double getSubtotal() { return subtotal; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }
    public double getDiscount() { return discount; }
    public void setDiscount(double discount) { this.discount = discount; }
    public int getBonusUsed() { return bonusUsed; }
    public void setBonusUsed(int bonusUsed) { this.bonusUsed = bonusUsed; }
    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public String getCreatedAt() { return createdAt; }
    public List<OrderItem> getItems() { return items; }
}