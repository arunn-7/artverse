package com.artverse.controller;

import com.artverse.entity.Artwork;
import com.artverse.exception.ArtworkNotFoundException;
import com.artverse.repository.ArtworkRepository;
import com.artverse.service.RazorpayService;
import com.razorpay.Order;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.artverse.dto.PaymentVerificationRequest;
import com.artverse.service.PurchaseService;
import com.artverse.entity.Auction;
import com.artverse.entity.AuctionStatus;
import com.artverse.entity.Bid;
import com.artverse.repository.AuctionRepository;
import com.artverse.repository.BidRepository;

import java.math.BigDecimal;
@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @Autowired
    private RazorpayService razorpayService;

    @Autowired
    private ArtworkRepository artworkRepository;

    @Autowired
    private PurchaseService purchaseService;

    @Autowired
    private AuctionRepository auctionRepository;

    @Autowired
    private BidRepository bidRepository;

    @PostMapping("/create-order")
    public String createOrder(
            @RequestParam Long artworkId,
            Authentication authentication) throws Exception {

        Artwork artwork = artworkRepository.findById(artworkId)
                .orElseThrow(() ->
                        new ArtworkNotFoundException("Artwork not found"));

        if (!artwork.isForSale()) {
            throw new RuntimeException("Artwork is not for sale");
        }

        if (artwork.getStatus() != com.artverse.entity.ArtworkStatus.AVAILABLE) {
            throw new RuntimeException("Artwork is not available");
        }

        if (artwork.getUser().getEmail().equals(authentication.getName())) {
            throw new RuntimeException("You cannot buy your own artwork");
        }

        long amountInPaise = artwork.getPrice()
                .multiply(java.math.BigDecimal.valueOf(100))
                .longValueExact();

        String receipt = "artwork_" + artworkId + "_"
                + System.currentTimeMillis();

        Order order = razorpayService.createOrder(
                amountInPaise,
                receipt
        );

        JSONObject response = new JSONObject();

        response.put("orderId", (Object) order.get("id"));
        response.put("amount", (Object) order.get("amount"));
        response.put("currency", (Object) order.get("currency"));
        response.put("keyId", razorpayService.getKeyId());
        response.put("artworkId", artworkId);

        return response.toString();
    }
    @PostMapping("/verify")
    public String verifyPayment(
            @RequestBody PaymentVerificationRequest request,
            Authentication authentication) throws Exception {

        Artwork artwork = artworkRepository.findById(request.getArtworkId())
                .orElseThrow(() ->
                        new ArtworkNotFoundException("Artwork not found"));

        long expectedAmountInPaise = artwork.getPrice()
                .multiply(java.math.BigDecimal.valueOf(100))
                .longValueExact();

        boolean valid = razorpayService.verifyAuctionPayment(
                request.getRazorpayOrderId(),
                request.getRazorpayPaymentId(),
                request.getRazorpaySignature(),
                request.getAuctionId(),
                expectedAmountInPaise
        );

        if (!valid) {
            throw new RuntimeException("Payment verification failed");
        }

        return purchaseService.buyArtwork(
                request.getArtworkId(),
                authentication
        );
    }
    @PostMapping("/auction/create-order")
    public String createAuctionOrder(
            @RequestParam Long auctionId,
            Authentication authentication) throws Exception {

        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new RuntimeException("Auction not found"));

        // Auction must be ended
        if (auction.getStatus() != AuctionStatus.ENDED) {
            throw new RuntimeException("Auction has not ended yet");
        }

        // Find highest bid
        Bid highestBid = bidRepository
                .findTopByAuctionOrderByAmountDesc(auction)
                .orElseThrow(() ->
                        new RuntimeException("No bids were placed on this auction"));

        // Check logged-in user is the winner
        if (!highestBid.getBidder().getEmail()
                .equals(authentication.getName())) {

            throw new RuntimeException(
                    "Only the auction winner can make this payment");
        }

        BigDecimal winningAmount = highestBid.getAmount();

        long amountInPaise = winningAmount
                .multiply(BigDecimal.valueOf(100))
                .longValueExact();

        String receipt = "auction_" + auctionId + "_"
                + System.currentTimeMillis();

        Order order = razorpayService.createOrder(
                amountInPaise,
                receipt
        );

        JSONObject response = new JSONObject();

        response.put("orderId", (Object) order.get("id"));
        response.put("amount", (Object) order.get("amount"));
        response.put("currency", (Object) order.get("currency"));
        response.put("keyId", razorpayService.getKeyId());
        response.put("auctionId", auctionId);
        response.put("artworkId", auction.getArtwork().getId());
        response.put("winningBid", winningAmount);
        response.put("winner", highestBid.getBidder().getFullName());

        return response.toString();
    }
    @PostMapping("/auction/verify")
    public String verifyAuctionPayment(
            @RequestBody PaymentVerificationRequest request,
            Authentication authentication) throws Exception {

        // 1. Find auction
        Auction auction = auctionRepository
                .findById(request.getAuctionId())
                .orElseThrow(() ->
                        new RuntimeException("Auction not found"));


        // 2. Get winning bid
        Bid winningBid = bidRepository
                .findTopByAuctionOrderByAmountDesc(auction)
                .orElseThrow(() ->
                        new RuntimeException("Winning bid not found"));


        // 3. Winning amount in paise
        long expectedAmountInPaise =
                winningBid.getAmount()
                        .multiply(java.math.BigDecimal.valueOf(100))
                        .longValueExact();


        // 4. Verify Razorpay payment
        boolean valid = razorpayService.verifyPayment(
                request.getRazorpayOrderId(),
                request.getRazorpayPaymentId(),
                request.getRazorpaySignature(),
                auction.getArtwork().getId(),
                expectedAmountInPaise
        );


        // 5. Stop if payment verification fails
        if (!valid) {
            throw new RuntimeException(
                    "Auction payment verification failed"
            );
        }


        // 6. Complete purchase
        return purchaseService.completeAuctionPurchase(
                request.getAuctionId(),
                authentication
        );
    }
}