package com.artverse.model;

import java.math.BigDecimal;

public class FirestoreBid {

    private String id;
    private String auctionId;

    private String bidderUid;
    private String bidderEmail;
    private String bidderName;

    private BigDecimal amount;
    private String createdAt;

    public FirestoreBid() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAuctionId() {
        return auctionId;
    }

    public void setAuctionId(String auctionId) {
        this.auctionId = auctionId;
    }

    public String getBidderUid() {
        return bidderUid;
    }

    public void setBidderUid(String bidderUid) {
        this.bidderUid = bidderUid;
    }

    public String getBidderEmail() {
        return bidderEmail;
    }

    public void setBidderEmail(String bidderEmail) {
        this.bidderEmail = bidderEmail;
    }

    public String getBidderName() {
        return bidderName;
    }

    public void setBidderName(String bidderName) {
        this.bidderName = bidderName;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}