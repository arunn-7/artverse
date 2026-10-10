package com.artverse.service;

import com.artverse.entity.User;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Service
public class FirestoreUserService {

    private final Firestore firestore;

    public FirestoreUserService(Firestore firestore) {
        this.firestore = firestore;
    }

    // Find user profile using normalized email as document ID
    public Optional<User> findByEmail(String email)
            throws ExecutionException, InterruptedException {

        if (email == null || email.isBlank()) {
            return Optional.empty();
        }

        String normalizedEmail = normalizeEmail(email);

        DocumentSnapshot document = firestore
                .collection("users")
                .document(normalizedEmail)
                .get()
                .get();

        if (!document.exists()) {
            return Optional.empty();
        }

        return Optional.of(mapToUser(document));
    }

    // Save or update user profile in Firestore
    public User save(User user)
            throws ExecutionException, InterruptedException {

        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new IllegalArgumentException("User email is required");
        }

        String email = normalizeEmail(user.getEmail());
        user.setEmail(email);

        Map<String, Object> data = new HashMap<>();

        data.put("email", email);
        data.put("userUid", user.getUserUid());
        data.put("fullName", user.getFullName());
        data.put("accountType", user.getAccountType());
        data.put("role", user.getRole());
        data.put("artistLevel", user.getArtistLevel());
        data.put("verificationStatus", user.getVerificationStatus());
        data.put("certificateUrl", user.getCertificateUrl());
        data.put("bio", user.getBio());
        data.put("profileImageUrl", user.getProfileImageUrl());

        LocalDateTime createdAt = user.getCreatedAt();
        data.put(
                "createdAt",
                createdAt != null ? createdAt.toString()
                        : LocalDateTime.now().toString());

        // Avoid Firestore rejecting null values
        data.entrySet().removeIf(entry -> entry.getValue() == null);

        firestore.collection("users")
                .document(email)
                .set(data)
                .get();

        return user;
    }

    // Retrieve a required user profile
    public User getUserByEmail(String email) {

        try {
            return findByEmail(email)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.UNAUTHORIZED,
                            "User profile not found in Firestore"));

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Firestore user lookup interrupted",
                    e);

        } catch (ExecutionException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Unable to retrieve user from Firestore",
                    e);
        }
    }

    private User mapToUser(DocumentSnapshot document) {

        User user = new User();

        user.setEmail(document.getString("email"));
        user.setUserUid(document.getString("userUid"));
        user.setFullName(document.getString("fullName"));
        user.setAccountType(document.getString("accountType"));
        user.setRole(document.getString("role"));
        user.setArtistLevel(document.getString("artistLevel"));
        user.setVerificationStatus(
                document.getString("verificationStatus"));
        user.setCertificateUrl(document.getString("certificateUrl"));
        user.setBio(document.getString("bio"));
        user.setProfileImageUrl(document.getString("profileImageUrl"));

        String createdAt = document.getString("createdAt");

        if (createdAt != null) {
            try {
                user.setCreatedAt(LocalDateTime.parse(createdAt));
            } catch (Exception ignored) {
                // Keep the default creation time if parsing fails.
            }
        }

        return user;
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
    public User getUserByUid(String uid) {
        try {
            var documents = firestore.collection("users")
                    .whereEqualTo("userUid", uid)
                    .limit(1)
                    .get()
                    .get()
                    .getDocuments();

            if (documents.isEmpty()) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User profile not found for UID");
            }

            return mapToUser(documents.get(0));

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "User lookup interrupted");
        } catch (ExecutionException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to look up user");
        }
    }
}