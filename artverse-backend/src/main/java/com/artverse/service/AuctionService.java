package com.artverse.service;

import com.artverse.entity.*;
import com.artverse.exception.ArtworkNotFoundException;
import com.artverse.exception.UserNotFoundException;
import com.artverse.repository.ArtworkRepository;
import com.artverse.repository.AuctionRepository;
import com.artverse.repository.BidRepository;
import com.artverse.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;

import com.artverse.dto.AuctionResponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuctionService {

    @Autowired
    private AuctionRepository auctionRepository;

    @Autowired
    private BidRepository bidRepository;

    @Autowired
    private ArtworkRepository artworkRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationService notificationService;


    // =====================================================
    // CREATE AUCTION
    // =====================================================

    public String createAuction(
            Long artworkId,
            BigDecimal startingPrice,
            LocalDateTime startTime,
            LocalDateTime endTime,
            Authentication authentication) {

        User artist = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new UserNotFoundException("User not found"));


        Artwork artwork = artworkRepository.findById(artworkId)
                .orElseThrow(() ->
                        new ArtworkNotFoundException("Artwork not found"));


        // Only artwork owner can create auction
        if (!artwork.getUser().getId().equals(artist.getId())) {
            throw new RuntimeException(
                    "You can only auction your own artwork");
        }


        // Artwork must not already be sold
        if (artwork.getStatus() == ArtworkStatus.SOLD) {
            throw new RuntimeException(
                    "Sold artwork cannot be auctioned");
        }


        // Artwork cannot already have an auction
        if (auctionRepository.findByArtwork(artwork).isPresent()) {
            throw new RuntimeException(
                    "Auction already exists for this artwork");
        }


        // Validate starting price
        if (startingPrice == null ||
                startingPrice.compareTo(BigDecimal.ZERO) <= 0) {

            throw new RuntimeException(
                    "Starting price must be greater than zero");
        }


        // Validate dates
        if (startTime == null || endTime == null) {
            throw new RuntimeException(
                    "Start time and end time are required");
        }


        if (!endTime.isAfter(startTime)) {
            throw new RuntimeException(
                    "End time must be after start time");
        }


        LocalDateTime now = LocalDateTime.now();


        // Auction cannot already be completely expired
        if (!endTime.isAfter(now)) {
            throw new RuntimeException(
                    "Auction end time must be in the future");
        }


        // =================================================
        // CREATE AUCTION
        // =================================================

        Auction auction = new Auction();

        auction.setArtwork(artwork);
        auction.setArtist(artist);
        auction.setStartingPrice(startingPrice);
        auction.setCurrentHighestBid(null);
        auction.setStartTime(startTime);
        auction.setEndTime(endTime);


        // =================================================
        // SET INITIAL STATUS
        // =================================================

        if (now.isBefore(startTime)) {

            auction.setStatus(AuctionStatus.UPCOMING);

        } else {

            auction.setStatus(AuctionStatus.ACTIVE);
        }


        auctionRepository.save(auction);


        // =================================================
        // MARK ARTWORK AS AUCTION
        // =================================================

        artwork.setForSale(false);
        artwork.setStatus(ArtworkStatus.AUCTION);

        artworkRepository.save(artwork);


        return "Auction created successfully";
    }


    // =====================================================
    // PLACE BID
    // =====================================================

    public String placeBid(
            Long auctionId,
            BigDecimal amount,
            Authentication authentication) {

        User bidder = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new UserNotFoundException("User not found"));


        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() ->
                        new RuntimeException("Auction not found"));


        LocalDateTime now = LocalDateTime.now();


        // =================================================
        // AUCTION NOT STARTED
        // =================================================

        if (now.isBefore(auction.getStartTime())) {

            auction.setStatus(AuctionStatus.UPCOMING);
            auctionRepository.save(auction);

            throw new RuntimeException(
                    "Auction has not started yet");
        }


        // =================================================
        // AUCTION ENDED
        // =================================================

        if (!now.isBefore(auction.getEndTime())) {

            if (auction.getStatus() != AuctionStatus.ENDED) {
                endAuction(auctionId);
            }

            throw new RuntimeException(
                    "Auction has ended");
        }


        // =================================================
        // AUCTION SHOULD BE ACTIVE
        // =================================================

        if (auction.getStatus() != AuctionStatus.ACTIVE) {

            auction.setStatus(AuctionStatus.ACTIVE);
            auctionRepository.save(auction);
        }


        // =================================================
        // ARTIST CANNOT BID ON OWN ARTWORK
        // =================================================

        if (auction.getArtist().getId().equals(bidder.getId())) {

            throw new RuntimeException(
                    "You cannot bid on your own artwork");
        }


        // =================================================
        // DETERMINE MINIMUM BID
        // =================================================

        BigDecimal minimumBid;

        if (auction.getCurrentHighestBid() == null) {

            minimumBid = auction.getStartingPrice();

        } else {

            minimumBid = auction.getCurrentHighestBid();
        }


        // =================================================
        // VALIDATE BID
        // =================================================

        if (amount == null ||
                amount.compareTo(minimumBid) <= 0) {

            throw new RuntimeException(
                    "Bid must be higher than the current highest bid");
        }


        // =================================================
        // FIND PREVIOUS HIGHEST BIDDER
        // =================================================

        User previousHighestBidder = null;

        Bid previousHighestBid =
                bidRepository
                        .findTopByAuctionOrderByAmountDesc(auction)
                        .orElse(null);


        if (previousHighestBid != null) {

            previousHighestBidder =
                    previousHighestBid.getBidder();
        }


        // =================================================
        // CREATE NEW BID
        // =================================================

        Bid bid = new Bid();

        bid.setAuction(auction);
        bid.setBidder(bidder);
        bid.setAmount(amount);
        bid.setCreatedAt(now);

        bidRepository.save(bid);


        // =================================================
        // UPDATE HIGHEST BID
        // =================================================

        auction.setCurrentHighestBid(amount);

        auctionRepository.save(auction);


        // =================================================
        // NOTIFY ARTIST
        // =================================================

        notificationService.createNotification(
                auction.getArtist(),
                bidder,
                "New Bid",
                bidder.getFullName()
                        + " placed a bid of ₹"
                        + amount
                        + " on your artwork \""
                        + auction.getArtwork().getTitle()
                        + "\".",
                NotificationType.NEW_BID
        );


        // =================================================
        // NOTIFY PREVIOUS HIGHEST BIDDER
        // =================================================

        if (previousHighestBidder != null &&
                !previousHighestBidder.getId().equals(bidder.getId())) {

            notificationService.createNotification(
                    previousHighestBidder,
                    bidder,
                    "You Have Been Outbid",
                    bidder.getFullName()
                            + " placed a higher bid of ₹"
                            + amount
                            + ".",
                    NotificationType.OUTBID
            );
        }


        return "Bid placed successfully";
    }


    // =====================================================
    // GET AUCTION
    // =====================================================

    public Auction getAuction(Long auctionId) {

        updateAuctionStatuses();

        return auctionRepository.findById(auctionId)
                .orElseThrow(() ->
                        new RuntimeException("Auction not found"));
    }


    // =====================================================
    // GET AUCTION BIDS
    // =====================================================

    public List<Bid> getAuctionBids(Long auctionId) {

        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() ->
                        new RuntimeException("Auction not found"));


        return bidRepository
                .findByAuctionOrderByAmountDesc(auction);
    }


    // =====================================================
    // END AUCTION
    // =====================================================

    public String endAuction(Long auctionId) {

        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() ->
                        new RuntimeException("Auction not found"));


        if (auction.getStatus() == AuctionStatus.ENDED) {

            throw new RuntimeException(
                    "Auction is already ended");
        }


        // =================================================
        // FIND HIGHEST BID
        // =================================================

        Bid highestBid =
                bidRepository
                        .findTopByAuctionOrderByAmountDesc(auction)
                        .orElse(null);


        // =================================================
        // MARK AUCTION AS ENDED
        // =================================================

        auction.setStatus(AuctionStatus.ENDED);


        // =================================================
        // SELECT WINNER
        // =================================================

        if (highestBid != null) {

            User winner = highestBid.getBidder();

            auction.setWinner(winner);

            auction.setCurrentHighestBid(
                    highestBid.getAmount());


            // =============================================
            // WINNER NOTIFICATION
            // =============================================

            notificationService.createNotification(
                    winner,
                    auction.getArtist(),
                    "Auction Won",
                    "You won the auction for \""
                            + auction.getArtwork().getTitle()
                            + "\" with a bid of ₹"
                            + highestBid.getAmount()
                            + ".",
                    NotificationType.AUCTION_WON
            );


            // =============================================
            // ARTIST NOTIFICATION
            // =============================================

            notificationService.createNotification(
                    auction.getArtist(),
                    winner,
                    "Auction Ended",
                    "Your auction for \""
                            + auction.getArtwork().getTitle()
                            + "\" ended with a highest bid of ₹"
                            + highestBid.getAmount()
                            + ".",
                    NotificationType.AUCTION_ENDED
            );

        } else {

            // =============================================
            // NO BIDS
            // =============================================

            notificationService.createNotification(
                    auction.getArtist(),
                    null,
                    "Auction Ended",
                    "Your auction for \""
                            + auction.getArtwork().getTitle()
                            + "\" ended without any bids.",
                    NotificationType.AUCTION_ENDED
            );
        }


        auctionRepository.save(auction);


        return highestBid != null
                ? "Auction ended. Winner selected."
                : "Auction ended without any bids.";
    }


    // =====================================================
    // GET AUCTION RESPONSE
    // =====================================================

    public AuctionResponse getAuctionResponse(Long auctionId) {

        // Make sure status is up to date
        updateAuctionStatuses();


        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() ->
                        new RuntimeException("Auction not found"));


        Artwork artwork = auction.getArtwork();


        long totalBids =
                bidRepository.countByAuction(auction);


        return new AuctionResponse(
                auction.getId(),
                artwork.getId(),
                artwork.getTitle(),
                artwork.getDescription(),
                artwork.getImageUrl(),
                artwork.getCategory(),
                auction.getArtist().getId(),
                auction.getArtist().getFullName(),
                auction.getStartingPrice(),
                auction.getCurrentHighestBid(),
                auction.getStartTime(),
                auction.getEndTime(),
                auction.getStatus().name(),
                totalBids
        );
    }


    // =====================================================
    // GET ACTIVE AUCTIONS
    // =====================================================

    public List<AuctionResponse> getActiveAuctions() {

        // Make sure statuses are updated first
        updateAuctionStatuses();


        List<Auction> auctions =
                auctionRepository.findByStatus(
                        AuctionStatus.ACTIVE);


        return auctions.stream()
                .map(auction -> {

                    Artwork artwork = auction.getArtwork();

                    long totalBids =
                            bidRepository.countByAuction(auction);


                    return new AuctionResponse(
                            auction.getId(),
                            artwork.getId(),
                            artwork.getTitle(),
                            artwork.getDescription(),
                            artwork.getImageUrl(),
                            artwork.getCategory(),
                            auction.getArtist().getId(),
                            auction.getArtist().getFullName(),
                            auction.getStartingPrice(),
                            auction.getCurrentHighestBid(),
                            auction.getStartTime(),
                            auction.getEndTime(),
                            auction.getStatus().name(),
                            totalBids
                    );
                })
                .toList();
    }


    // =====================================================
    // GET UPCOMING AUCTIONS
    // =====================================================

    public List<AuctionResponse> getUpcomingAuctions() {

        // Make sure statuses are updated first
        updateAuctionStatuses();


        List<Auction> auctions =
                auctionRepository.findByStatus(
                        AuctionStatus.UPCOMING);


        return auctions.stream()
                .map(auction -> {

                    Artwork artwork = auction.getArtwork();

                    long totalBids =
                            bidRepository.countByAuction(auction);


                    return new AuctionResponse(
                            auction.getId(),
                            artwork.getId(),
                            artwork.getTitle(),
                            artwork.getDescription(),
                            artwork.getImageUrl(),
                            artwork.getCategory(),
                            auction.getArtist().getId(),
                            auction.getArtist().getFullName(),
                            auction.getStartingPrice(),
                            auction.getCurrentHighestBid(),
                            auction.getStartTime(),
                            auction.getEndTime(),
                            auction.getStatus().name(),
                            totalBids
                    );
                })
                .toList();
    }


    // =====================================================
    // AUTOMATIC AUCTION STATUS UPDATE
    // =====================================================

    @Scheduled(fixedRate = 10000)
    public void updateAuctionStatuses() {

        LocalDateTime now = LocalDateTime.now();


        // Get ALL auctions instead of relying
        // on their current database status
        List<Auction> auctions =
                auctionRepository.findAll();


        for (Auction auction : auctions) {

            // =============================================
            // IGNORE CANCELLED AUCTIONS
            // =============================================

            if (auction.getStatus() ==
                    AuctionStatus.CANCELLED) {

                continue;
            }


            // =============================================
            // UPCOMING
            // =============================================

            if (now.isBefore(auction.getStartTime())) {

                if (auction.getStatus() !=
                        AuctionStatus.UPCOMING) {

                    auction.setStatus(
                            AuctionStatus.UPCOMING);

                    auctionRepository.save(auction);
                }

                continue;
            }


            // =============================================
            // ACTIVE
            // =============================================

            if (!now.isBefore(auction.getStartTime())
                    && now.isBefore(auction.getEndTime())) {

                if (auction.getStatus() !=
                        AuctionStatus.ACTIVE) {

                    auction.setStatus(
                            AuctionStatus.ACTIVE);

                    auctionRepository.save(auction);
                }

                continue;
            }


            // =============================================
            // ENDED
            // =============================================

            if (!now.isBefore(auction.getEndTime())) {

                if (auction.getStatus() !=
                        AuctionStatus.ENDED) {

                    endAuction(auction.getId());
                }
            }
        }
    }
}