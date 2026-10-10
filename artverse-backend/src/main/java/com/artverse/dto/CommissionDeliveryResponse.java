
package com.artverse.dto;

import java.time.LocalDateTime;

public class CommissionDeliveryResponse {

    private String id;
    private String commissionId;
    private String artistName;
    private String fileUrl;
    private String message;
    private LocalDateTime submittedAt;

    public CommissionDeliveryResponse() {
    }

    public CommissionDeliveryResponse(
            String id,
            String commissionId,
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

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }
}
