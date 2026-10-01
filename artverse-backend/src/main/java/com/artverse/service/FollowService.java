package com.artverse.service;

import com.artverse.entity.Follow;
import com.artverse.entity.User;
import com.artverse.exception.UserNotFoundException;
import com.artverse.repository.FollowRepository;
import com.artverse.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import java.util.List;
import com.artverse.dto.UserSummaryResponse;
import com.artverse.entity.NotificationType;

@Service
public class FollowService {

    @Autowired
    private FollowRepository followRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationService notificationService;

    public String followUser(Long userId, Authentication authentication) {

        User follower = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        User following = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (follower.getId().equals(following.getId())) {
            throw new RuntimeException("You cannot follow yourself");
        }

        if (followRepository.existsByFollowerAndFollowing(follower, following)) {
            throw new RuntimeException("Already following this user");
        }

        Follow follow = new Follow();
        follow.setFollower(follower);
        follow.setFollowing(following);

        followRepository.save(follow);

        notificationService.createNotification(
                following,
                follower,
                "New Follower",
                follower.getFullName() + " started following you.",
                NotificationType.FOLLOW
        );

        return "User followed successfully";
    }
    public String unfollowUser(Long followingId, Authentication authentication) {

        User follower = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        User following = userRepository.findById(followingId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Follow follow = followRepository.findByFollowerAndFollowing(follower, following)
                .orElseThrow(() -> new RuntimeException("You are not following this user"));

        followRepository.delete(follow);

        return "User unfollowed successfully";
    }
    public List<UserSummaryResponse> getFollowers(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        return followRepository.findByFollowing(user)
                .stream()
                .map(follow -> new UserSummaryResponse(
                        follow.getFollower().getId(),
                        follow.getFollower().getFullName(),
                        follow.getFollower().getProfileImageUrl()
                ))
                .toList();
    }
    public List<UserSummaryResponse> getFollowing(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        return followRepository.findByFollower(user)
                .stream()
                .map(follow -> new UserSummaryResponse(
                        follow.getFollowing().getId(),
                        follow.getFollowing().getFullName(),
                        follow.getFollowing().getProfileImageUrl()
                ))
                .toList();
    }
    public long getFollowersCount(User user) {
        return followRepository.countByFollowing(user);
    }

    public long getFollowingCount(User user) {
        return followRepository.countByFollower(user);
    }
}