package com.artverse.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AuctionResponse {

    private Long auctionId;
    private Long artworkId;

    private String title;
    private String description;
    private String imageUrl;
    private String category;

    private Long artistId;
    private String artistName;

    private BigDecimal startingPrice;
    private BigDecimal currentHighestBid;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private String status;

    private Long totalBids;


    public AuctionResponse(
            Long auctionId,
            Long artworkId,
            String title,
            String description,
            String imageUrl,
            String category,
            Long artistId,
            String artistName,
            BigDecimal startingPrice,
            BigDecimal currentHighestBid,
            LocalDateTime startTime,
            LocalDateTime endTime,
            String status,
            Long totalBids) {

        this.auctionId = auctionId;
        this.artworkId = artworkId;
        this.title = title;
        this.description = description;
        this.imageUrl = imageUrl;
        this.category = category;
        this.artistId = artistId;
        this.artistName = artistName;
        this.startingPrice = startingPrice;
        this.currentHighestBid = currentHighestBid;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        this.totalBids = totalBids;
    }


    public Long getAuctionId() {
        return auctionId;
    }

    public Long getArtworkId() {
        return artworkId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getCategory() {
        return category;
    }

    public Long getArtistId() {
        return artistId;
    }

    public String getArtistName() {
        return artistName;
    }

    public BigDecimal getStartingPrice() {
        return startingPrice;
    }

    public BigDecimal getCurrentHighestBid() {
        return currentHighestBid;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public String getStatus() {
        return status;
    }

    public Long getTotalBids() {
        return totalBids;
    }
}