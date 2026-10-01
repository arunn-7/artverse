package com.artverse.dto;

import java.math.BigDecimal;

public class SellerDashboardResponse {

    private long totalSales;
    private BigDecimal totalRevenue;
    private long activeListings;

    public SellerDashboardResponse(long totalSales,
                                   BigDecimal totalRevenue,
                                   long activeListings) {
        this.totalSales = totalSales;
        this.totalRevenue = totalRevenue;
        this.activeListings = activeListings;
    }

    public long getTotalSales() {
        return totalSales;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public long getActiveListings() {
        return activeListings;
    }
}