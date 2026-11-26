package com.lunch.auth.repository;

import com.lunch.auth.model.BonusTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BonusTransactionRepository extends JpaRepository<BonusTransaction, Long> {
    List<BonusTransaction> findByUserIdOrderByIdDesc(Long userId);
    boolean existsByOrderIdAndAmountGreaterThan(Long orderId, int amount);
}