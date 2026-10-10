package com.artverse.service;

import com.artverse.entity.User;
import com.artverse.entity.NotificationType;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import com.artverse.service.FirestoreArtworkService;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service
public class FirestoreLikeService {

    private final Firestore firestore;
    private final FirestoreUserService userService;
    private final FirestoreArtworkService artworkService;
    private final NotificationService notificationService;

    public FirestoreLikeService(
            Firestore firestore,
            FirestoreUserService userService,
            FirestoreArtworkService artworkService,
            NotificationService notificationService) {
        this.firestore = firestore;
        this.userService = userService;
        this.artworkService = artworkService;
        this.notificationService = notificationService;
    }

    public String likeArtwork(String artworkId, String email) {
        try {
            User user = userService.getUserByEmail(email);

            var artwork = artworkService.getArtworkById(artworkId);
            if (artwork == null) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Artwork not found");
            }

            String uid = user.getUserUid();
            if (uid == null || uid.isBlank()) {
                throw new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "User UID not found");
            }

            String likeId = artworkId + "_" + uid;
            var ref = firestore.collection("likes").document(likeId);

            DocumentSnapshot existing = ref.get().get();
            if (existing.exists()) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "You already liked this artwork");
            }

            Map<String, Object> data = new HashMap<>();
            data.put("artworkId", artworkId);
            data.put("userUid", uid);
            data.put("userEmail", user.getEmail());
            data.put("userName", user.getFullName());
            data.put("createdAt", java.time.Instant.now().toString());

            ref.create(data).get();

            // Notify the artwork owner, except when liking own artwork.
            String artistUid = artwork.getArtistUid();

            if (artistUid != null && !artistUid.equals(uid)) {
                User receiver = userService.getUserByUid(artistUid);

                notificationService.createNotification(
                        receiver,
                        user,
                        "New Like",
                        user.getFullName() + " liked your artwork \""
                                + artwork.getTitle() + "\".",
                        NotificationType.LIKE);
            }

            return "Artwork liked successfully";

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Like operation interrupted");
        } catch (ExecutionException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to save like");
        }
    }

    public String unlikeArtwork(String artworkId, String email) {
        try {
            User user = userService.getUserByEmail(email);
            String uid = user.getUserUid();

            if (uid == null || uid.isBlank()) {
                throw new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "User UID not found");
            }

            String likeId = artworkId + "_" + uid;
            var ref = firestore.collection("likes").document(likeId);

            DocumentSnapshot existing = ref.get().get();
            if (!existing.exists()) {
                return "You haven't liked this artwork";
            }

            ref.delete().get();
            return "Artwork unliked successfully";

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Unlike operation interrupted");
        } catch (ExecutionException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to remove like");
        }
    }
}