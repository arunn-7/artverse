package com.artverse.dto;

import java.time.LocalDateTime;

public class AdminCertificateResponse {

    private Long id;

    private Long artistId;
    private String artistName;
    private String artistEmail;
    private String artistLevel;

    private String certificateName;
    private String certificateUrl;
    private String verificationStatus;
    private LocalDateTime uploadedAt;

    public AdminCertificateResponse() {
    }

    public AdminCertificateResponse(
            Long id,
            Long artistId,
            String artistName,
            String artistEmail,
            String artistLevel,
            String certificateName,
            String certificateUrl,
            String verificationStatus,
            LocalDateTime uploadedAt) {

        this.id = id;
        this.artistId = artistId;
        this.artistName = artistName;
        this.artistEmail = artistEmail;
        this.artistLevel = artistLevel;
        this.certificateName = certificateName;
        this.certificateUrl = certificateUrl;
        this.verificationStatus = verificationStatus;
        this.uploadedAt = uploadedAt;
    }

    public Long getId() {
        return id;
    }

    public Long getArtistId() {
        return artistId;
    }

    public String getArtistName() {
        return artistName;
    }

    public String getArtistEmail() {
        return artistEmail;
    }

    public String getArtistLevel() {
        return artistLevel;
    }

    public String getCertificateName() {
        return certificateName;
    }

    public String getCertificateUrl() {
        return certificateUrl;
    }

    public String getVerificationStatus() {
        return verificationStatus;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setArtistId(Long artistId) {
        this.artistId = artistId;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public void setArtistEmail(String artistEmail) {
        this.artistEmail = artistEmail;
    }

    public void setArtistLevel(String artistLevel) {
        this.artistLevel = artistLevel;
    }

    public void setCertificateName(String certificateName) {
        this.certificateName = certificateName;
    }

    public void setCertificateUrl(String certificateUrl) {
        this.certificateUrl = certificateUrl;
    }

    public void setVerificationStatus(String verificationStatus) {
        this.verificationStatus = verificationStatus;
    }

    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }
}