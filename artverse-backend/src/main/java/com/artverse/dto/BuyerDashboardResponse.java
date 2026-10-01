package com.artverse.dto;

import java.math.BigDecimal;

public class BuyerDashboardResponse {

    private long totalPurchases;
    private BigDecimal totalSpent;

    public BuyerDashboardResponse(long totalPurchases,
                                  BigDecimal totalSpent) {
        this.totalPurchases = totalPurchases;
        this.totalSpent = totalSpent;
    }

    public long getTotalPurchases() {
        return totalPurchases;
    }

    public BigDecimal getTotalSpent() {
        return totalSpent;
    }
}