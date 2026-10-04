package com.artverse.repository;

import com.artverse.entity.Commission;
import com.artverse.entity.CommissionOffer;
import com.artverse.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommissionOfferRepository
        extends JpaRepository<CommissionOffer, Long> {

    // All offers for a commission
    List<CommissionOffer> findByCommission(Commission commission);

    // All offers submitted by an artist
    List<CommissionOffer> findByArtist(User artist);

    // Check whether artist already submitted an offer
    boolean existsByCommissionAndArtist(
            Commission commission,
            User artist
    );
}