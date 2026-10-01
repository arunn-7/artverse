package com.artverse.dto;

public class UserSummaryResponse {

    private Long id;
    private String fullName;
    private String profileImageUrl;

    public UserSummaryResponse() {
    }

    public UserSummaryResponse(Long id,
                               String fullName,
                               String profileImageUrl) {
        this.id = id;
        this.fullName = fullName;
        this.profileImageUrl = profileImageUrl;
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

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }
}