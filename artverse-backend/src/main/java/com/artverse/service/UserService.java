package com.artverse.service;

import com.artverse.dto.UserResponse;
import com.artverse.dto.UpdateProfileRequest;
import com.artverse.dto.UserSummaryResponse;
import com.artverse.dto.PublicUserProfileResponse;
import com.artverse.entity.User;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

@Service
public class UserService {

    @Autowired
    private Firestore firestore;

    @Autowired
    private FirestoreUserService firestoreUserService;

    @Autowired
    private CloudinaryService cloudinaryService;

    @Autowired
    private ArtworkService artworkService;

    @Autowired
    private FollowService followService;

    public UserResponse getCurrentUserProfile(
            Authentication authentication) {

        User user = firestoreUserService.getUserByEmail(
                authentication.getName());

        UserResponse response = new UserResponse(
                null,
                user.getFullName(),
                user.getEmail(),
                user.getRole(),
                user.getBio(),
                user.getProfileImageUrl(),
                user.getCreatedAt());

        response.setArtworkCount(
                (long) artworkService.getMyArtworks(user).size());

        response.setFollowers(followService.getFollowersCount(user));
        response.setFollowing(followService.getFollowingCount(user));

        return response;
    }

    public String updateProfile(
            UpdateProfileRequest request,
            Authentication authentication) {

        User user = firestoreUserService.getUserByEmail(
                authentication.getName());

        user.setFullName(request.getFullName());
        user.setBio(request.getBio());

        saveUserProfile(user);

        return "Profile updated successfully";
    }

    public String uploadProfilePicture(
            MultipartFile image,
            Authentication authentication) throws IOException {

        User user = firestoreUserService.getUserByEmail(
                authentication.getName());

        String imageUrl = cloudinaryService.uploadImage(image);
        user.setProfileImageUrl(imageUrl);

        saveUserProfile(user);

        return "Profile picture updated successfully";
    }

    public List<UserSummaryResponse> searchUsers(String keyword) {

        if (keyword == null || keyword.isBlank()) {
            return new ArrayList<>();
        }

        try {
            List<? extends DocumentSnapshot> documents = firestore
                    .collection("users")
                    .get()
                    .get()
                    .getDocuments();

            List<UserSummaryResponse> results = new ArrayList<>();
            String searchTerm = keyword.toLowerCase(Locale.ROOT);

            for (DocumentSnapshot document : documents) {
                String fullName = document.getString("fullName");
                String uid = document.getString("userUid");

                if (fullName != null
                        && uid != null
                        && fullName.toLowerCase(Locale.ROOT)
                        .contains(searchTerm)) {

                    results.add(new UserSummaryResponse(
                            uid,
                            fullName,
                            document.getString("profileImageUrl")));
                }
            }

            return results;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "User search interrupted");
        } catch (ExecutionException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to search users");
        }
    }

    public PublicUserProfileResponse getPublicProfile(
            String userUid,
            Authentication authentication) {

        User profileUser = firestoreUserService.getUserByUid(userUid);

        List<com.artverse.dto.ArtworkFeedResponse> artworks =
                artworkService.getMyArtworks(profileUser);

        long followers = followService.getFollowersCount(profileUser);
        long following = followService.getFollowingCount(profileUser);

        boolean isFollowing = false;

        if (authentication != null
                && authentication.getName() != null) {

            User currentUser = firestoreUserService.getUserByEmail(
                    authentication.getName());

            if (currentUser.getUserUid() != null
                    && !currentUser.getUserUid().equals(userUid)) {

                try {
                    String followDocumentId =
                            currentUser.getUserUid() + "_" + userUid;

                    isFollowing = firestore
                            .collection("follows")
                            .document(followDocumentId)
                            .get()
                            .get()
                            .exists();

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new ResponseStatusException(
                            HttpStatus.INTERNAL_SERVER_ERROR,
                            "Follow status lookup interrupted");
                } catch (ExecutionException e) {
                    throw new ResponseStatusException(
                            HttpStatus.INTERNAL_SERVER_ERROR,
                            "Failed to retrieve follow status");
                }
            }
        }

        return new PublicUserProfileResponse(
                profileUser.getUserUid(),
                profileUser.getFullName(),
                profileUser.getBio(),
                profileUser.getProfileImageUrl(),
                artworks.size(),
                followers,
                following,
                isFollowing,
                artworks);
    }

    public String uploadCertificate(
            MultipartFile certificate,
            Authentication authentication) throws IOException {

        User user = firestoreUserService.getUserByEmail(
                authentication.getName());

        if (!"ARTIST".equalsIgnoreCase(user.getAccountType())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only artists can upload certificates");
        }

        String level = user.getArtistLevel();

        if (!"INTERMEDIATE".equalsIgnoreCase(level)
                && !"PROFESSIONAL".equalsIgnoreCase(level)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Certificate is not required for this artist level");
        }

        String certificateUrl =
                cloudinaryService.uploadCertificate(certificate);

        user.setCertificateUrl(certificateUrl);
        user.setVerificationStatus("PENDING");

        saveUserProfile(user);

        return "Certificate uploaded successfully";
    }

    private void saveUserProfile(User user) {
        try {
            firestoreUserService.save(user);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Profile update interrupted");
        } catch (ExecutionException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to save profile");
        }
    }
}