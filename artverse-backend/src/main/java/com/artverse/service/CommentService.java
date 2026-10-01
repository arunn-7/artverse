package com.artverse.service;

import com.artverse.entity.Artwork;
import com.artverse.entity.Comment;
import com.artverse.entity.User;
import com.artverse.repository.ArtworkRepository;
import com.artverse.repository.CommentRepository;
import com.artverse.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.time.LocalDateTime;
import com.artverse.dto.CommentResponse;
import java.util.stream.Collectors;
import org.springframework.transaction.annotation.Transactional;
import com.artverse.exception.ArtworkNotFoundException;
import com.artverse.exception.UserNotFoundException;
import com.artverse.entity.NotificationType;

@Service
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private ArtworkRepository artworkRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationService notificationService;

    public String addComment(Long artworkId,
                             String email,
                             String text) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Artwork artwork = artworkRepository.findById(artworkId)
                .orElseThrow(() -> new ArtworkNotFoundException("Artwork not found"));

        Comment comment = new Comment(
                text,
                user,
                artwork,
                LocalDateTime.now()
        );

        commentRepository.save(comment);
        if (!artwork.getUser().getId().equals(user.getId())) {

            notificationService.createNotification(
                    artwork.getUser(),
                    user,
                    "New Comment",
                    user.getFullName() + " commented on your artwork \"" +
                            artwork.getTitle() + "\".",
                    NotificationType.COMMENT
            );
        }

        return "Comment added successfully";
    }
    public List<CommentResponse> getComments(Long artworkId) {

        Artwork artwork = artworkRepository.findById(artworkId)
                .orElseThrow(() -> new RuntimeException("Artwork not found"));

        return commentRepository.findByArtwork(artwork)
                .stream()
                .map(comment -> new CommentResponse(
                        comment.getId(),
                        comment.getText(),
                        comment.getUser().getFullName(),
                        comment.getCreatedAt()
                ))
                .collect(Collectors.toList());
    }

    @Transactional
    public String deleteComment(Long commentId, String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        commentRepository.deleteByIdAndUser(commentId, user);

        return "Comment deleted successfully";
    }

}