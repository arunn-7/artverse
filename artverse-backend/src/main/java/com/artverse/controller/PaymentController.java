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
import com.artverse.entity.Commission;
import com.artverse.entity.CommissionStatus;
import com.artverse.entity.CommissionPaymentStatus;
import com.artverse.repository.CommissionRepository;
import com.artverse.repository.CommissionOfferRepository;
import com.artverse.entity.CommissionOffer;
import com.artverse.entity.CommissionOfferStatus;
import com.artverse.service.NotificationService;
import com.artverse.entity.NotificationType;


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

    @Autowired
    private CommissionRepository commissionRepository;

    @Autowired
    private CommissionOfferRepository commissionOfferRepository;

    @Autowired
    private NotificationService notificationService;

    @PostMapping("/commission/create-order")
    public String createCommissionOrder(
            @RequestParam Long commissionId,
            Authentication authentication) throws Exception {

        Commission commission = commissionRepository.findById(commissionId)
                .orElseThrow(() ->
                        new RuntimeException("Commission not found"));

        // Only the client who created the commission can pay
        if (!commission.getClient().getEmail()
                .equals(authentication.getName())) {

            throw new RuntimeException(
                    "Only the commission owner can make this payment");
        }

        // Commission must have an artist selected
        if (commission.getStatus()
                != com.artverse.entity.CommissionStatus.ARTIST_SELECTED) {

            throw new RuntimeException(
                    "Artist has not been selected");
        }

        // Find selected offer
        CommissionOffer selectedOffer =
                commissionOfferRepository
                        .findByCommission(commission)
                        .stream()
                        .filter(offer ->
                                offer.getStatus()
                                        == CommissionOfferStatus.SELECTED)
                        .findFirst()
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Selected offer not found"));

        // Payment amount = artist's proposed fee
        long amountInPaise =
                selectedOffer.getProposedFee()
                        .multiply(BigDecimal.valueOf(100))
                        .longValueExact();

        String receipt =
                "commission_" + commissionId + "_"
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
        response.put("commissionId", commissionId);
        response.put("offerId", selectedOffer.getId());
        response.put("proposedFee", selectedOffer.getProposedFee());

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
    @PostMapping("/commission/verify")
    public String verifyCommissionPayment(
            @RequestBody PaymentVerificationRequest request,
            Authentication authentication) throws Exception {

        // 1. Find commission
        Commission commission =
                commissionRepository.findById(
                        request.getCommissionId()
                ).orElseThrow(() ->
                        new RuntimeException("Commission not found"));

        // 2. Only the client can make the payment
        if (!commission.getClient().getEmail()
                .equals(authentication.getName())) {

            throw new RuntimeException(
                    "Only the commission client can make payment"
            );
        }

        // 3. Commission must be delivered
        if (commission.getStatus()
                != CommissionStatus.DELIVERED) {

            throw new RuntimeException(
                    "Commission is not ready for payment"
            );
        }

        // 4. Prevent duplicate payment
        if (commission.getPaymentStatus()
                == CommissionPaymentStatus.PAID) {

            throw new RuntimeException(
                    "Commission has already been paid"
            );
        }

        // 5. Find selected offer
        CommissionOffer selectedOffer =
                commissionOfferRepository
                        .findByCommission(commission)
                        .stream()
                        .filter(offer ->
                                offer.getStatus()
                                        == CommissionOfferStatus.SELECTED)
                        .findFirst()
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Selected offer not found"
                                ));

        // 6. Get accepted offer amount
        BigDecimal amount =
                selectedOffer.getProposedFee();

        long expectedAmountInPaise =
                amount
                        .multiply(BigDecimal.valueOf(100))
                        .longValueExact();

        // 7. Verify payment with Razorpay
        boolean valid =
                razorpayService.verifyCommissionPayment(
                        request.getRazorpayOrderId(),
                        request.getRazorpayPaymentId(),
                        request.getRazorpaySignature(),
                        commission.getId(),
                        expectedAmountInPaise
                );

        // 8. Stop if verification fails
        if (!valid) {
            throw new RuntimeException(
                    "Commission payment verification failed"
            );
        }

        // 9. Mark payment as paid
        commission.setPaymentStatus(
                CommissionPaymentStatus.PAID
        );

        // 10. Mark commission as completed
        commission.setStatus(
                CommissionStatus.COMPLETED
        );

        commissionRepository.save(commission);

        // 11. Notify artist
        notificationService.createNotification(
                selectedOffer.getArtist(),
                commission.getClient(),
                "Commission Payment Received",
                "Payment of ₹"
                        + amount
                        + " has been completed for your commission \""
                        + commission.getTitle()
                        + "\".",
                NotificationType.COMMISSION
        );

        return "Commission payment completed successfully";
    }
}