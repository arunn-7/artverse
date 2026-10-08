package com.artverse.controller;

import com.artverse.dto.ArtworkFeedResponse;
import com.artverse.dto.UserResponse;
import com.artverse.entity.User;
import com.artverse.exception.UserNotFoundException;
import com.artverse.repository.UserRepository;
import com.artverse.service.ArtworkService;
import com.artverse.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import com.artverse.dto.UpdateProfileRequest;
import com.artverse.dto.UserSummaryResponse;
import com.artverse.dto.PublicUserProfileResponse;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ArtworkService artworkService;

    @GetMapping("/me")
    public UserResponse getCurrentUser(Authentication authentication) {

        return userService.getCurrentUserProfile(authentication);
    }

    @GetMapping("/me/artworks")
    public List<ArtworkFeedResponse> getMyArtworks(Authentication authentication) {

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        return artworkService.getMyArtworks(user);
    }
    @PutMapping("/me")
    public String updateProfile(
            @RequestBody UpdateProfileRequest request,
            Authentication authentication) {

        System.out.println("===== UPDATE PROFILE CONTROLLER =====");

        return userService.updateProfile(request, authentication);
    }
    @PutMapping("/me/profile-picture")
    public String uploadProfilePicture(
            @RequestPart("image") MultipartFile image,
            Authentication authentication) throws IOException {

        return userService.uploadProfilePicture(image, authentication);
    }
    @GetMapping("/search")
    public List<UserSummaryResponse> searchUsers(
            @RequestParam String keyword) {

        return userService.searchUsers(keyword);
    }
    @GetMapping("/{id}")
    public PublicUserProfileResponse getPublicProfile(
            @PathVariable Long id,
            Authentication authentication) {

        return userService.getPublicProfile(id, authentication);
    }
    @PutMapping("/me/certificate")
    public String uploadCertificate(
            @RequestPart("certificate") MultipartFile certificate,
            Authentication authentication) throws IOException {

        return userService.uploadCertificate(
                certificate,
                authentication
        );
    }
}