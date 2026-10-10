package com.artverse.dto;

import java.util.List;

public class PublicUserProfileResponse {

    private String id;
    private String fullName;
    private String bio;
    private String profileImageUrl;

    private long artworkCount;
    private long followers;
    private long following;

    private boolean isFollowing;

    private List<ArtworkFeedResponse> artworks;

    public PublicUserProfileResponse() {
    }

    public PublicUserProfileResponse(
            String id,
            String fullName,
            String bio,
            String profileImageUrl,
            long artworkCount,
            long followers,
            long following,
            boolean isFollowing,
            List<ArtworkFeedResponse> artworks) {

        this.id = id;
        this.fullName = fullName;
        this.bio = bio;
        this.profileImageUrl = profileImageUrl;
        this.artworkCount = artworkCount;
        this.followers = followers;
        this.following = following;
        this.isFollowing = isFollowing;
        this.artworks = artworks;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
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

    public long getFollowers() {
        return followers;
    }

    public void setFollowers(long followers) {
        this.followers = followers;
    }

    public long getFollowing() {
        return following;
    }

    public void setFollowing(long following) {
        this.following = following;
    }

    public boolean isFollowing() {
        return isFollowing;
    }

    public void setFollowing(boolean following) {
        this.isFollowing = following;
    }

    public List<ArtworkFeedResponse> getArtworks() {
        return artworks;
    }

    public void setArtworks(List<ArtworkFeedResponse> artworks) {
        this.artworks = artworks;
    }
}