package com.artverse.dto;

import jakarta.validation.constraints.NotBlank;

public class CommissionDeliveryRequest {

    @NotBlank
    private String fileUrl;

    private String message;

    public CommissionDeliveryRequest() {
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
}