package com.artverse.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "commission_offers")
public class CommissionOffer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Commission request
    @ManyToOne
    @JoinColumn(name = "commission_id", nullable = false)
    private Commission commission;

    // Artist making the offer
    @ManyToOne
    @JoinColumn(name = "artist_id", nullable = false)
    private User artist;

    // Artist's proposed fee
    @Column(nullable = false)
    private BigDecimal proposedFee;

    // Number of days artist needs
    @Column(nullable = false)
    private Integer estimatedDays;

    // Optional message from artist
    @Column(columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CommissionOfferStatus status =
            CommissionOfferStatus.OFFERED;

    private LocalDateTime createdAt;


    public CommissionOffer() {
    }


    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public Commission getCommission() {
        return commission;
    }

    public void setCommission(Commission commission) {
        this.commission = commission;
    }


    public User getArtist() {
        return artist;
    }

    public void setArtist(User artist) {
        this.artist = artist;
    }


    public BigDecimal getProposedFee() {
        return proposedFee;
    }

    public void setProposedFee(BigDecimal proposedFee) {
        this.proposedFee = proposedFee;
    }


    public Integer getEstimatedDays() {
        return estimatedDays;
    }

    public void setEstimatedDays(Integer estimatedDays) {
        this.estimatedDays = estimatedDays;
    }


    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }


    public CommissionOfferStatus getStatus() {
        return status;
    }

    public void setStatus(CommissionOfferStatus status) {
        this.status = status;
    }


    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}