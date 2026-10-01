package com.artverse.repository;

import com.artverse.entity.Certificate;
import com.artverse.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CertificateRepository
        extends JpaRepository<Certificate, Long> {

    List<Certificate> findByUser(User user);

    List<Certificate> findByUserId(Long userId);

    List<Certificate> findByVerificationStatus(
            String verificationStatus
    );
}