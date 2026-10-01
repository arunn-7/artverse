package com.artverse.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class SalesHistoryResponse {

    private String artworkTitle;
    private String buyerName;
    private BigDecimal price;
    private LocalDateTime purchaseDate;

    public SalesHistoryResponse(String artworkTitle,
                                String buyerName,
                                BigDecimal price,
                                LocalDateTime purchaseDate) {
        this.artworkTitle = artworkTitle;
        this.buyerName = buyerName;
        this.price = price;
        this.purchaseDate = purchaseDate;
    }

    public String getArtworkTitle() {
        return artworkTitle;
    }

    public String getBuyerName() {
        return buyerName;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public LocalDateTime getPurchaseDate() {
        return purchaseDate;
    }
}