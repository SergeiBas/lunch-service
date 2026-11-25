package com.lunch.order.repository;

import com.lunch.order.model.GroupOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GroupOrderRepository extends JpaRepository<GroupOrder, Long> {
    Optional<GroupOrder> findByRoomCode(String roomCode);
    boolean existsByRoomCode(String roomCode);
    List<GroupOrder> findByCollectorIdOrderByIdDesc(Long collectorId);
}