package com.artverse.dto;

import java.math.BigDecimal;

public class MarketplaceArtworkResponse {

    private String id;
    private String title;
    private String description;
    private String imageUrl;
    private String category;

    private String artistName;
    private String artistProfileImage;

    private BigDecimal price;
    private String currency;

    public MarketplaceArtworkResponse(
            String id,
            String title,
            String description,
            String imageUrl,
            String category,
            String artistName,
            String artistProfileImage,
            BigDecimal price,
            String currency) {

        this.id = id;
        this.title = title;
        this.description = description;
        this.imageUrl = imageUrl;
        this.category = category;
        this.artistName = artistName;
        this.artistProfileImage = artistProfileImage;
        this.price = price;
        this.currency = currency;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getArtistProfileImage() {
        return artistProfileImage;
    }

    public void setArtistProfileImage(String artistProfileImage) {
        this.artistProfileImage = artistProfileImage;
    }

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public MarketplaceArtworkResponse() {

    }

    // Generate getters
}