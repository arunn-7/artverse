package com.artverse.repository;

import com.artverse.entity.Auction;
import com.artverse.entity.AuctionStatus;
import com.artverse.entity.Artwork;
import com.artverse.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AuctionRepository extends JpaRepository<Auction, Long> {

    Optional<Auction> findByArtwork(Artwork artwork);

    List<Auction> findByStatus(AuctionStatus status);

    List<Auction> findByArtist(User artist);
}