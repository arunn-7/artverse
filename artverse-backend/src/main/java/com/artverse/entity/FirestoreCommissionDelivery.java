package com.artverse.entity;

import java.time.LocalDateTime;

public class FirestoreCommissionDelivery {

    private String id;
    private String commissionId;
    private String artistUid;
    private String artistName;
    private String fileUrl;
    private String message;
    private LocalDateTime submittedAt;

    public FirestoreCommissionDelivery() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getCommissionId() { return commissionId; }
    public void setCommissionId(String commissionId) { this.commissionId = commissionId; }
    public String getArtistUid() { return artistUid; }
    public void setArtistUid(String artistUid) { this.artistUid = artistUid; }
    public String getArtistName() { return artistName; }
    public void setArtistName(String artistName) { this.artistName = artistName; }
    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }
}