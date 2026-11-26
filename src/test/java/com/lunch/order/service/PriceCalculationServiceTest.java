package com.lunch.order.service;

import com.lunch.order.model.PriceBreakdown;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PriceCalculationServiceTest {

    private final PriceCalculationService service = new PriceCalculationService();

    // ---------- групова знижка ----------

    @Test
    void discountPercent_individualOrderNeverGetsDiscount() {
        assertThat(service.discountPercent(50, false)).isZero();
    }

    @Test
    void discountPercent_groupTiers() {
        assertThat(service.discountPercent(4, true)).isZero();
        assertThat(service.discountPercent(5, true)).isEqualTo(5);
        assertThat(service.discountPercent(9, true)).isEqualTo(5);
        assertThat(service.discountPercent(10, true)).isEqualTo(10);
        assertThat(service.discountPercent(30, true)).isEqualTo(10);
    }

    @Test
    void calculate_groupOrderAppliesDiscount() {
        PriceBreakdown price = service.calculate(1000.0, 10, true, 0, 0);

        assertThat(price.getDiscountPercent()).isEqualTo(10);
        assertThat(price.getDiscount()).isEqualTo(100.0);
        assertThat(price.getTotal()).isEqualTo(900.0);
    }

    @Test
    void calculate_individualOrderHasNoDiscount() {
        PriceBreakdown price = service.calculate(1000.0, 10, false, 0, 0);

        assertThat(price.getDiscount()).isZero();
        assertThat(price.getTotal()).isEqualTo(1000.0);
    }

    // ---------- бонуси ----------

    @Test
    void calculate_bonusUsedFullyWhenWithinLimits() {
        PriceBreakdown price = service.calculate(100.0, 2, false, 30, 100);

        assertThat(price.getBonusUsed()).isEqualTo(30);
        assertThat(price.getTotal()).isEqualTo(70.0);
    }

    @Test
    void calculate_bonusLimitedByBalance() {
        PriceBreakdown price = service.calculate(100.0, 2, false, 50, 30);

        assertThat(price.getBonusUsed()).isEqualTo(30);
        assertThat(price.getTotal()).isEqualTo(70.0);
    }

    @Test
    void calculate_bonusLimitedByOrderAmount() {
        PriceBreakdown price = service.calculate(40.0, 1, false, 100, 100);

        assertThat(price.getBonusUsed()).isEqualTo(40);
        assertThat(price.getTotal()).isZero();
    }

    @Test
    void calculate_negativeBonusRequestIsIgnored() {
        PriceBreakdown price = service.calculate(100.0, 2, false, -20, 100);

        assertThat(price.getBonusUsed()).isZero();
        assertThat(price.getTotal()).isEqualTo(100.0);
    }

    @Test
    void calculate_bonusIsAppliedAfterDiscount() {
        PriceBreakdown price = service.calculate(1000.0, 10, true, 100, 1000);

        assertThat(price.getDiscount()).isEqualTo(100.0);
        assertThat(price.getBonusUsed()).isEqualTo(100);
        assertThat(price.getTotal()).isEqualTo(800.0);
    }

    @Test
    void maxBonus_roundsAmountDownToWholeHryvnias() {
        assertThat(service.maxBonus(99.99, 500)).isEqualTo(99);
        assertThat(service.maxBonus(99.99, 20)).isEqualTo(20);
        assertThat(service.maxBonus(0.0, 20)).isZero();
    }

    // ---------- нарахування ----------

    @Test
    void bonusEarned_isFivePercentRoundedDown() {
        assertThat(service.bonusEarned(200.0)).isEqualTo(10);
        assertThat(service.bonusEarned(199.99)).isEqualTo(9);
        assertThat(service.bonusEarned(10.0)).isZero();
        assertThat(service.bonusEarned(0.0)).isZero();
    }
}