package com.artverse.repository;

import com.artverse.entity.Auction;
import com.artverse.entity.Bid;
import com.artverse.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BidRepository extends JpaRepository<Bid, Long> {

    List<Bid> findByAuctionOrderByAmountDesc(Auction auction);

    Optional<Bid> findTopByAuctionOrderByAmountDesc(Auction auction);

    List<Bid> findByBidder(User bidder);

    long countByAuction(Auction auction);
}