package com.artverse.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PurchaseHistoryResponse {

    private String artworkTitle;
    private String sellerName;
    private BigDecimal price;
    private LocalDateTime purchaseDate;

    public PurchaseHistoryResponse(String artworkTitle,
                                   String sellerName,
                                   BigDecimal price,
                                   LocalDateTime purchaseDate) {

        this.artworkTitle = artworkTitle;
        this.sellerName = sellerName;
        this.price = price;
        this.purchaseDate = purchaseDate;
    }

    public String getArtworkTitle() {
        return artworkTitle;
    }

    public String getSellerName() {
        return sellerName;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public LocalDateTime getPurchaseDate() {
        return purchaseDate;
    }
}