package com.artverse.service;
import com.artverse.entity.NotificationType;

import com.artverse.entity.Artwork;
import com.artverse.entity.Like;
import com.artverse.entity.User;
import com.artverse.repository.ArtworkRepository;
import com.artverse.repository.LikeRepository;
import com.artverse.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.artverse.exception.AlreadyLikedException;
import com.artverse.exception.ArtworkNotFoundException;
import com.artverse.exception.UserNotFoundException;

@Service
@Transactional
public class LikeService {

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private ArtworkRepository artworkRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationService notificationService;

    public String likeArtwork(Long artworkId, String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Artwork artwork = artworkRepository.findById(artworkId)
                .orElseThrow(() -> new ArtworkNotFoundException("Artwork not found"));

        if (likeRepository.existsByUserAndArtwork(user, artwork)) {
            throw new AlreadyLikedException("You already liked this artwork");
        }

        Like like = new Like(user, artwork);

        likeRepository.save(like);
        // Don't notify if the user likes their own artwork
        if (!artwork.getUser().getId().equals(user.getId())) {

            notificationService.createNotification(
                    artwork.getUser(),
                    user,
                    "New Like",
                    user.getFullName() + " liked your artwork \"" + artwork.getTitle() + "\".",
                    NotificationType.LIKE
            );
        }

        return "Artwork liked successfully";
    }

    public String unlikeArtwork(Long artworkId, String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Artwork artwork = artworkRepository.findById(artworkId)
                .orElseThrow(() -> new RuntimeException("Artwork not found"));

        if (!likeRepository.existsByUserAndArtwork(user, artwork)) {
            return "You haven't liked this artwork";
        }

        likeRepository.deleteByUserAndArtwork(user, artwork);

        return "Artwork unliked successfully";
    }

}