package com.artverse.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class FirestoreCommissionOffer {

    private String id;
    private String commissionId;
    private String artistUid;
    private String artistName;
    private BigDecimal proposedFee;
    private Integer estimatedDays;
    private String message;
    private CommissionOfferStatus status = CommissionOfferStatus.OFFERED;
    private LocalDateTime createdAt;

    public FirestoreCommissionOffer() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getCommissionId() { return commissionId; }
    public void setCommissionId(String commissionId) { this.commissionId = commissionId; }
    public String getArtistUid() { return artistUid; }
    public void setArtistUid(String artistUid) { this.artistUid = artistUid; }
    public String getArtistName() { return artistName; }
    public void setArtistName(String artistName) { this.artistName = artistName; }
    public BigDecimal getProposedFee() { return proposedFee; }
    public void setProposedFee(BigDecimal proposedFee) { this.proposedFee = proposedFee; }
    public Integer getEstimatedDays() { return estimatedDays; }
    public void setEstimatedDays(Integer estimatedDays) { this.estimatedDays = estimatedDays; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public CommissionOfferStatus getStatus() { return status; }
    public void setStatus(CommissionOfferStatus status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}