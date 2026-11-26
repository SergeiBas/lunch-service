package com.lunch.order.model;

public class PriceBreakdown {

    private final double subtotal;
    private final int discountPercent;
    private final double discount;
    private final int bonusUsed;
    private final double total;

    public PriceBreakdown(double subtotal, int discountPercent, double discount,
                          int bonusUsed, double total) {
        this.subtotal = subtotal;
        this.discountPercent = discountPercent;
        this.discount = discount;
        this.bonusUsed = bonusUsed;
        this.total = total;
    }

    public double getSubtotal() { return subtotal; }
    public int getDiscountPercent() { return discountPercent; }
    public double getDiscount() { return discount; }
    public int getBonusUsed() { return bonusUsed; }
    public double getTotal() { return total; }
}