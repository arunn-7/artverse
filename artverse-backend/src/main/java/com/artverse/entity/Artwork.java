package com.artverse.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "artworks")
public class Artwork {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(length = 1000)
    private String description;

    private String imageUrl;

    private String category;

    private boolean forSale = false;

    private BigDecimal price = BigDecimal.ZERO;

    private String currency = "INR";

    @Enumerated(EnumType.STRING)
    private ArtworkStatus status = ArtworkStatus.AVAILABLE;

    public void setStatus(ArtworkStatus status) {
        this.status = status;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public void setForSale(boolean forSale) {
        this.forSale = forSale;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ArtworkStatus getStatus() {
        return status;
    }

    public String getCurrency() {
        return currency;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public boolean isForSale() {
        return forSale;
    }

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    public Artwork() {
    }

    public Long getId() {
        return id;
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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}