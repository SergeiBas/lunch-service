package com.lunch.order.service;

import com.lunch.order.model.CartLine;
import com.lunch.order.model.PriceBreakdown;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PriceCalculationService {

    public static final int BONUS_ACCRUAL_PERCENT = 5;

    public double subtotal(List<CartLine> lines) {
        double sum = lines.stream().mapToDouble(CartLine::getLineTotal).sum();
        return round2(sum);
    }

    /** Відсоток групової знижки за загальною кількістю порцій. Разові замовлення знижки не мають. */
    public int discountPercent(int totalQuantity, boolean group) {
        if (!group) {
            return 0;
        }
        if (totalQuantity >= 10) {
            return 10;
        }
        if (totalQuantity >= 5) {
            return 5;
        }
        return 0;
    }

    /** Скільки бонусів можна списати: не більше балансу й не більше суми після знижки (цілі гривні). */
    public int maxBonus(double amountAfterDiscount, int bonusBalance) {
        return Math.max(0, Math.min(bonusBalance, (int) Math.floor(amountAfterDiscount)));
    }

    /** Підсумкова вартість: спочатку знижка, потім списання бонусів. */
    public PriceBreakdown calculate(double subtotal, int totalQuantity, boolean group,
                                    int requestedBonus, int bonusBalance) {
        double sub = round2(subtotal);
        int percent = discountPercent(totalQuantity, group);
        double discount = round2(sub * percent / 100.0);
        double afterDiscount = round2(sub - discount);

        int bonus = Math.min(Math.max(requestedBonus, 0), maxBonus(afterDiscount, bonusBalance));
        double total = round2(afterDiscount - bonus);

        return new PriceBreakdown(sub, percent, discount, bonus, total);
    }

    /** Скільки бонусів нараховується за замовлення (округлення вниз). */
    public int bonusEarned(double total) {
        if (total <= 0) {
            return 0;
        }
        return (int) Math.floor(total * BONUS_ACCRUAL_PERCENT / 100.0);
    }

    public double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}