package com.lunch.order.model;

import jakarta.persistence.*;

@Entity
@Table(name = "group_orders")
public class GroupOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "collector_id", nullable = false)
    private Long collectorId;

    @Column(nullable = false)
    private String title;

    @Column(name = "room_code", nullable = false, unique = true)
    private String roomCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GroupOrderStatus status = GroupOrderStatus.OPEN;

    // заповнюється самою БД (DEFAULT CURRENT_TIMESTAMP)
    @Column(name = "created_at", insertable = false, updatable = false)
    private String createdAt;

    public boolean isOpen() {
        return status == GroupOrderStatus.OPEN;
    }

    public Long getId() { return id; }
    public Long getCollectorId() { return collectorId; }
    public void setCollectorId(Long collectorId) { this.collectorId = collectorId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getRoomCode() { return roomCode; }
    public void setRoomCode(String roomCode) { this.roomCode = roomCode; }
    public GroupOrderStatus getStatus() { return status; }
    public void setStatus(GroupOrderStatus status) { this.status = status; }
    public String getCreatedAt() { return createdAt; }
}