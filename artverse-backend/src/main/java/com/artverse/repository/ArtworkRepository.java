package com.artverse.repository;
import java.util.List;
import com.artverse.entity.Artwork;
import org.springframework.data.jpa.repository.JpaRepository;
import com.artverse.entity.User;
import com.artverse.entity.ArtworkStatus;
import java.util.List;


public interface ArtworkRepository extends JpaRepository<Artwork, Long> {
    List<Artwork> findByUser(User user);
    long countByUser(User user);
    List<Artwork> findByForSaleTrueAndStatus(ArtworkStatus status);
    long countByUserAndForSaleTrue(User user);
}