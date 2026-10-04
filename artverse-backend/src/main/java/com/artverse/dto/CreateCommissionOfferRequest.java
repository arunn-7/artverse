package com.artverse.dto;

import java.math.BigDecimal;

public class CreateCommissionOfferRequest {

    private BigDecimal proposedFee;
    private Integer estimatedDays;
    private String message;

    public CreateCommissionOfferRequest() {
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
}