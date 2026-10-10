
package com.artverse.dto;

import com.artverse.entity.CommissionOfferStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CommissionOfferResponse {

    private String id;
    private String commissionId;
    private String artistId;
    private String artistName;
    private BigDecimal proposedFee;
    private Integer estimatedDays;
    private String message;
    private CommissionOfferStatus status;
    private LocalDateTime createdAt;

    public CommissionOfferResponse(
            String id,
            String commissionId,
            String artistId,
            String artistName,
            BigDecimal proposedFee,
            Integer estimatedDays,
            String message,
            CommissionOfferStatus status,
            LocalDateTime createdAt) {

        this.id = id;
        this.commissionId = commissionId;
        this.artistId = artistId;
        this.artistName = artistName;
        this.proposedFee = proposedFee;
        this.estimatedDays = estimatedDays;
        this.message = message;
        this.status = status;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCommissionId() {
        return commissionId;
    }

    public void setCommissionId(String commissionId) {
        this.commissionId = commissionId;
    }

    public String getArtistId() {
        return artistId;
    }

    public void setArtistId(String artistId) {
        this.artistId = artistId;
    }

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
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

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
