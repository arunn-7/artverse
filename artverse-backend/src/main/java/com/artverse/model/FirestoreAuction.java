package com.artverse.model;

import java.math.BigDecimal;

public class FirestoreAuction {

    private String id;
    private String artworkId;
    private String artworkTitle;
    private String artworkImageUrl;

    private String artistUid;
    private String artistEmail;
    private String artistName;

    private BigDecimal startingPrice;
    private BigDecimal currentHighestBid;

    private String startTime;
    private String endTime;
    private String status;

    private String winnerUid;
    private String winnerEmail;
    private String winnerName;

    private Long totalBids;
    private String createdAt;
    private String updatedAt;

    public FirestoreAuction() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getArtworkId() {
        return artworkId;
    }

    public void setArtworkId(String artworkId) {
        this.artworkId = artworkId;
    }

    public String getArtworkTitle() {
        return artworkTitle;
    }

    public void setArtworkTitle(String artworkTitle) {
        this.artworkTitle = artworkTitle;
    }

    public String getArtworkImageUrl() {
        return artworkImageUrl;
    }

    public void setArtworkImageUrl(String artworkImageUrl) {
        this.artworkImageUrl = artworkImageUrl;
    }

    public String getArtistUid() {
        return artistUid;
    }

    public void setArtistUid(String artistUid) {
        this.artistUid = artistUid;
    }

    public String getArtistEmail() {
        return artistEmail;
    }

    public void setArtistEmail(String artistEmail) {
        this.artistEmail = artistEmail;
    }

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public BigDecimal getStartingPrice() {
        return startingPrice;
    }

    public void setStartingPrice(BigDecimal startingPrice) {
        this.startingPrice = startingPrice;
    }

    public BigDecimal getCurrentHighestBid() {
        return currentHighestBid;
    }

    public void setCurrentHighestBid(BigDecimal currentHighestBid) {
        this.currentHighestBid = currentHighestBid;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getWinnerUid() {
        return winnerUid;
    }

    public void setWinnerUid(String winnerUid) {
        this.winnerUid = winnerUid;
    }

    public String getWinnerEmail() {
        return winnerEmail;
    }

    public void setWinnerEmail(String winnerEmail) {
        this.winnerEmail = winnerEmail;
    }

    public String getWinnerName() {
        return winnerName;
    }

    public void setWinnerName(String winnerName) {
        this.winnerName = winnerName;
    }

    public Long getTotalBids() {
        return totalBids;
    }

    public void setTotalBids(Long totalBids) {
        this.totalBids = totalBids;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}