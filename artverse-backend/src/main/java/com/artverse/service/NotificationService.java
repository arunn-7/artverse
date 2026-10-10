package com.artverse.service;

import com.artverse.dto.NotificationResponse;
import com.artverse.dto.UnreadCountResponse;
import com.artverse.entity.NotificationType;
import com.artverse.entity.User;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service
public class NotificationService {

    private final Firestore firestore;
    private final FirestoreUserService userService;

    public NotificationService(
            Firestore firestore,
            FirestoreUserService userService) {
        this.firestore = firestore;
        this.userService = userService;
    }

    public void createNotification(
            User receiver,
            User sender,
            String title,
            String message,
            NotificationType type) {

        try {
            if (receiver == null || receiver.getUserUid() == null) {
                return;
            }

            Map<String, Object> data = new HashMap<>();
            data.put("receiverUid", receiver.getUserUid());
            data.put("receiverEmail", receiver.getEmail());
            data.put("senderUid",
                    sender != null ? sender.getUserUid() : null);
            data.put("senderName",
                    sender != null ? sender.getFullName() : "ArtVerse");
            data.put("senderProfileImage",
                    sender != null ? sender.getProfileImageUrl() : null);
            data.put("title", title);
            data.put("message", message);
            data.put("type", type != null ? type.name() : "GENERAL");
            data.put("read", false);
            data.put("createdAt", Instant.now().toString());

            data.entrySet().removeIf(entry -> entry.getValue() == null);

            firestore.collection("notifications")
                    .document()
                    .set(data)
                    .get();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Notification creation interrupted");
        } catch (ExecutionException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to create notification");
        }
    }

    public List<NotificationResponse> getMyNotifications(
            Authentication authentication) {

        User user = userService.getUserByEmail(authentication.getName());

        try {
            List<DocumentSnapshot> documents = new ArrayList<>(
                    firestore.collection("notifications")
                            .whereEqualTo("receiverUid", user.getUserUid())
                            .get().get().getDocuments());

            documents.sort((a, b) -> {
                String first = a.getString("createdAt");
                String second = b.getString("createdAt");
                if (first == null) return 1;
                if (second == null) return -1;
                return second.compareTo(first);
            });

            List<NotificationResponse> responses = new ArrayList<>();

            for (DocumentSnapshot document : documents) {
                String timestamp = document.getString("createdAt");
                LocalDateTime createdAt = timestamp == null ? null
                        : LocalDateTime.ofInstant(
                        Instant.parse(timestamp), ZoneOffset.UTC);

                Boolean read = document.getBoolean("read");

                responses.add(new NotificationResponse(
                        document.getId(),
                        document.getString("title"),
                        document.getString("message"),
                        document.getString("type"),
                        document.getString("senderName") != null
                                ? document.getString("senderName") : "ArtVerse",
                        document.getString("senderProfileImage"),
                        Boolean.TRUE.equals(read),
                        createdAt));
            }

            return responses;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Notification retrieval interrupted");
        } catch (ExecutionException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to retrieve notifications");
        }
    }

    public String markAsRead(
            String notificationId,
            Authentication authentication) {

        User user = userService.getUserByEmail(authentication.getName());

        try {
            var reference = firestore.collection("notifications")
                    .document(notificationId);

            DocumentSnapshot document = reference.get().get();

            if (!document.exists()) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Notification not found");
            }

            if (!user.getUserUid().equals(
                    document.getString("receiverUid"))) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "You cannot modify this notification");
            }

            reference.update("read", true).get();
            return "Notification marked as read";

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Notification update interrupted");
        } catch (ExecutionException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to update notification");
        }
    }

    public String markAllAsRead(Authentication authentication) {

        User user = userService.getUserByEmail(authentication.getName());

        try {
            List<? extends DocumentSnapshot> documents = firestore
                    .collection("notifications")
                    .whereEqualTo("receiverUid", user.getUserUid())
                    .whereEqualTo("read", false)
                    .get()
                    .get()
                    .getDocuments();

            for (DocumentSnapshot document : documents) {
                document.getReference().update("read", true);
            }

            return "All notifications marked as read";

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Notification update interrupted");
        } catch (ExecutionException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to update notifications");
        }
    }

    public UnreadCountResponse getUnreadCount(
            Authentication authentication) {

        User user = userService.getUserByEmail(authentication.getName());

        try {
            long count = firestore.collection("notifications")
                    .whereEqualTo("receiverUid", user.getUserUid())
                    .whereEqualTo("read", false)
                    .get().get().size();

            return new UnreadCountResponse(count);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Unread count retrieval interrupted");
        } catch (ExecutionException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to count unread notifications");
        }
    }
}