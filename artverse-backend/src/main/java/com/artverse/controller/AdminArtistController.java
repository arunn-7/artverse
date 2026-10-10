package com.artverse.controller;

import com.artverse.dto.AdminArtistDetailsResponse;
import com.artverse.entity.User;
import com.artverse.service.ArtworkService;
import com.artverse.service.FirestoreUserService;
import com.artverse.service.FollowService;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/admin/artists")
public class AdminArtistController {

    private final Firestore firestore;
    private final FirestoreUserService userService;
    private final ArtworkService artworkService;
    private final FollowService followService;

    public AdminArtistController(
            Firestore firestore,
            FirestoreUserService userService,
            ArtworkService artworkService,
            FollowService followService) {
        this.firestore = firestore;
        this.userService = userService;
        this.artworkService = artworkService;
        this.followService = followService;
    }

    @GetMapping
    public List<AdminArtistDetailsResponse> getAllArtists() {
        try {
            List<? extends DocumentSnapshot> documents = firestore
                    .collection("users")
                    .whereEqualTo("accountType", "ARTIST")
                    .get()
                    .get()
                    .getDocuments();

            List<AdminArtistDetailsResponse> responses =
                    new ArrayList<>();

            for (DocumentSnapshot document : documents) {
                String email = document.getString("email");
                String uid = document.getString("userUid");

                // Skip legacy profiles without Firebase UIDs.
                if (email == null || uid == null || uid.isBlank()) {
                    continue;
                }

                User artist = userService.getUserByEmail(email);
                responses.add(convertToResponse(artist));
            }

            return responses;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Artist retrieval interrupted");
        } catch (ExecutionException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to retrieve artists");
        }
    }

    @GetMapping("/{id}")
    public AdminArtistDetailsResponse getArtistById(
            @PathVariable String id) {
        try {
            List<? extends DocumentSnapshot> documents = firestore
                    .collection("users")
                    .whereEqualTo("userUid", id)
                    .whereEqualTo("accountType", "ARTIST")
                    .limit(1)
                    .get()
                    .get()
                    .getDocuments();

            if (documents.isEmpty()) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Artist not found");
            }

            String email = documents.get(0).getString("email");

            if (email == null) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Artist email not found");
            }

            User artist = userService.getUserByEmail(email);

            if (artist.getUserUid() == null
                    || artist.getUserUid().isBlank()) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Artist Firebase UID is missing");
            }

            return convertToResponse(artist);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Artist retrieval interrupted");
        } catch (ExecutionException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to retrieve artist");
        }
    }

    private AdminArtistDetailsResponse convertToResponse(User artist) {
        long artworkCount =
                artworkService.getMyArtworks(artist).size();

        long followers =
                followService.getFollowersCount(artist);

        long following =
                followService.getFollowingCount(artist);

        return new AdminArtistDetailsResponse(
                artist.getUserUid(),
                artist.getFullName(),
                artist.getEmail(),
                artist.getAccountType(),
                artist.getArtistLevel(),
                artist.getVerificationStatus(),
                artist.getBio(),
                artist.getProfileImageUrl(),
                artworkCount,
                followers,
                following,
                artist.getCreatedAt()
        );
    }
}