package com.artverse.repository;

import com.artverse.entity.Artwork;
import com.artverse.entity.Comment;
import com.artverse.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByArtwork(Artwork artwork);

    void deleteByIdAndUser(Long id, User user);

    long countByArtwork(Artwork artwork);
}