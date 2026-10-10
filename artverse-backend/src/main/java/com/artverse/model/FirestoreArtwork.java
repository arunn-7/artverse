package com.artverse.model;

import java.math.BigDecimal;

public class FirestoreArtwork {

    private String id;
    private String title;
    private String description;
    private String imageUrl;
    private String category;

    private boolean forSale;
    private BigDecimal price = BigDecimal.ZERO;
    private String currency = "INR";
    private String status = "AVAILABLE";

    private String artistUid;
    private String artistName;
    private String artistProfileImageUrl;

    public FirestoreArtwork() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public boolean isForSale() {
        return forSale;
    }

    public void setForSale(boolean forSale) {
        this.forSale = forSale;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getArtistUid() {
        return artistUid;
    }

    public void setArtistUid(String artistUid) {
        this.artistUid = artistUid;
    }

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public String getArtistProfileImageUrl() {
        return artistProfileImageUrl;
    }

    public void setArtistProfileImageUrl(
            String artistProfileImageUrl) {
        this.artistProfileImageUrl = artistProfileImageUrl;
    }
}