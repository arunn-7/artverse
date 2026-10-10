package com.artverse.service;

import com.artverse.entity.User;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.util.concurrent.ExecutionException;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import java.util.List;


@Service
public class ArtistProgressionService {

    private final Firestore firestore;
    private final FirestoreUserService userService;
    private final FollowService followService;

    public ArtistProgressionService(
            Firestore firestore,
            FirestoreUserService userService,
            FollowService followService) {
        this.firestore = firestore;
        this.userService = userService;
        this.followService = followService;
    }

    // Check and upgrade one artist
    public void checkAndUpgradeArtist(User artist)
            throws ExecutionException, InterruptedException {

        if (artist != null) {
            try {
                checkAndUpgradeArtist(artist);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            } catch (ExecutionException e) {
                throw new RuntimeException(
                        "Failed to save artist progression", e);
            }
        }

        long followers = followService.getFollowersCount(artist);

        String currentLevel = artist.getArtistLevel();

        if ("BEGINNER".equalsIgnoreCase(currentLevel)
                && followers >= 1000) {

            artist.setArtistLevel("INTERMEDIATE");
            userService.save(artist);

        } else if ("INTERMEDIATE".equalsIgnoreCase(currentLevel)
                && followers >= 5000) {

            artist.setArtistLevel("PROFESSIONAL");
            userService.save(artist);
        }
    }

    // Automatically check all Firebase artists every hour
    @Scheduled(fixedRate = 3600000)
    public void checkAllArtists() {

        try {
            List<QueryDocumentSnapshot> documents = firestore
                    .collection("users")
                    .whereEqualTo("accountType", "ARTIST")
                    .get()
                    .get()
                    .getDocuments();

            for (QueryDocumentSnapshot document : documents) {

                String email = document.getString("email");
                String uid = document.getString("userUid");

                // Skip legacy profiles without Firebase UID
                if (email == null || uid == null || uid.isBlank()) {
                    continue;
                }

                User artist = userService.getUserByEmail(email);

                if (artist != null) {
                    checkAndUpgradeArtist(artist);
                }
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(
                    "Artist progression check was interrupted", e);

        } catch (ExecutionException e) {
            throw new RuntimeException(
                    "Failed to retrieve artists from Firestore", e);
        }
    }
}