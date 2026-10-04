package com.artverse.dto;

import com.artverse.entity.CommissionOfferStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CommissionOfferResponse {

    private Long id;
    private Long commissionId;
    private Long artistId;
    private String artistName;
    private BigDecimal proposedFee;
    private Integer estimatedDays;
    private String message;
    private CommissionOfferStatus status;
    private LocalDateTime createdAt;

    public CommissionOfferResponse(
            Long id,
            Long commissionId,
            Long artistId,
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

    public Long getId() {
        return id;
    }

    public Long getCommissionId() {
        return commissionId;
    }

    public Long getArtistId() {
        return artistId;
    }

    public String getArtistName() {
        return artistName;
    }

    public BigDecimal getProposedFee() {
        return proposedFee;
    }

    public Integer getEstimatedDays() {
        return estimatedDays;
    }

    public String getMessage() {
        return message;
    }

    public CommissionOfferStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}