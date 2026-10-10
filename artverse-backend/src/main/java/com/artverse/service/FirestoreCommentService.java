package com.artverse.service;

import com.artverse.dto.CommentResponse;
import com.artverse.entity.NotificationType;
import com.artverse.entity.User;
import com.artverse.model.FirestoreArtwork;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service
public class FirestoreCommentService {

    private final Firestore firestore;
    private final FirestoreUserService userService;
    private final FirestoreArtworkService artworkService;
    private final NotificationService notificationService;

    public FirestoreCommentService(
            Firestore firestore,
            FirestoreUserService userService,
            FirestoreArtworkService artworkService,
            NotificationService notificationService) {

        this.firestore = firestore;
        this.userService = userService;
        this.artworkService = artworkService;
        this.notificationService = notificationService;
    }

    // Add a comment to an artwork
    public String addComment(
            String artworkId,
            String email,
            String text) {

        if (text == null || text.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Comment cannot be empty");
        }

        try {
            FirestoreArtwork artwork =
                    artworkService.getArtworkById(artworkId);

            if (artwork == null) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Artwork not found");
            }

            User user = userService.getUserByEmail(email);

            if (user.getUserUid() == null
                    || user.getUserUid().isBlank()) {
                throw new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "User UID not found");
            }

            String commentId = firestore
                    .collection("comments")
                    .document()
                    .getId();

            String createdAt = Instant.now().toString();

            Map<String, Object> data = new HashMap<>();
            data.put("artworkId", artworkId);
            data.put("userUid", user.getUserUid());
            data.put("userEmail", user.getEmail());
            data.put("userName", user.getFullName());
            data.put("text", text.trim());
            data.put("createdAt", createdAt);

            firestore.collection("comments")
                    .document(commentId)
                    .create(data)
                    .get();

            // Notify the artwork owner, except for self-comments.
            String artistUid = artwork.getArtistUid();

            if (artistUid != null
                    && !artistUid.equals(user.getUserUid())) {

                User receiver = userService.getUserByUid(artistUid);

                notificationService.createNotification(
                        receiver,
                        user,
                        "New Comment",
                        user.getFullName()
                                + " commented on your artwork \""
                                + artwork.getTitle() + "\".",
                        NotificationType.COMMENT);
            }

            return "Comment added successfully. ID: " + commentId;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Comment operation interrupted");

        } catch (ExecutionException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to save comment");
        }
    }

    // Retrieve comments for an artwork
    public List<CommentResponse> getComments(String artworkId) {

        try {
            FirestoreArtwork artwork =
                    artworkService.getArtworkById(artworkId);

            if (artwork == null) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Artwork not found");
            }

            List<? extends DocumentSnapshot> documents = firestore
                    .collection("comments")
                    .whereEqualTo("artworkId", artworkId)
                    .get()
                    .get()
                    .getDocuments();

            List<CommentResponse> responses = new ArrayList<>();

            for (DocumentSnapshot document : documents) {

                String timestamp =
                        document.getString("createdAt");

                LocalDateTime createdAt = timestamp == null
                        ? null
                        : LocalDateTime.ofInstant(
                        Instant.parse(timestamp),
                        ZoneOffset.UTC);

                responses.add(new CommentResponse(
                        document.getId(),
                        document.getString("text"),
                        document.getString("userName"),
                        createdAt));
            }

            // Oldest comments first.
            responses.sort((a, b) -> {
                if (a.getCreatedAt() == null) return 1;
                if (b.getCreatedAt() == null) return -1;

                return a.getCreatedAt()
                        .compareTo(b.getCreatedAt());
            });

            return responses;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Comment retrieval interrupted");

        } catch (ExecutionException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to retrieve comments");
        }
    }

    // Delete a comment only if the current user owns it
    public String deleteComment(
            String commentId,
            String email) {

        try {
            User user = userService.getUserByEmail(email);

            var reference = firestore
                    .collection("comments")
                    .document(commentId);

            DocumentSnapshot document =
                    reference.get().get();

            if (!document.exists()) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Comment not found");
            }

            String commentOwnerUid =
                    document.getString("userUid");

            if (user.getUserUid() == null
                    || !user.getUserUid().equals(commentOwnerUid)) {

                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "You can only delete your own comments");
            }

            reference.delete().get();

            return "Comment deleted successfully";

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Comment deletion interrupted");

        } catch (ExecutionException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to delete comment");
        }
    }
}