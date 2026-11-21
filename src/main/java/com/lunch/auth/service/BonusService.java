package com.lunch.auth.service;

import com.lunch.auth.model.BonusTransaction;
import com.lunch.auth.model.User;
import com.lunch.auth.repository.BonusTransactionRepository;
import com.lunch.auth.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BonusService {

    private final BonusTransactionRepository transactionRepository;
    private final UserRepository userRepository;

    public BonusService(BonusTransactionRepository transactionRepository,
                        UserRepository userRepository) {
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<BonusTransaction> getHistory(Long userId) {
        return transactionRepository.findByUserIdOrderByIdDesc(userId);
    }

    /** Нарахування (amount > 0) або списання (amount < 0) бонусів. Використаємо в замовленнях. */
    @Transactional
    public void changeBalance(Long userId, int amount, String reason, Long orderId) {
        User user = userRepository.findById(userId).orElseThrow();
        int newBalance = user.getBonusBalance() + amount;
        if (newBalance < 0) {
            throw new IllegalArgumentException("Недостатньо бонусів");
        }
        user.setBonusBalance(newBalance);

        BonusTransaction transaction = new BonusTransaction();
        transaction.setUserId(userId);
        transaction.setOrderId(orderId);
        transaction.setAmount(amount);
        transaction.setReason(reason);
        transactionRepository.save(transaction);
    }
}