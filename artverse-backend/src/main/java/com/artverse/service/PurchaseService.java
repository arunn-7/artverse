package com.artverse.service;

import com.artverse.entity.Artwork;
import com.artverse.entity.ArtworkStatus;
import com.artverse.entity.Purchase;
import com.artverse.entity.User;
import com.artverse.exception.ArtworkNotFoundException;
import com.artverse.exception.UserNotFoundException;
import com.artverse.repository.ArtworkRepository;
import com.artverse.repository.PurchaseRepository;
import com.artverse.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import com.artverse.entity.NotificationType;
import com.artverse.dto.PurchaseHistoryResponse;
import com.artverse.dto.SalesHistoryResponse;
import com.artverse.dto.SellerDashboardResponse;
import java.math.BigDecimal;
import com.artverse.dto.BuyerDashboardResponse;
import java.math.BigDecimal;
import java.util.List;
import com.artverse.entity.Auction;
import com.artverse.entity.AuctionStatus;
import com.artverse.entity.Bid;
import com.artverse.repository.AuctionRepository;
import com.artverse.repository.BidRepository;


import java.time.LocalDateTime;

@Service
public class PurchaseService {

    @Autowired
    private PurchaseRepository purchaseRepository;

    @Autowired
    private ArtworkRepository artworkRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private AuctionRepository auctionRepository;

    @Autowired
    private BidRepository bidRepository;

    public String buyArtwork(Long artworkId, Authentication authentication) {

        User buyer = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Artwork artwork = artworkRepository.findById(artworkId)
                .orElseThrow(() -> new ArtworkNotFoundException("Artwork not found"));

        if (!artwork.isForSale()) {
            throw new RuntimeException("Artwork is not for sale");
        }

        if (artwork.getStatus() != ArtworkStatus.AVAILABLE) {
            throw new RuntimeException("Artwork is not available");
        }

        if (artwork.getUser().getId().equals(buyer.getId())) {
            throw new RuntimeException("You cannot buy your own artwork");
        }

        Purchase purchase = new Purchase();

        purchase.setBuyer(buyer);
        purchase.setSeller(artwork.getUser());
        purchase.setArtwork(artwork);
        purchase.setPrice(artwork.getPrice());
        purchase.setPurchaseDate(LocalDateTime.now());

        purchaseRepository.save(purchase);

        artwork.setForSale(false);
        artwork.setStatus(ArtworkStatus.SOLD);

        artworkRepository.save(artwork);

        notificationService.createNotification(
                artwork.getUser(),
                buyer,
                "Artwork Sold",
                buyer.getFullName() + " purchased your artwork \"" + artwork.getTitle() + "\".",
                NotificationType.PURCHASE
        );


        return "Artwork purchased successfully";
    }
    public List<PurchaseHistoryResponse> getMyPurchases(Authentication authentication) {

        User buyer = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        return purchaseRepository.findByBuyer(buyer)
                .stream()
                .map(purchase -> new PurchaseHistoryResponse(
                        purchase.getArtwork().getTitle(),
                        purchase.getSeller().getFullName(),
                        purchase.getPrice(),
                        purchase.getPurchaseDate()
                ))
                .toList();
    }
    public List<SalesHistoryResponse> getMySales(Authentication authentication) {

        User seller = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        return purchaseRepository.findBySeller(seller)
                .stream()
                .map(purchase -> new SalesHistoryResponse(
                        purchase.getArtwork().getTitle(),
                        purchase.getBuyer().getFullName(),
                        purchase.getPrice(),
                        purchase.getPurchaseDate()
                ))
                .toList();
    }
    public SellerDashboardResponse getSellerDashboard(Authentication authentication) {

        User seller = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        long totalSales = purchaseRepository.countBySeller(seller);

        BigDecimal totalRevenue = purchaseRepository.findBySeller(seller)
                .stream()
                .map(Purchase::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long activeListings = artworkRepository.countByUserAndForSaleTrue(seller);

        return new SellerDashboardResponse(
                totalSales,
                totalRevenue,
                activeListings
        );
    }
    public BuyerDashboardResponse getBuyerDashboard(Authentication authentication) {

        User buyer = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        long totalPurchases = purchaseRepository.countByBuyer(buyer);

        BigDecimal totalSpent = purchaseRepository.findByBuyer(buyer)
                .stream()
                .map(Purchase::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new BuyerDashboardResponse(
                totalPurchases,
                totalSpent
        );
    }
    public String completeAuctionPurchase(
            Long auctionId,
            Authentication authentication) {

        // 1. Get logged-in user
        User buyer = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new UserNotFoundException("User not found"));


        // 2. Find auction
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() ->
                        new RuntimeException("Auction not found"));


        // 3. Auction must be ended
        if (auction.getStatus() != AuctionStatus.ENDED) {
            throw new RuntimeException(
                    "Auction has not ended yet"
            );
        }


        // 4. Get winner
        User winner = auction.getWinner();

        if (winner == null) {
            throw new RuntimeException(
                    "This auction has no winner"
            );
        }


        // 5. Only winner can make payment
        if (!winner.getId().equals(buyer.getId())) {
            throw new RuntimeException(
                    "Only the auction winner can make the payment"
            );
        }


        // 6. Get artwork
        Artwork artwork = auction.getArtwork();


        // 7. Prevent duplicate purchase
        if (purchaseRepository.existsByArtwork(artwork)) {
            throw new RuntimeException(
                    "Artwork has already been purchased"
            );
        }


        // 8. Get winning bid
        Bid winningBid = bidRepository
                .findTopByAuctionOrderByAmountDesc(auction)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Winning bid not found"
                        ));


        // 9. Create purchase
        Purchase purchase = new Purchase();

        purchase.setBuyer(buyer);
        purchase.setSeller(auction.getArtist());
        purchase.setArtwork(artwork);
        purchase.setPrice(winningBid.getAmount());
        purchase.setPurchaseDate(LocalDateTime.now());

        purchaseRepository.save(purchase);


        // 10. Mark artwork as SOLD
        artwork.setForSale(false);
        artwork.setStatus(ArtworkStatus.SOLD);

        artworkRepository.save(artwork);


        // 11. Notify artist
        notificationService.createNotification(
                auction.getArtist(),
                buyer,
                "Auction Payment Received",
                buyer.getFullName()
                        + " purchased your artwork \""
                        + artwork.getTitle()
                        + "\" for ₹"
                        + winningBid.getAmount()
                        + ".",
                NotificationType.PURCHASE
        );


        return "Auction payment successful and purchase created";
    }

}