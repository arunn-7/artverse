package com.artverse.controller;

import com.artverse.entity.Auction;
import com.artverse.entity.Bid;
import com.artverse.service.AuctionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.artverse.dto.AuctionResponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/auctions")
public class AuctionController {

    @Autowired
    private AuctionService auctionService;


    // ==========================================
    // CREATE AUCTION
    // ==========================================

    @PostMapping("/create")
    public String createAuction(
            @RequestParam Long artworkId,
            @RequestParam BigDecimal startingPrice,
            @RequestParam LocalDateTime startTime,
            @RequestParam LocalDateTime endTime,
            Authentication authentication) {

        return auctionService.createAuction(
                artworkId,
                startingPrice,
                startTime,
                endTime,
                authentication
        );
    }


    // ==========================================
    // PLACE BID
    // ==========================================

    @PostMapping("/{auctionId}/bid")
    public String placeBid(
            @PathVariable Long auctionId,
            @RequestParam BigDecimal amount,
            Authentication authentication) {

        return auctionService.placeBid(
                auctionId,
                amount,
                authentication
        );
    }


    // ==========================================
    // GET AUCTION
    // ==========================================

    @GetMapping("/{auctionId}")
    public AuctionResponse getAuction(
            @PathVariable Long auctionId) {

        return auctionService.getAuctionResponse(auctionId);
    }


    // ==========================================
    // GET BIDS
    // ==========================================

    @GetMapping("/{auctionId}/bids")
    public List<Bid> getAuctionBids(
            @PathVariable Long auctionId) {

        return auctionService.getAuctionBids(auctionId);
    }


    // ==========================================
    // END AUCTION
    // ==========================================

    @PostMapping("/{auctionId}/end")
    public String endAuction(
            @PathVariable Long auctionId) {

        return auctionService.endAuction(auctionId);
    }

    @GetMapping
    public List<AuctionResponse> getActiveAuctions() {

        return auctionService.getActiveAuctions();
    }

    @GetMapping("/upcoming")
    public List<AuctionResponse> getUpcomingAuctions() {
        return auctionService.getUpcomingAuctions();
    }
}