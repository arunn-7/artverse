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

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @Autowired
    private RazorpayService razorpayService;

    @Autowired
    private ArtworkRepository artworkRepository;

    @Autowired
    private PurchaseService purchaseService;

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

        boolean valid = razorpayService.verifyPayment(
                request.getRazorpayOrderId(),
                request.getRazorpayPaymentId(),
                request.getRazorpaySignature(),
                request.getArtworkId(),
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
}