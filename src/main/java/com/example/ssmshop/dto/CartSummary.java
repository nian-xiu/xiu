package com.example.ssmshop.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class CartSummary {
    private List<CartLine> lines = new ArrayList<>();
    private BigDecimal totalAmount = BigDecimal.ZERO;
    private int totalQuantity;

    public List<CartLine> getLines() {
        return lines;
    }

    public void setLines(List<CartLine> lines) {
        this.lines = lines;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public int getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(int totalQuantity) {
        this.totalQuantity = totalQuantity;
    }
}
