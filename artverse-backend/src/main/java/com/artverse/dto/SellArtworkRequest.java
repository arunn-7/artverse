package com.artverse.dto;

import java.math.BigDecimal;

public class SellArtworkRequest {

    private BigDecimal price;

    public SellArtworkRequest() {
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}