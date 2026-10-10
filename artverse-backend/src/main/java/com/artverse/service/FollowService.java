package com.artverse.service;

import com.artverse.dto.UserSummaryResponse;
import com.artverse.entity.NotificationType;
import com.artverse.entity.User;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service
public class FollowService {

    private final Firestore firestore;
    private final FirestoreUserService userService;
    private final NotificationService notificationService;

    public FollowService(
            Firestore firestore,
            FirestoreUserService userService,
            NotificationService notificationService) {
        this.firestore = firestore;
        this.userService = userService;
        this.notificationService = notificationService;
    }

    private String currentUid(Authentication authentication) {
        User user = userService.getUserByEmail(authentication.getName());

        if (user.getUserUid() == null || user.getUserUid().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Firebase UID not found");
        }

        return user.getUserUid();
    }

    private String followId(String followerUid, String followingUid) {
        return followerUid + "_" + followingUid;
    }

    public String followUser(String followingUid,
                             Authentication authentication) {
        try {
            String followerUid = currentUid(authentication);

            if (followerUid.equals(followingUid)) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "You cannot follow yourself");
            }

            User follower = userService.getUserByUid(followerUid);
            User following = userService.getUserByUid(followingUid);

            String id = followId(followerUid, followingUid);
            var reference = firestore.collection("follows").document(id);

            if (reference.get().get().exists()) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Already following this user");
            }

            reference.set(Map.of(
                    "followerUid", followerUid,
                    "followingUid", followingUid,
                    "createdAt", Instant.now().toString()
            )).get();

            notificationService.createNotification(
                    following,
                    follower,
                    "New Follower",
                    follower.getFullName() + " started following you.",
                    NotificationType.FOLLOW
            );

            return "User followed successfully";

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Follow operation interrupted");
        } catch (ExecutionException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Could not save follow relationship");
        }
    }

    public String unfollowUser(String followingUid,
                               Authentication authentication) {
        try {
            String followerUid = currentUid(authentication);
            String id = followId(followerUid, followingUid);

            var reference = firestore.collection("follows").document(id);

            if (!reference.get().get().exists()) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "You are not following this user");
            }

            reference.delete().get();
            return "User unfollowed successfully";

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Unfollow operation interrupted");
        } catch (ExecutionException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Could not remove follow relationship");
        }
    }

    public List<UserSummaryResponse> getFollowers(String userUid) {
        return getUsersForRelationship("followerUid", "followingUid", userUid);
    }

    public List<UserSummaryResponse> getFollowing(String userUid) {
        return getUsersForRelationship("followingUid", "followerUid", userUid);
    }

    private List<UserSummaryResponse> getUsersForRelationship(
            String resultField,
            String queryField,
            String userUid) {
        try {
            List<QueryDocumentSnapshot> documents = firestore
                    .collection("follows")
                    .whereEqualTo(queryField, userUid)
                    .get()
                    .get()
                    .getDocuments();

            List<UserSummaryResponse> result = new ArrayList<>();

            for (DocumentSnapshot document : documents) {
                String otherUid = document.getString(resultField);

                if (otherUid == null) {
                    continue;
                }

                User user = userService.getUserByUid(otherUid);

                result.add(new UserSummaryResponse(
                        user.getUserUid(),
                        user.getFullName(),
                        user.getProfileImageUrl()
                ));
            }

            return result;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Follow lookup interrupted");
        } catch (ExecutionException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Could not retrieve follow relationships");
        }
    }

    public long getFollowersCount(User user) {
        return countRelationships("followingUid", user.getUserUid());
    }

    public long getFollowingCount(User user) {
        return countRelationships("followerUid", user.getUserUid());
    }

    private long countRelationships(String field, String userUid) {
        if (userUid == null || userUid.isBlank()) {
            return 0;
        }

        try {
            return firestore.collection("follows")
                    .whereEqualTo(field, userUid)
                    .get()
                    .get()
                    .size();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Follow count interrupted");
        } catch (ExecutionException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Could not count follow relationships");
        }
    }
}