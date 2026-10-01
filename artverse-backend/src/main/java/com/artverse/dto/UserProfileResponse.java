package com.artverse.dto;

public class UserProfileResponse {

    private Long id;
    private String fullName;
    private String email;
    private String bio;
    private String profileImageUrl;
    private long artworkCount;

    public UserProfileResponse() {
    }

    public UserProfileResponse(Long id,
                               String fullName,
                               String email,
                               String bio,
                               String profileImageUrl,
                               long artworkCount) {

        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.bio = bio;
        this.profileImageUrl = profileImageUrl;
        this.artworkCount = artworkCount;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public long getArtworkCount() {
        return artworkCount;
    }

    public void setArtworkCount(long artworkCount) {
        this.artworkCount = artworkCount;
    }
}