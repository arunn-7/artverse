package com.artverse.repository;

import com.artverse.entity.Purchase;
import com.artverse.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import com.artverse.entity.Artwork;

import java.util.List;

public interface PurchaseRepository extends JpaRepository<Purchase, Long> {

    List<Purchase> findByBuyer(User buyer);

    List<Purchase> findBySeller(User seller);

    long countBySeller(User seller);

    long countByBuyer(User buyer);

    boolean existsByArtwork(Artwork artwork);


}