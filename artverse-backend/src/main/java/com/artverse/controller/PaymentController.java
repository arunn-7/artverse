
package com.artverse.controller;

import com.artverse.dto.PaymentVerificationRequest;
import com.artverse.entity.*;
import com.artverse.model.FirestoreArtwork;
import com.artverse.model.FirestoreAuction;
import com.artverse.service.*;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.razorpay.Order;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final RazorpayService razorpayService;
    private final PurchaseService purchaseService;
    private final FirestoreArtworkService artworkService;
    private final FirestoreAuctionService auctionService;
    private final FirestoreUserService userService;
    private final Firestore firestore;
    private final NotificationService notificationService;

    private static final String COMMISSIONS = "commissions";
    private static final String OFFERS = "commissionOffers";

    public PaymentController(
            RazorpayService razorpayService,
            PurchaseService purchaseService,
            FirestoreArtworkService artworkService,
            FirestoreAuctionService auctionService,
            FirestoreUserService userService,
            Firestore firestore,
            NotificationService notificationService) {

        this.razorpayService = razorpayService;
        this.purchaseService = purchaseService;
        this.artworkService = artworkService;
        this.auctionService = auctionService;
        this.userService = userService;
        this.firestore = firestore;
        this.notificationService = notificationService;
    }

    // ---------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------

    private String currentEmail(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Authentication required");
        }

        return authentication.getName().trim().toLowerCase(Locale.ROOT);
    }

    private DocumentSnapshot getDocument(
            String collection, String id, String message) {
        try {
            DocumentSnapshot document = firestore.collection(collection)
                    .document(id)
                    .get()
                    .get();

            if (!document.exists()) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND, message);
            }

            return document;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Firestore operation interrupted");
        } catch (ExecutionException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Firestore operation failed");
        }
    }

    private QuerySnapshot query(
            com.google.cloud.firestore.Query query) {
        try {
            return query.get().get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Firestore query interrupted");
        } catch (ExecutionException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Firestore query failed");
        }
    }

    private BigDecimal readMoney(
            DocumentSnapshot document, String field) {
        Object value = document.get(field);

        if (value == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Missing amount field: " + field);
        }

        try {
            return new BigDecimal(value.toString());
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid amount field: " + field);
        }
    }

    private long toPaise(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Payment amount must be positive");
        }

        try {
            return amount.multiply(BigDecimal.valueOf(100))
                    .longValueExact();
        } catch (ArithmeticException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid payment amount precision");
        }
    }

    private JSONObject orderResponse(
            Order order, long amount, String idField, String id) {
        JSONObject response = new JSONObject();
        response.put("orderId", (Object) String.valueOf(order.get("id")));
        response.put("amount", (Object) amount);
        response.put("currency", (Object) "INR");
        response.put("keyId", (Object) razorpayService.getKeyId());
        response.put(idField, (Object) id);
        return response;
    }

    // ---------------------------------------------------------
    // Artwork payment
    // ---------------------------------------------------------

    @PostMapping("/artwork/create-order")
    public String createArtworkOrder(
            @RequestParam String artworkId,
            Authentication authentication) throws Exception {

        String email = currentEmail(authentication);
        FirestoreArtwork artwork = artworkService.getArtworkById(artworkId);

        if (artwork == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Artwork not found");
        }

        if (!artwork.isForSale()
                || !"AVAILABLE".equalsIgnoreCase(artwork.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Artwork is not available");
        }

        if (artwork.getArtistUid() != null) {
            User artist = userService.getUserByUid(artwork.getArtistUid());

            if (artist != null && artist.getEmail() != null
                    && email.equalsIgnoreCase(artist.getEmail())) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "You cannot buy your own artwork");
            }
        }

        long amount = toPaise(artwork.getPrice());
        String receipt = "artwork_" + artworkId + "_" + System.currentTimeMillis();
        Order order = razorpayService.createOrder(amount, receipt);

        return orderResponse(order, amount, "artworkId", artworkId).toString();
    }

    @PostMapping("/verify")
    public String verifyArtworkPayment(
            @RequestBody PaymentVerificationRequest request,
            Authentication authentication) throws Exception {

        FirestoreArtwork artwork =
                artworkService.getArtworkById(request.getArtworkId());

        if (artwork == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Artwork not found");
        }

        long expectedAmount = toPaise(artwork.getPrice());

        boolean valid = razorpayService.verifyPayment(
                request.getRazorpayOrderId(),
                request.getRazorpayPaymentId(),
                request.getRazorpaySignature(),
                request.getArtworkId(),
                expectedAmount);

        if (!valid) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Payment verification failed");
        }

        return purchaseService.buyArtwork(
                request.getArtworkId(),
                authentication,
                request.getRazorpayOrderId(),
                request.getRazorpayPaymentId());
    }

    // ---------------------------------------------------------
    // Auction payment
    // ---------------------------------------------------------

    @PostMapping("/auction/create-order")
    public String createAuctionOrder(
            @RequestParam String auctionId,
            Authentication authentication) throws Exception {

        String email = currentEmail(authentication);
        FirestoreAuction auction = auctionService.getAuction(auctionId);

        if (auction == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Auction not found");
        }

        if (!"ENDED".equalsIgnoreCase(auction.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Auction has not ended yet");
        }

        if (auction.getWinnerUid() == null
                || auction.getWinnerEmail() == null
                || !auction.getWinnerEmail().equalsIgnoreCase(email)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only the auction winner can make this payment");
        }

        long amount = toPaise(auction.getCurrentHighestBid());
        String receipt = "auction_" + auctionId + "_" + System.currentTimeMillis();
        Order order = razorpayService.createOrder(amount, receipt);

        JSONObject response =
                orderResponse(order, amount, "auctionId", auctionId);
        response.put("artworkId", (Object) auction.getArtworkId());
        response.put("winningBid", (Object) auction.getCurrentHighestBid());
        response.put("winner", (Object) auction.getWinnerName());

        return response.toString();
    }

    @PostMapping("/auction/verify")
    public String verifyAuctionPayment(
            @RequestBody PaymentVerificationRequest request,
            Authentication authentication) throws Exception {

        FirestoreAuction auction =
                auctionService.getAuction(request.getAuctionId());

        if (auction == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Auction not found");
        }

        if (!"ENDED".equalsIgnoreCase(auction.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Auction has not ended yet");
        }

        if (auction.getWinnerEmail() == null
                || !auction.getWinnerEmail().equalsIgnoreCase(
                currentEmail(authentication))) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only the auction winner can complete payment");
        }

        long expectedAmount = toPaise(auction.getCurrentHighestBid());

        boolean valid = razorpayService.verifyAuctionPayment(
                request.getRazorpayOrderId(),
                request.getRazorpayPaymentId(),
                request.getRazorpaySignature(),
                request.getAuctionId(),
                expectedAmount);

        if (!valid) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Auction payment verification failed");
        }

        return purchaseService.completeAuctionPurchase(
                request.getAuctionId(),
                authentication,
                request.getRazorpayOrderId(),
                request.getRazorpayPaymentId());
    }

    // ---------------------------------------------------------
    // Commission payment - Firestore
    // ---------------------------------------------------------

    @PostMapping("/commission/create-order")
    public String createCommissionOrder(
            @RequestParam String commissionId,
            Authentication authentication) throws Exception {

        String email = currentEmail(authentication);

        DocumentSnapshot commission = getDocument(
                COMMISSIONS, commissionId, "Commission not found");

        String clientEmail = commission.getString("clientEmail");

        if (clientEmail == null || !email.equalsIgnoreCase(clientEmail)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only the commission owner can make this payment");
        }

        String status = commission.getString("status");
        if (!CommissionStatus.ARTIST_SELECTED.name().equals(status)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Artist has not been selected");
        }

        String paymentStatus = commission.getString("paymentStatus");
        if (CommissionPaymentStatus.PAID.name().equals(paymentStatus)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Commission has already been paid");
        }

        QuerySnapshot offers = query(
                firestore.collection(OFFERS)
                        .whereEqualTo("commissionId", commissionId)
                        .whereEqualTo("status",
                                CommissionOfferStatus.SELECTED.name())
                        .limit(1));

        if (offers.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Selected offer not found");
        }

        QueryDocumentSnapshot offer = offers.getDocuments().get(0);
        BigDecimal fee = readMoney(offer, "proposedFee");
        long amount = toPaise(fee);

        Order order = razorpayService.createOrder(
                amount,
                "commission_" + commissionId + "_" + System.currentTimeMillis());

        // Save the order ID so verification can confirm this commission's order.
        Map<String, Object> update = new HashMap<>();
        update.put("razorpayOrderId", String.valueOf(order.get("id")));
        update.put("updatedAt", LocalDateTime.now().toString());

        try {
            firestore.collection(COMMISSIONS)
                    .document(commissionId)
                    .update(update)
                    .get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Saving commission payment order was interrupted");
        } catch (ExecutionException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Could not save commission payment order");
        }

        JSONObject response = orderResponse(
                order, amount, "commissionId", commissionId);
        response.put("offerId", (Object) offer.getId());
        response.put("proposedFee", (Object) fee.toPlainString());

        return response.toString();
    }

    @PostMapping("/commission/verify")
    public String verifyCommissionPayment(
            @RequestBody PaymentVerificationRequest request,
            Authentication authentication) throws Exception {

        if (request.getCommissionId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Commission ID is required");
        }

        String commissionId = String.valueOf(request.getCommissionId());
        String email = currentEmail(authentication);

        DocumentSnapshot commission = getDocument(
                COMMISSIONS, commissionId, "Commission not found");

        String clientEmail = commission.getString("clientEmail");
        if (clientEmail == null || !email.equalsIgnoreCase(clientEmail)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only the commission client can make payment");
        }

        if (!CommissionStatus.DELIVERED.name()
                .equals(commission.getString("status"))) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Commission is not ready for payment");
        }

        if (CommissionPaymentStatus.PAID.name()
                .equals(commission.getString("paymentStatus"))) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Commission has already been paid");
        }

        String storedOrderId = commission.getString("razorpayOrderId");
        if (storedOrderId == null
                || !storedOrderId.equals(request.getRazorpayOrderId())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Payment order does not match this commission");
        }

        QuerySnapshot offers = query(
                firestore.collection(OFFERS)
                        .whereEqualTo("commissionId", commissionId)
                        .whereEqualTo("status",
                                CommissionOfferStatus.SELECTED.name())
                        .limit(1));

        if (offers.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Selected offer not found");
        }

        QueryDocumentSnapshot offer = offers.getDocuments().get(0);
        BigDecimal fee = readMoney(offer, "proposedFee");
        long expectedAmount = toPaise(fee);

        boolean valid = razorpayService.verifyCommissionPayment(
                request.getRazorpayOrderId(),
                request.getRazorpayPaymentId(),
                request.getRazorpaySignature(),
                request.getCommissionId(),
                expectedAmount);

        if (!valid) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Commission payment verification failed");
        }

        // Persist payment identifiers and statuses in Firestore.
        Map<String, Object> update = new HashMap<>();
        update.put("paymentStatus", CommissionPaymentStatus.PAID.name());
        update.put("status", CommissionStatus.COMPLETED.name());
        update.put("razorpayOrderId", request.getRazorpayOrderId());
        update.put("razorpayPaymentId", request.getRazorpayPaymentId());
        update.put("updatedAt", LocalDateTime.now().toString());

        try {
            firestore.collection(COMMISSIONS)
                    .document(commissionId)
                    .update(update)
                    .get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Saving commission payment was interrupted");
        } catch (ExecutionException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Could not save commission payment");
        }

        // Resolve the selected artist's Firestore user profile.
        String artistUid = offer.getString("artistUid");
        User artist = userService.getUserByUid(artistUid);
        User client = userService.getUserByEmail(clientEmail);

        notificationService.createNotification(
                artist,
                client,
                "Commission Payment Received",
                "Payment of ₹" + fee
                        + " has been completed for your commission \""
                        + commission.getString("title") + "\".",
                NotificationType.COMMISSION);

        return "Commission payment completed successfully";
    }
}
