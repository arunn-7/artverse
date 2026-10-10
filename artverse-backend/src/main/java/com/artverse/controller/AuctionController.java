package com.artverse.controller;

import com.artverse.model.FirestoreAuction;
import com.artverse.model.FirestoreBid;
import com.artverse.service.FirestoreAuctionService;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/auctions")
public class AuctionController {

    private final FirestoreAuctionService auctionService;

    public AuctionController(FirestoreAuctionService auctionService) {
        this.auctionService = auctionService;
    }

    @PostMapping("/create")
    public String createAuction(
            @RequestParam String artworkId,
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

    @PostMapping("/{auctionId}/bid")
    public String placeBid(
            @PathVariable String auctionId,
            @RequestParam BigDecimal amount,
            Authentication authentication) {

        return auctionService.placeBid(
                auctionId,
                amount,
                authentication
        );
    }

    @GetMapping("/{auctionId}")
    public FirestoreAuction getAuction(
            @PathVariable String auctionId) {

        return auctionService.getAuction(auctionId);
    }

    @GetMapping("/{auctionId}/bids")
    public List<FirestoreBid> getAuctionBids(
            @PathVariable String auctionId) {

        return auctionService.getAuctionBids(auctionId);
    }

    @PostMapping("/{auctionId}/end")
    public String endAuction(
            @PathVariable String auctionId) {

        return auctionService.endAuction(auctionId);
    }

    @GetMapping
    public List<FirestoreAuction> getActiveAuctions() {
        return auctionService.getActiveAuctions();
    }

    @GetMapping("/upcoming")
    public List<FirestoreAuction> getUpcomingAuctions() {
        return auctionService.getUpcomingAuctions();
    }

    @GetMapping("/all")
    public List<FirestoreAuction> getAllAuctions() {
        return auctionService.getAllAuctions();
    }
}