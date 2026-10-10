package com.artverse.controller;

import com.artverse.dto.ArtworkFeedResponse;
import com.artverse.dto.ArtworkRequest;
import com.artverse.dto.ArtworkResponse;
import com.artverse.dto.SellArtworkRequest;
import com.artverse.entity.User;
import com.artverse.service.ArtworkService;
import com.artverse.service.FirestoreUserService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.core.Authentication;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/artworks")
@SecurityRequirement(name = "Bearer Authentication")
public class ArtworkController {


    @Autowired
    private FirestoreUserService firestoreUserService;

    @Autowired
    private ArtworkService artworkService;

    // Check whether the logged-in user is an artist
    private User requireArtist(Authentication authentication) {

        User user = firestoreUserService.getUserByEmail(
                authentication.getName());

        if (!"ARTIST".equalsIgnoreCase(user.getAccountType())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only artists can perform this action");
        }

        return user;
    }

    // Upload artwork - artist only
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String uploadArtwork(
            @RequestParam String title,
            @RequestParam(required = false) String description,
            @RequestParam String category,
            @RequestPart MultipartFile image,
            Authentication authentication) throws IOException {

        requireArtist(authentication);

        ArtworkRequest request = new ArtworkRequest();
        request.setTitle(title);
        request.setDescription(description);
        request.setCategory(category);

        return artworkService.uploadArtwork(
                request, image, authentication);
    }

    // Get all artworks
    @GetMapping
    public List<ArtworkResponse> getAllArtworks() {
        return artworkService.getAllArtworks();
    }

    // Get artworks belonging to the logged-in artist
    @GetMapping("/my-artworks")
    public List<ArtworkFeedResponse> getMyArtworks(
            Authentication authentication) {

        User user = requireArtist(authentication);
        return artworkService.getMyArtworks(user);
    }

    // Artwork feed
    @GetMapping("/feed")
    public List<ArtworkFeedResponse> getFeed(
            Authentication authentication) {

        if (authentication == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authentication required");
        }

        User currentUser = firestoreUserService.getUserByEmail(
                authentication.getName());

        return artworkService.getArtworkFeed(currentUser);
    }

    // Get artwork by Firestore document ID
    @GetMapping("/{id}")
    public ArtworkResponse getArtworkById(
            @PathVariable String id) {
        return artworkService.getArtworkById(id);
    }

    // Update artwork - artist only
    @PutMapping("/{id}")
    public String updateArtwork(
            @PathVariable String id,
            @Valid @RequestBody ArtworkRequest request,
            Authentication authentication) {

        requireArtist(authentication);

        return artworkService.updateArtwork(
                id, request, authentication);
    }

    // Delete artwork - artist only
    @DeleteMapping("/{id}")
    public String deleteArtwork(
            @PathVariable String id,
            Authentication authentication) {

        requireArtist(authentication);

        return artworkService.deleteArtwork(id, authentication);
    }

    // Put artwork for sale - artist only
    @PutMapping("/{id}/sell")
    public String sellArtwork(
            @PathVariable String id,
            @RequestBody SellArtworkRequest request,
            Authentication authentication) {

        requireArtist(authentication);

        return artworkService.putArtworkForSale(
                id, request, authentication);
    }

    // Remove artwork from sale - artist only
    @PutMapping("/{id}/remove-sale")
    public String removeArtworkFromSale(
            @PathVariable String id,
            Authentication authentication) {

        requireArtist(authentication);

        return artworkService.removeArtworkFromSale(
                id, authentication);
    }
}