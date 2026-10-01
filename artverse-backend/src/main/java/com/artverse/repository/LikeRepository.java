package com.artverse.repository;

import com.artverse.entity.Artwork;
import com.artverse.entity.Like;
import com.artverse.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LikeRepository extends JpaRepository<Like, Long> {

    boolean existsByUserAndArtwork(User user, Artwork artwork);

    void deleteByUserAndArtwork(User user, Artwork artwork);

    long countByArtwork(Artwork artwork);
}