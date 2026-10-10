package com.artverse.dto;

public class AdminCertificateResponse {

    private String id;
    private String artistId;
    private String uploadedAt;
    private String artistName;
    private String artistEmail;
    private String artistLevel;
    private String certificateName;
    private String certificateUrl;
    private String verificationStatus;

    public AdminCertificateResponse() {
    }

    public AdminCertificateResponse(
            String id,
            String artistId,
            String artistName,
            String artistEmail,
            String artistLevel,
            String certificateName,
            String certificateUrl,
            String verificationStatus,
            String uploadedAt) {

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

    public String getId() {
        return id;
    }

    public String getArtistId() {
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

    public String getUploadedAt() {
        return uploadedAt;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setArtistId(String artistId) {
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

    public void setUploadedAt(String uploadedAt) {
        this.uploadedAt = uploadedAt;
    }
}