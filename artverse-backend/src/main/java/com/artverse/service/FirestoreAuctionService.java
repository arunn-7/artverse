package com.artverse.service;

import com.artverse.entity.NotificationType;
import com.artverse.entity.User;
import com.artverse.model.FirestoreArtwork;
import com.artverse.model.FirestoreAuction;
import com.artverse.model.FirestoreBid;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.SetOptions;
import com.google.cloud.firestore.Transaction;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service
public class FirestoreAuctionService {

    private static final String AUCTIONS = "auctions";
    private static final String BIDS = "bids";

    private final Firestore firestore;
    private final FirestoreArtworkService artworkService;
    private final FirestoreUserService userService;
    private final NotificationService notificationService;

    public FirestoreAuctionService(
            Firestore firestore,
            FirestoreArtworkService artworkService,
            FirestoreUserService userService,
            NotificationService notificationService) {

        this.firestore = firestore;
        this.artworkService = artworkService;
        this.userService = userService;
        this.notificationService = notificationService;
    }

    public String createAuction(
            String artworkId,
            BigDecimal startingPrice,
            LocalDateTime startTime,
            LocalDateTime endTime,
            Authentication authentication) {

        try {
            User artist = getAuthenticatedUser(authentication);

            if (startingPrice == null
                    || startingPrice.compareTo(BigDecimal.ZERO) <= 0) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Starting price must be greater than zero");
            }

            if (startTime == null || endTime == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Start time and end time are required");
            }

            LocalDateTime now = LocalDateTime.now();

            if (!endTime.isAfter(startTime)) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "End time must be after start time");
            }

            if (!endTime.isAfter(now)) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "End time must be in the future");
            }

            FirestoreArtwork artwork =
                    artworkService.getArtworkById(artworkId);

            if (artwork == null) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Artwork not found");
            }

            if (artist.getUserUid() == null
                    || !artist.getUserUid().equals(artwork.getArtistUid())) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "You can only auction your own artwork");
            }

            if (!artwork.isForSale()
                    || "SOLD".equalsIgnoreCase(artwork.getStatus())
                    || "AUCTION".equalsIgnoreCase(artwork.getStatus())) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Artwork is not available for auction");
            }

            List<QueryDocumentSnapshot> existingAuctions = firestore
                    .collection(AUCTIONS)
                    .whereEqualTo("artworkId", artworkId)
                    .get()
                    .get()
                    .getDocuments();

            for (DocumentSnapshot document : existingAuctions) {
                String status = document.getString("status");

                if ("UPCOMING".equalsIgnoreCase(status)
                        || "ACTIVE".equalsIgnoreCase(status)) {
                    throw new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "An active or upcoming auction already exists");
                }
            }

            DocumentReference auctionRef =
                    firestore.collection(AUCTIONS).document();

            FirestoreAuction auction = new FirestoreAuction();

            auction.setId(auctionRef.getId());
            auction.setArtworkId(artworkId);
            auction.setArtworkTitle(artwork.getTitle());
            auction.setArtworkImageUrl(artwork.getImageUrl());

            auction.setArtistUid(artist.getUserUid());
            auction.setArtistEmail(artist.getEmail());
            auction.setArtistName(artist.getFullName());

            auction.setStartingPrice(startingPrice);
            auction.setCurrentHighestBid(null);
            auction.setStartTime(startTime.toString());
            auction.setEndTime(endTime.toString());
            auction.setStatus(
                    now.isBefore(startTime) ? "UPCOMING" : "ACTIVE");

            auction.setTotalBids(0L);
            auction.setCreatedAt(now.toString());
            auction.setUpdatedAt(now.toString());

            auctionRef.set(auctionToMap(auction)).get();

            artwork.setForSale(false);
            artwork.setStatus("AUCTION");

            try {
                artworkService.updateArtwork(artworkId, artwork);
            } catch (Exception e) {
                auctionRef.delete().get();

                throw new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Unable to update artwork for auction",
                        e);
            }

            return "Auction created successfully. Auction ID: "
                    + auction.getId();

        } catch (ResponseStatusException e) {
            throw e;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Auction creation was interrupted",
                    e);
        } catch (ExecutionException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Unable to create auction",
                    e);
        }
    }

    public String placeBid(
            String auctionId,
            BigDecimal amount,
            Authentication authentication) {

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Bid amount must be greater than zero");
        }

        User bidder = getAuthenticatedUser(authentication);

        DocumentReference auctionRef =
                firestore.collection(AUCTIONS).document(auctionId);

        DocumentReference bidRef =
                firestore.collection(BIDS).document();

        final String[] previousBidderUid = {null};
        final BigDecimal[] acceptedAmount = {null};

        try {
            firestore.runTransaction(transaction -> {
                DocumentSnapshot snapshot = transaction.get(auctionRef).get();

                if (!snapshot.exists()) {
                    throw new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Auction not found");
                }

                FirestoreAuction auction = mapAuction(snapshot);

                LocalDateTime now = LocalDateTime.now();
                LocalDateTime start =
                        LocalDateTime.parse(auction.getStartTime());
                LocalDateTime end =
                        LocalDateTime.parse(auction.getEndTime());

                if (now.isBefore(start)) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Auction has not started yet");
                }

                if (!now.isBefore(end)
                        || "ENDED".equalsIgnoreCase(auction.getStatus())) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Auction has ended");
                }

                if (auction.getArtistUid() != null
                        && auction.getArtistUid()
                        .equals(bidder.getUserUid())) {
                    throw new ResponseStatusException(
                            HttpStatus.FORBIDDEN,
                            "You cannot bid on your own artwork");
                }

                BigDecimal currentHighest =
                        auction.getCurrentHighestBid();

                BigDecimal minimumBid = currentHighest != null
                        ? currentHighest
                        : auction.getStartingPrice();

                if (amount.compareTo(minimumBid) <= 0) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Bid must be greater than " + minimumBid);
                }

                // Read the previous highest bid before writing.
                List<QueryDocumentSnapshot> bidDocuments = transaction.get(
                                firestore.collection(BIDS)
                                        .whereEqualTo("auctionId", auctionId))
                        .get()
                        .getDocuments();

                BigDecimal previousHighestAmount = null;

                for (DocumentSnapshot bidDocument : bidDocuments) {
                    BigDecimal existingAmount =
                            readDecimal(bidDocument.get("amount"));

                    if (existingAmount != null
                            && (previousHighestAmount == null
                            || existingAmount.compareTo(
                            previousHighestAmount) > 0)) {

                        previousHighestAmount = existingAmount;
                        previousBidderUid[0] = bidDocument.getString("bidderUid");
                    }
                }

                FirestoreBid bid = new FirestoreBid();

                bid.setId(bidRef.getId());
                bid.setAuctionId(auctionId);
                bid.setBidderUid(bidder.getUserUid());
                bid.setBidderEmail(bidder.getEmail());
                bid.setBidderName(bidder.getFullName());
                bid.setAmount(amount);
                bid.setCreatedAt(now.toString());

                auction.setCurrentHighestBid(amount);
                auction.setTotalBids(
                        auction.getTotalBids() == null
                                ? 1L
                                : auction.getTotalBids() + 1L);
                auction.setStatus("ACTIVE");
                auction.setUpdatedAt(now.toString());

                transaction.set(bidRef, bidToMap(bid));
                transaction.set(
                        auctionRef,
                        auctionToMap(auction),
                        SetOptions.merge());

                acceptedAmount[0] = amount;

                return null;
            }).get();

        } catch (ResponseStatusException e) {
            throw e;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Bid operation was interrupted",
                    e);
        } catch (ExecutionException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Unable to place bid",
                    e);
        }

        // Notifications happen after the bid transaction commits.
        try {
            DocumentSnapshot auctionSnapshot = auctionRef.get().get();
            FirestoreAuction auction = mapAuction(auctionSnapshot);

            User artist = userService.getUserByUid(auction.getArtistUid());

            notificationService.createNotification(
                    artist,
                    bidder,
                    "New Bid",
                    bidder.getFullName() + " placed a bid of "
                            + acceptedAmount[0] + " on "
                            + auction.getArtworkTitle(),
                    NotificationType.NEW_BID);

        } catch (Exception ignored) {
            // A notification failure must not undo a committed bid.
        }

        if (previousBidderUid[0] != null
                && !previousBidderUid[0].equals(bidder.getUserUid())) {
            try {
                User previousBidder =
                        userService.getUserByUid(previousBidderUid[0]);

                notificationService.createNotification(
                        previousBidder,
                        bidder,
                        "Outbid",
                        "Your bid on the artwork has been exceeded.",
                        NotificationType.OUTBID);

            } catch (Exception ignored) {
                // A notification failure must not undo a committed bid.
            }
        }

        return "Bid placed successfully";
    }

    public FirestoreAuction getAuction(String auctionId) {
        try {
            DocumentSnapshot document = firestore
                    .collection(AUCTIONS)
                    .document(auctionId)
                    .get()
                    .get();

            if (!document.exists()) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Auction not found");
            }

            FirestoreAuction auction = mapAuction(document);
            updateAuctionStatus(auction);

            return auction;

        } catch (ResponseStatusException e) {
            throw e;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Auction lookup was interrupted",
                    e);
        } catch (ExecutionException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Unable to retrieve auction",
                    e);
        }
    }

    public List<FirestoreBid> getAuctionBids(String auctionId) {
        try {
            DocumentSnapshot auctionDocument = firestore
                    .collection(AUCTIONS)
                    .document(auctionId)
                    .get()
                    .get();

            if (!auctionDocument.exists()) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Auction not found");
            }

            List<FirestoreBid> bids = new ArrayList<>();

            List<QueryDocumentSnapshot> documents = firestore
                    .collection(BIDS)
                    .whereEqualTo("auctionId", auctionId)
                    .get()
                    .get()
                    .getDocuments();

            for (DocumentSnapshot document : documents) {
                FirestoreBid bid = mapBid(document);

                if (bid != null) {
                    bids.add(bid);
                }
            }

            bids.sort(
                    Comparator.comparing(
                            FirestoreBid::getAmount,
                            Comparator.nullsLast(Comparator.reverseOrder())));

            return bids;

        } catch (ResponseStatusException e) {
            throw e;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Bid lookup was interrupted",
                    e);
        } catch (ExecutionException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Unable to retrieve bids",
                    e);
        }
    }

    public String endAuction(String auctionId) {
        try {
            DocumentReference auctionRef =
                    firestore.collection(AUCTIONS).document(auctionId);

            DocumentSnapshot document = auctionRef.get().get();

            if (!document.exists()) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Auction not found");
            }

            FirestoreAuction auction = mapAuction(document);

            if ("ENDED".equalsIgnoreCase(auction.getStatus())) {
                return "Auction has already ended";
            }

            LocalDateTime now = LocalDateTime.now();

            if (now.isBefore(LocalDateTime.parse(auction.getEndTime()))) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Auction end time has not been reached");
            }

            List<QueryDocumentSnapshot> documents = firestore
                    .collection(BIDS)
                    .whereEqualTo("auctionId", auctionId)
                    .get()
                    .get()
                    .getDocuments();

            FirestoreBid highestBid = null;

            for (DocumentSnapshot bidDocument : documents) {
                FirestoreBid bid = mapBid(bidDocument);

                if (bid != null
                        && (highestBid == null
                        || bid.getAmount().compareTo(
                        highestBid.getAmount()) > 0)) {
                    highestBid = bid;
                }
            }

            auction.setStatus("ENDED");
            auction.setUpdatedAt(now.toString());

            if (highestBid != null) {
                auction.setWinnerUid(highestBid.getBidderUid());
                auction.setWinnerEmail(highestBid.getBidderEmail());
                auction.setWinnerName(highestBid.getBidderName());
                auction.setCurrentHighestBid(highestBid.getAmount());
            }

            auctionRef.set(auctionToMap(auction), SetOptions.merge()).get();

            try {
                User artist = userService.getUserByUid(auction.getArtistUid());

                if (highestBid != null) {
                    User winner =
                            userService.getUserByUid(highestBid.getBidderUid());

                    notificationService.createNotification(
                            winner,
                            artist,
                            "Auction Won",
                            "Congratulations! You won the auction for "
                                    + auction.getArtworkTitle(),
                            NotificationType.AUCTION_WON);

                    notificationService.createNotification(
                            artist,
                            winner,
                            "Auction Ended",
                            "Your auction ended. Winner: "
                                    + winner.getFullName(),
                            NotificationType.AUCTION_ENDED);
                } else {
                    notificationService.createNotification(
                            artist,
                            null,
                            "Auction Ended",
                            "Your auction ended without any bids.",
                            NotificationType.AUCTION_ENDED);
                }

            } catch (Exception ignored) {
                // Keep the ended auction even if notification delivery fails.
            }

            return highestBid == null
                    ? "Auction ended without any bids"
                    : "Auction ended. Winner: " + highestBid.getBidderName();

        } catch (ResponseStatusException e) {
            throw e;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Ending the auction was interrupted",
                    e);
        } catch (ExecutionException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Unable to end auction",
                    e);
        }
    }

    public List<FirestoreAuction> getActiveAuctions() {
        return getAuctionsByStatus("ACTIVE");
    }

    public List<FirestoreAuction> getUpcomingAuctions() {
        return getAuctionsByStatus("UPCOMING");
    }

    public List<FirestoreAuction> getAllAuctions() {
        return getAuctionsByStatus(null);
    }

    private List<FirestoreAuction> getAuctionsByStatus(String status) {
        try {
            List<FirestoreAuction> auctions = new ArrayList<>();

            List<QueryDocumentSnapshot> documents = firestore
                    .collection(AUCTIONS)
                    .get()
                    .get()
                    .getDocuments();

            for (DocumentSnapshot document : documents) {
                FirestoreAuction auction = mapAuction(document);

                if (auction == null) {
                    continue;
                }

                updateAuctionStatus(auction);

                if (status == null
                        || status.equalsIgnoreCase(auction.getStatus())) {
                    auctions.add(auction);
                }
            }

            return auctions;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Auction retrieval was interrupted",
                    e);
        } catch (ExecutionException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Unable to retrieve auctions",
                    e);
        }
    }

    @Scheduled(fixedRate = 10000)
    public void updateAuctionStatuses() {
        getAuctionsByStatus(null);
    }

    private void updateAuctionStatus(FirestoreAuction auction) {
        if (auction == null
                || "ENDED".equalsIgnoreCase(auction.getStatus())) {
            return;
        }

        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime start = LocalDateTime.parse(auction.getStartTime());
            LocalDateTime end = LocalDateTime.parse(auction.getEndTime());

            if (!now.isBefore(end)) {
                endAuction(auction.getId());
                return;
            }

            String newStatus = now.isBefore(start) ? "UPCOMING" : "ACTIVE";

            if (!newStatus.equalsIgnoreCase(auction.getStatus())) {
                auction.setStatus(newStatus);
                auction.setUpdatedAt(now.toString());

                firestore.collection(AUCTIONS)
                        .document(auction.getId())
                        .set(auctionToMap(auction), SetOptions.merge())
                        .get();
            }

        } catch (Exception ignored) {
            // Preserve the current status if an update fails.
        }
    }

    private User getAuthenticatedUser(Authentication authentication) {
        if (authentication == null
                || authentication.getName() == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authentication is required");
        }

        return userService.getUserByEmail(authentication.getName());
    }

    private FirestoreAuction mapAuction(DocumentSnapshot document) {
        FirestoreAuction auction = document.toObject(FirestoreAuction.class);

        if (auction == null) {
            return null;
        }

        auction.setId(document.getId());

        auction.setStartingPrice(
                readDecimal(document.get("startingPrice")));

        auction.setCurrentHighestBid(
                readDecimal(document.get("currentHighestBid")));

        return auction;
    }

    private FirestoreBid mapBid(DocumentSnapshot document) {
        FirestoreBid bid = document.toObject(FirestoreBid.class);

        if (bid == null) {
            return null;
        }

        bid.setId(document.getId());
        bid.setAmount(readDecimal(document.get("amount")));

        return bid;
    }

    private BigDecimal readDecimal(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }

        if (value instanceof Number) {
            return new BigDecimal(value.toString());
        }

        if (value instanceof String) {
            return new BigDecimal((String) value);
        }

        throw new IllegalArgumentException(
                "Invalid Firestore monetary value: " + value);
    }

    private Map<String, Object> auctionToMap(FirestoreAuction auction) {
        Map<String, Object> data = new HashMap<>();

        putIfNotNull(data, "id", auction.getId());
        putIfNotNull(data, "artworkId", auction.getArtworkId());
        putIfNotNull(data, "artworkTitle", auction.getArtworkTitle());
        putIfNotNull(data, "artworkImageUrl", auction.getArtworkImageUrl());

        putIfNotNull(data, "artistUid", auction.getArtistUid());
        putIfNotNull(data, "artistEmail", auction.getArtistEmail());
        putIfNotNull(data, "artistName", auction.getArtistName());

        putIfNotNull(
                data,
                "startingPrice",
                auction.getStartingPrice() == null
                        ? null
                        : auction.getStartingPrice().toPlainString());

        putIfNotNull(
                data,
                "currentHighestBid",
                auction.getCurrentHighestBid() == null
                        ? null
                        : auction.getCurrentHighestBid().toPlainString());

        putIfNotNull(data, "startTime", auction.getStartTime());
        putIfNotNull(data, "endTime", auction.getEndTime());
        putIfNotNull(data, "status", auction.getStatus());

        putIfNotNull(data, "winnerUid", auction.getWinnerUid());
        putIfNotNull(data, "winnerEmail", auction.getWinnerEmail());
        putIfNotNull(data, "winnerName", auction.getWinnerName());

        putIfNotNull(data, "totalBids", auction.getTotalBids());
        putIfNotNull(data, "createdAt", auction.getCreatedAt());
        putIfNotNull(data, "updatedAt", auction.getUpdatedAt());

        return data;
    }

    private Map<String, Object> bidToMap(FirestoreBid bid) {
        Map<String, Object> data = new HashMap<>();

        putIfNotNull(data, "id", bid.getId());
        putIfNotNull(data, "auctionId", bid.getAuctionId());
        putIfNotNull(data, "bidderUid", bid.getBidderUid());
        putIfNotNull(data, "bidderEmail", bid.getBidderEmail());
        putIfNotNull(data, "bidderName", bid.getBidderName());

        putIfNotNull(
                data,
                "amount",
                bid.getAmount() == null
                        ? null
                        : bid.getAmount().toPlainString());

        putIfNotNull(data, "createdAt", bid.getCreatedAt());

        return data;
    }

    private void putIfNotNull(
            Map<String, Object> data,
            String key,
            Object value) {

        if (value != null) {
            data.put(key, value);
        }
    }
}