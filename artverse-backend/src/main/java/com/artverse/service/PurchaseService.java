package com.artverse.service;

import com.artverse.dto.BuyerDashboardResponse;
import com.artverse.dto.PurchaseHistoryResponse;
import com.artverse.dto.SalesHistoryResponse;
import com.artverse.dto.SellerDashboardResponse;
import com.artverse.entity.NotificationType;
import com.artverse.entity.User;
import com.artverse.model.FirestoreArtwork;
import com.artverse.model.FirestoreAuction;
import com.artverse.model.FirestoreBid;
import com.artverse.model.FirestorePurchase;
import com.google.cloud.firestore.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutionException;

@Service
public class PurchaseService {

    private static final String PURCHASES = "purchases";
    private static final String ARTWORKS = "artworks";
    private static final String AUCTIONS = "auctions";
    private static final String BIDS = "bids";

    private final Firestore firestore;
    private final FirestoreUserService userService;
    private final NotificationService notificationService;

    public PurchaseService(
            Firestore firestore,
            FirestoreUserService userService,
            NotificationService notificationService) {
        this.firestore = firestore;
        this.userService = userService;
        this.notificationService = notificationService;
    }

    public String buyArtwork(
            String artworkId,
            Authentication authentication,
            String orderId,
            String paymentId) {

        User buyer = getAuthenticatedUser(authentication);

        DocumentReference artworkRef =
                firestore.collection(ARTWORKS).document(artworkId);

        DocumentReference purchaseRef =
                firestore.collection(PURCHASES).document();

        final Map<String, Object>[] purchaseResult = new Map[]{null};

        try {
            firestore.runTransaction(transaction -> {
                DocumentSnapshot artworkDoc = transaction.get(artworkRef).get();

                if (!artworkDoc.exists()) {
                    throw new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Artwork not found");
                }

                FirestoreArtwork artwork = artworkDoc.toObject(FirestoreArtwork.class);

                if (artwork == null) {
                    throw new ResponseStatusException(
                            HttpStatus.INTERNAL_SERVER_ERROR,
                            "Unable to read artwork");
                }

                artwork.setId(artworkDoc.getId());

                if (!artwork.isForSale()
                        || !"AVAILABLE".equalsIgnoreCase(artwork.getStatus())) {
                    throw new ResponseStatusException(
                            HttpStatus.CONFLICT, "Artwork is not available");
                }

                if (buyer.getUserUid().equals(artwork.getArtistUid())) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "You cannot purchase your own artwork");
                }

                BigDecimal price = artwork.getPrice();

                if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Artwork has an invalid price");
                }

                Map<String, Object> purchase = createPurchaseMap(
                        buyer,
                        artwork.getArtistUid(),
                        artwork.getTitle(),
                        artwork.getImageUrl(),
                        artworkId,
                        null,
                        price,
                        orderId,
                        paymentId,
                        "ARTWORK"
                );

                transaction.set(purchaseRef, purchase);

                Map<String, Object> artworkUpdate = new HashMap<>();
                artworkUpdate.put("forSale", false);
                artworkUpdate.put("status", "SOLD");
                transaction.update(artworkRef, artworkUpdate);

                purchaseResult[0] = purchase;
                return null;
            }).get();

            notifySeller(
                    (String) purchaseResult[0].get("sellerUid"),
                    buyer,
                    (String) purchaseResult[0].get("artworkTitle"),
                    "Artwork Sold"
            );

            return "Artwork purchased successfully";

        } catch (ResponseStatusException e) {
            throw e;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Purchase was interrupted", e);
        } catch (ExecutionException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Unable to complete purchase", e);
        }
    }

    public String completeAuctionPurchase(
            String auctionId,
            Authentication authentication,
            String orderId,
            String paymentId) {

        User buyer = getAuthenticatedUser(authentication);

        DocumentReference auctionRef =
                firestore.collection(AUCTIONS).document(auctionId);

        DocumentReference purchaseRef =
                firestore.collection(PURCHASES).document();

        final Map<String, Object>[] purchaseResult = new Map[]{null};

        try {
            firestore.runTransaction(transaction -> {
                DocumentSnapshot auctionDoc = transaction.get(auctionRef).get();

                if (!auctionDoc.exists()) {
                    throw new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Auction not found");
                }

                FirestoreAuction auction =
                        auctionDoc.toObject(FirestoreAuction.class);

                if (auction == null) {
                    throw new ResponseStatusException(
                            HttpStatus.INTERNAL_SERVER_ERROR,
                            "Unable to read auction");
                }

                auction.setId(auctionDoc.getId());

                if (!"ENDED".equalsIgnoreCase(auction.getStatus())) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST, "Auction has not ended");
                }

                if (auction.getWinnerUid() == null
                        || !auction.getWinnerUid().equals(buyer.getUserUid())) {
                    throw new ResponseStatusException(
                            HttpStatus.FORBIDDEN,
                            "Only the auction winner can complete the purchase");
                }

                if (auctionDoc.getBoolean("purchaseCompleted") != null
                        && Boolean.TRUE.equals(
                        auctionDoc.getBoolean("purchaseCompleted"))) {
                    throw new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "Auction purchase has already been completed");
                }

                String artworkId = auction.getArtworkId();

                DocumentReference artworkRef =
                        firestore.collection(ARTWORKS).document(artworkId);

                DocumentSnapshot artworkDoc = transaction.get(artworkRef).get();

                if (!artworkDoc.exists()) {
                    throw new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Auction artwork not found");
                }

                FirestoreArtwork artwork =
                        artworkDoc.toObject(FirestoreArtwork.class);

                if (artwork == null) {
                    throw new ResponseStatusException(
                            HttpStatus.INTERNAL_SERVER_ERROR,
                            "Unable to read artwork");
                }

                BigDecimal winningAmount = auction.getCurrentHighestBid();

                if (winningAmount == null
                        || winningAmount.compareTo(BigDecimal.ZERO) <= 0) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Winning bid amount is invalid");
                }

                Map<String, Object> purchase = createPurchaseMap(
                        buyer,
                        auction.getArtistUid(),
                        artwork.getTitle(),
                        artwork.getImageUrl(),
                        artworkId,
                        auctionId,
                        winningAmount,
                        orderId,
                        paymentId,
                        "AUCTION"
                );

                transaction.set(purchaseRef, purchase);

                Map<String, Object> artworkUpdate = new HashMap<>();
                artworkUpdate.put("forSale", false);
                artworkUpdate.put("status", "SOLD");
                transaction.update(artworkRef, artworkUpdate);

                Map<String, Object> auctionUpdate = new HashMap<>();
                auctionUpdate.put("purchaseCompleted", true);
                auctionUpdate.put("paymentId", paymentId);
                auctionUpdate.put("orderId", orderId);
                auctionUpdate.put("updatedAt", LocalDateTime.now().toString());
                transaction.set(auctionRef, auctionUpdate, SetOptions.merge());

                purchaseResult[0] = purchase;
                return null;
            }).get();

            notifySeller(
                    (String) purchaseResult[0].get("sellerUid"),
                    buyer,
                    (String) purchaseResult[0].get("artworkTitle"),
                    "Auction Payment Received"
            );

            return "Auction purchase completed successfully";

        } catch (ResponseStatusException e) {
            throw e;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Auction purchase was interrupted", e);
        } catch (ExecutionException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Unable to complete auction purchase", e);
        }
    }

    public List<PurchaseHistoryResponse> getMyPurchases(
            Authentication authentication) {
        User buyer = getAuthenticatedUser(authentication);
        List<PurchaseHistoryResponse> result = new ArrayList<>();

        for (FirestorePurchase purchase : getPurchases()) {
            if (buyer.getUserUid().equals(purchase.getBuyerUid())) {
                result.add(new PurchaseHistoryResponse(
                        purchase.getArtworkTitle(),
                        purchase.getSellerName(),
                        decimal(purchase.getPrice()),
                        parseDate(purchase.getPurchaseDate())
                ));
            }
        }

        return result;
    }

    public List<SalesHistoryResponse> getMySales(
            Authentication authentication) {
        User seller = getAuthenticatedUser(authentication);
        List<SalesHistoryResponse> result = new ArrayList<>();

        for (FirestorePurchase purchase : getPurchases()) {
            if (seller.getUserUid().equals(purchase.getSellerUid())) {
                result.add(new SalesHistoryResponse(
                        purchase.getArtworkTitle(),
                        purchase.getBuyerName(),
                        decimal(purchase.getPrice()),
                        parseDate(purchase.getPurchaseDate())
                ));
            }
        }

        return result;
    }

    public SellerDashboardResponse getSellerDashboard(
            Authentication authentication) {
        User seller = getAuthenticatedUser(authentication);

        long sales = 0;
        BigDecimal revenue = BigDecimal.ZERO;

        for (FirestorePurchase purchase : getPurchases()) {
            if (seller.getUserUid().equals(purchase.getSellerUid())) {
                sales++;
                revenue = revenue.add(decimal(purchase.getPrice()));
            }
        }

        long activeListings = 0;

        try {
            List<QueryDocumentSnapshot> artworks = firestore
                    .collection(ARTWORKS)
                    .whereEqualTo("artistUid", seller.getUserUid())
                    .whereEqualTo("forSale", true)
                    .get().get().getDocuments();

            for (DocumentSnapshot artwork : artworks) {
                if ("AVAILABLE".equalsIgnoreCase(artwork.getString("status"))) {
                    activeListings++;
                }
            }
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Unable to load seller dashboard", e);
        }

        return new SellerDashboardResponse(sales, revenue, activeListings);
    }

    public BuyerDashboardResponse getBuyerDashboard(
            Authentication authentication) {
        User buyer = getAuthenticatedUser(authentication);

        long count = 0;
        BigDecimal spent = BigDecimal.ZERO;

        for (FirestorePurchase purchase : getPurchases()) {
            if (buyer.getUserUid().equals(purchase.getBuyerUid())) {
                count++;
                spent = spent.add(decimal(purchase.getPrice()));
            }
        }

        return new BuyerDashboardResponse(count, spent);
    }

    private List<FirestorePurchase> getPurchases() {
        try {
            List<FirestorePurchase> result = new ArrayList<>();

            for (QueryDocumentSnapshot document : firestore
                    .collection(PURCHASES).get().get().getDocuments()) {
                FirestorePurchase purchase =
                        document.toObject(FirestorePurchase.class);
                if (purchase != null) {
                    purchase.setId(document.getId());
                    result.add(purchase);
                }
            }

            return result;
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Unable to retrieve purchase records", e);
        }
    }

    private Map<String, Object> createPurchaseMap(
            User buyer,
            String sellerUid,
            String artworkTitle,
            String imageUrl,
            String artworkId,
            String auctionId,
            BigDecimal price,
            String orderId,
            String paymentId,
            String purchaseType) {

        User seller = userService.getUserByUid(sellerUid);

        if (seller == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Seller account not found");
        }

        Map<String, Object> map = new HashMap<>();
        map.put("buyerUid", buyer.getUserUid());
        map.put("buyerEmail", buyer.getEmail());
        map.put("buyerName", buyer.getFullName());
        map.put("sellerUid", sellerUid);
        map.put("sellerEmail", seller.getEmail());
        map.put("sellerName", seller.getFullName());
        map.put("artworkId", artworkId);
        map.put("artworkTitle", artworkTitle);
        map.put("artworkImageUrl", imageUrl == null ? "" : imageUrl);
        map.put("price", price.toPlainString());
        map.put("currency", "INR");
        map.put("purchaseDate", LocalDateTime.now().toString());
        map.put("orderId", orderId == null ? "" : orderId);
        map.put("paymentId", paymentId == null ? "" : paymentId);
        map.put("purchaseType", purchaseType);

        if (auctionId != null) {
            map.put("auctionId", auctionId);
        }

        return map;
    }

    private User getAuthenticatedUser(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Authentication is required");
        }

        User user = userService.getUserByEmail(authentication.getName());

        if (user == null || user.getUserUid() == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "User account not found");
        }

        return user;
    }

    private void notifySeller(
            String sellerUid, User buyer, String title, String notificationTitle) {
        try {
            User seller = userService.getUserByUid(sellerUid);
            if (seller != null) {
                notificationService.createNotification(
                        seller,
                        buyer,
                        notificationTitle,
                        "Your artwork \"" + title + "\" has been purchased.",
                        NotificationType.COMMISSION
                );
            }
        } catch (Exception ignored) {
            // The purchase is committed even if notification delivery fails.
        }
    }

    private BigDecimal decimal(String value) {
        return value == null || value.isBlank()
                ? BigDecimal.ZERO : new BigDecimal(value);
    }

    private LocalDateTime parseDate(String value) {
        return value == null || value.isBlank()
                ? LocalDateTime.now() : LocalDateTime.parse(value);
    }
}