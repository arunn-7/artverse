package com.artverse.dto;

import java.time.LocalDateTime;

public class AdminArtistDetailsResponse {

    private String id;
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

    public AdminArtistDetailsResponse() {
    }

    public AdminArtistDetailsResponse(
            String id,
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

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAccountType() { return accountType; }
    public void setAccountType(String accountType) { this.accountType = accountType; }

    public String getArtistLevel() { return artistLevel; }
    public void setArtistLevel(String artistLevel) { this.artistLevel = artistLevel; }

    public String getVerificationStatus() { return verificationStatus; }
    public void setVerificationStatus(String verificationStatus) {
        this.verificationStatus = verificationStatus;
    }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getProfileImageUrl() { return profileImageUrl; }
    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public Long getArtworkCount() { return artworkCount; }
    public void setArtworkCount(Long artworkCount) {
        this.artworkCount = artworkCount;
    }

    public Long getFollowers() { return followers; }
    public void setFollowers(Long followers) { this.followers = followers; }

    public Long getFollowing() { return following; }
    public void setFollowing(Long following) { this.following = following; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}