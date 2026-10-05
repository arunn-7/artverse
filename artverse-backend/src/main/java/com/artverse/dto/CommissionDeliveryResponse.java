package com.artverse.dto;

import java.time.LocalDateTime;

public class CommissionDeliveryResponse {

    private Long id;
    private Long commissionId;
    private String artistName;
    private String fileUrl;
    private String message;
    private LocalDateTime submittedAt;

    public CommissionDeliveryResponse(
            Long id,
            Long commissionId,
            String artistName,
            String fileUrl,
            String message,
            LocalDateTime submittedAt) {

        this.id = id;
        this.commissionId = commissionId;
        this.artistName = artistName;
        this.fileUrl = fileUrl;
        this.message = message;
        this.submittedAt = submittedAt;
    }

    public Long getId() {
        return id;
    }

    public Long getCommissionId() {
        return commissionId;
    }

    public String getArtistName() {
        return artistName;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }
}