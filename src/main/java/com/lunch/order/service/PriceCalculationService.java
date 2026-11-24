package com.lunch.order.service;

import com.lunch.order.model.CartLine;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PriceCalculationService {

    public double subtotal(List<CartLine> lines) {
        double sum = lines.stream().mapToDouble(CartLine::getLineTotal).sum();
        return round2(sum);
    }

    public double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}