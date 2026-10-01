package com.artverse.service;

import com.artverse.dto.UserResponse;
import com.artverse.entity.User;
import com.artverse.exception.UserNotFoundException;
import com.artverse.repository.ArtworkRepository;
import com.artverse.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import com.artverse.dto.UpdateProfileRequest;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import com.artverse.repository.FollowRepository;
import com.artverse.dto.UserSummaryResponse;
import java.util.List;
import com.artverse.dto.PublicUserProfileResponse;




@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ArtworkRepository artworkRepository;

    @Autowired
    private FollowRepository followRepository;

    @Autowired
    private CloudinaryService cloudinaryService;

    @Autowired
    private ArtworkService artworkService;

    public UserResponse getCurrentUserProfile(Authentication authentication) {

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        return new UserResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole(),
                user.getBio(),
                user.getProfileImageUrl(),
                artworkRepository.countByUser(user),
                followRepository.countByFollowing(user),
                followRepository.countByFollower(user),
                user.getCreatedAt()
        );
    }
    public String updateProfile(UpdateProfileRequest request,
                                Authentication authentication) {

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        user.setFullName(request.getFullName());
        user.setBio(request.getBio());

        userRepository.save(user);

        return "Profile updated successfully";
    }
    public String uploadProfilePicture(MultipartFile image,
                                       Authentication authentication) throws IOException {

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        String imageUrl = cloudinaryService.uploadImage(image);

        user.setProfileImageUrl(imageUrl);

        userRepository.save(user);

        return "Profile picture updated successfully";
    }
    public List<UserSummaryResponse> searchUsers(String keyword) {

        return userRepository.findByFullNameContainingIgnoreCase(keyword)
                .stream()
                .map(user -> new UserSummaryResponse(
                        user.getId(),
                        user.getFullName(),
                        user.getProfileImageUrl()
                ))
                .toList();
    }
    public PublicUserProfileResponse getPublicProfile(Long userId,
                                                      Authentication authentication) {

        User profileUser = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        User currentUser = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        boolean isFollowing = followRepository.existsByFollowerAndFollowing(
                currentUser,
                profileUser
        );

        return new PublicUserProfileResponse(
                profileUser.getId(),
                profileUser.getFullName(),
                profileUser.getBio(),
                profileUser.getProfileImageUrl(),
                artworkRepository.countByUser(profileUser),
                followRepository.countByFollowing(profileUser),
                followRepository.countByFollower(profileUser),
                isFollowing,
                artworkService.getMyArtworks(profileUser)
        );
    }

}