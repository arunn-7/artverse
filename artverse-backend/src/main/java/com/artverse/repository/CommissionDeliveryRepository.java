package com.artverse.repository;

import com.artverse.entity.Commission;
import com.artverse.entity.CommissionDelivery;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CommissionDeliveryRepository
        extends JpaRepository<CommissionDelivery, Long> {

    Optional<CommissionDelivery> findByCommission(Commission commission);
}