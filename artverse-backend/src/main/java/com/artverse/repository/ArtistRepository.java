package com.artverse.repository;

import com.artverse.entity.Artist;
import com.artverse.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ArtistRepository extends JpaRepository<Artist, Long> {

    Optional<Artist> findByUser(User user);

    Optional<Artist> findByUserId(Long userId);

    boolean existsByUser(User user);
}