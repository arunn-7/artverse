package com.artverse.dto;

import java.time.LocalDateTime;

public class AdminArtistDetailsResponse {

    private Long id;
    private String fullName;
    private String email;
    private String accountType;
    private String artistLevel;
    private String verificationStatus;
    private String bio;
    private String profileImageUrl;

    private Long artworkCount;
    private Long followers;
    private Long following;

    private LocalDateTime createdAt;


    // ==========================================
    // DEFAULT CONSTRUCTOR
    // ==========================================

    public AdminArtistDetailsResponse() {
    }


    // ==========================================
    // FULL CONSTRUCTOR
    // ==========================================

    public AdminArtistDetailsResponse(
            Long id,
            String fullName,
            String email,
            String accountType,
            String artistLevel,
            String verificationStatus,
            String bio,
            String profileImageUrl,
            Long artworkCount,
            Long followers,
            Long following,
            LocalDateTime createdAt) {

        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.accountType = accountType;
        this.artistLevel = artistLevel;
        this.verificationStatus = verificationStatus;
        this.bio = bio;
        this.profileImageUrl = profileImageUrl;
        this.artworkCount = artworkCount;
        this.followers = followers;
        this.following = following;
        this.createdAt = createdAt;
    }


    // ==========================================
    // GETTERS
    // ==========================================

    public Long getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getAccountType() {
        return accountType;
    }

    public String getArtistLevel() {
        return artistLevel;
    }

    public String getVerificationStatus() {
        return verificationStatus;
    }

    public String getBio() {
        return bio;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public Long getArtworkCount() {
        return artworkCount;
    }

    public Long getFollowers() {
        return followers;
    }

    public Long getFollowing() {
        return following;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }


    // ==========================================
    // SETTERS
    // ==========================================

    public void setId(Long id) {
        this.id = id;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public void setArtistLevel(String artistLevel) {
        this.artistLevel = artistLevel;
    }

    public void setVerificationStatus(String verificationStatus) {
        this.verificationStatus = verificationStatus;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public void setArtworkCount(Long artworkCount) {
        this.artworkCount = artworkCount;
    }

    public void setFollowers(Long followers) {
        this.followers = followers;
    }

    public void setFollowing(Long following) {
        this.following = following;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}