package com.artverse.repository;

import com.artverse.entity.Commission;
import com.artverse.entity.CommissionStatus;
import com.artverse.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommissionRepository
        extends JpaRepository<Commission, Long> {

    // User's own commission requests
    List<Commission> findByClient(User client);

    // Open commissions visible to artists
    List<Commission> findByStatus(CommissionStatus status);
}