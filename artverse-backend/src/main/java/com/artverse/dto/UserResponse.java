package com.artverse.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;


public class UserResponse {

    private Long id;
    private String fullName;
    private String email;
    private String role;
    private LocalDateTime createdAt;

    private String bio;
    private String profileImageUrl;

    // Artist-only fields
    private Long artworkCount;
    private Long followers;
    private Long following;

    public UserResponse() {
    }

    public UserResponse(
            Long id,
            String fullName,
            String email,
            String role,
            String bio,
            String profileImageUrl,
            LocalDateTime createdAt) {

        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.bio = bio;
        this.profileImageUrl = profileImageUrl;
        this.createdAt = createdAt;
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

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
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

    public Long getArtworkCount() {
        return artworkCount;
    }

    public void setArtworkCount(Long artworkCount) {
        this.artworkCount = artworkCount;
    }

    public Long getFollowers() {
        return followers;
    }

    public void setFollowers(Long followers) {
        this.followers = followers;
    }

    public Long getFollowing() {
        return following;
    }

    public void setFollowing(Long following) {
        this.following = following;
    }
}