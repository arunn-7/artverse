
package com.artverse.service;

import com.artverse.dto.LoginRequest;
import com.artverse.dto.LoginResponse;
import com.artverse.dto.RegisterRequest;
import com.artverse.entity.User;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service
public class AuthService {

    private final FirestoreUserService firestoreUserService;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    @Value("${firebase.web.api-key}")
    private String firebaseWebApiKey;

    public AuthService(
            FirestoreUserService firestoreUserService,
            ObjectMapper objectMapper) {
        this.firestoreUserService = firestoreUserService;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newHttpClient();
    }

    public String register(RegisterRequest request) {
        String email = normalizeEmail(request.getEmail());
        String accountType = request.getAccountType()
                .trim().toUpperCase(Locale.ROOT);

        String artistLevel = null;
        String verificationStatus;

        if (!accountType.equals("USER")
                && !accountType.equals("ARTIST")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Invalid account type");
        }

        if (accountType.equals("ARTIST")) {
            if (request.getArtistLevel() == null
                    || request.getArtistLevel().isBlank()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Artist level is required");
            }

            artistLevel = request.getArtistLevel()
                    .trim().toUpperCase(Locale.ROOT);

            if (!artistLevel.equals("BEGINNER")
                    && !artistLevel.equals("INTERMEDIATE")
                    && !artistLevel.equals("PROFESSIONAL")) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Invalid artist level");
            }

            // Preserve the current ArtVerse registration rules.
            verificationStatus = artistLevel.equals("BEGINNER")
                    ? "NOT_REQUIRED" : "PENDING";
        } else {
            verificationStatus = "NOT_APPLICABLE";
        }

        try {
            if (firestoreUserService.findByEmail(email).isPresent()) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT, "Email already exists");
            }
        } catch (ExecutionException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Unable to check existing user");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "User lookup interrupted");
        }

        UserRecord createdAccount;

        try {
            createdAccount = FirebaseAuth.getInstance().createUser(
                    new UserRecord.CreateRequest()
                            .setEmail(email)
                            .setPassword(request.getPassword())
                            .setDisplayName(request.getFullName().trim())
            );
        } catch (FirebaseAuthException e) {
            if ("EMAIL_EXISTS".equals(e.getAuthErrorCode().name())) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT, "Email already exists");
            }
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Firebase account creation failed");
        }

        User user = new User();
        user.setUserUid(createdAccount.getUid());
        user.setFullName(request.getFullName().trim());
        user.setEmail(email);
        user.setAccountType(accountType);
        user.setRole(accountType);
        user.setArtistLevel(artistLevel);
        user.setVerificationStatus(verificationStatus);
        user.setCreatedAt(LocalDateTime.now());

        try {
            firestoreUserService.save(user);
            return "Registration Successful";
        } catch (Exception e) {
            // Avoid leaving an unlinked Auth account if profile creation fails.
            try {
                FirebaseAuth.getInstance().deleteUser(createdAccount.getUid());
            } catch (FirebaseAuthException rollbackError) {
                // Log this operationally in production.
            }

            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Account created but profile save failed. Please retry.");
        }
    }

    public LoginResponse login(LoginRequest request) {
        String email = normalizeEmail(request.getEmail());

        String url = "https://identitytoolkit.googleapis.com/v1/"
                + "accounts:signInWithPassword?key="
                + URLEncoder.encode(
                firebaseWebApiKey, StandardCharsets.UTF_8);

        try {
            String body = objectMapper.writeValueAsString(Map.of(
                    "email", email,
                    "password", request.getPassword(),
                    "returnSecureToken", true
            ));

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = httpClient.send(
                    httpRequest,
                    HttpResponse.BodyHandlers.ofString());

            JsonNode json = objectMapper.readTree(response.body());

            if (response.statusCode() != 200) {
                throw new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Invalid email or password");
            }

            String idToken = json.path("idToken").asText(null);
            String uid = json.path("localId").asText(null);

            if (idToken == null || uid == null) {
                throw new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Firebase sign-in response was incomplete");
            }

            User user = firestoreUserService.findByEmail(email)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.FORBIDDEN,
                            "User profile not found"));

            // Link the actual Firebase UID to the Firestore profile.
            user.setUserUid(uid);
            firestoreUserService.save(user);

            return new LoginResponse(
                    idToken,
                    user.getFullName(),
                    user.getEmail(),
                    user.getRole()
            );

        } catch (ResponseStatusException e) {
            throw e;
        } catch (IOException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Unable to communicate with Firebase");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Firebase login interrupted");
        } catch (ExecutionException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Unable to load user profile");
        }
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Email is required");
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
