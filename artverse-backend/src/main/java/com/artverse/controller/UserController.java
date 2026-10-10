
package com.artverse.controller;

import com.artverse.dto.ArtworkFeedResponse;
import com.artverse.dto.UserResponse;
import com.artverse.entity.User;
import com.artverse.service.ArtworkService;
import com.artverse.service.FirestoreUserService;
import com.artverse.service.UserService;
import com.artverse.dto.UpdateProfileRequest;
import com.artverse.dto.UserSummaryResponse;
import com.artverse.dto.PublicUserProfileResponse;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final FirestoreUserService firestoreUserService;
    private final ArtworkService artworkService;

    public UserController(
            UserService userService,
            FirestoreUserService firestoreUserService,
            ArtworkService artworkService) {
        this.userService = userService;
        this.firestoreUserService = firestoreUserService;
        this.artworkService = artworkService;
    }

    @GetMapping("/me")
    public UserResponse getCurrentUser(Authentication authentication) {
        return userService.getCurrentUserProfile(authentication);
    }

    @GetMapping("/me/artworks")
    public List<ArtworkFeedResponse> getMyArtworks(
            Authentication authentication) {

        User user = firestoreUserService.getUserByEmail(
                authentication.getName());

        return artworkService.getMyArtworks(user);
    }

    @PutMapping("/me")
    public String updateProfile(
            @RequestBody UpdateProfileRequest request,
            Authentication authentication) {
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
            @PathVariable String id,
            Authentication authentication) {
        return userService.getPublicProfile(id, authentication);
    }

    @PutMapping("/me/certificate")
    public String uploadCertificate(
            @RequestPart("certificate") MultipartFile certificate,
            Authentication authentication) throws IOException {
        return userService.uploadCertificate(certificate, authentication);
    }
}
