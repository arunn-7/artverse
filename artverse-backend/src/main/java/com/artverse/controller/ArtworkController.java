package com.artverse.controller;

import com.artverse.dto.ArtworkFeedResponse;
import com.artverse.dto.ArtworkRequest;
import com.artverse.dto.ArtworkResponse;
import com.artverse.dto.SellArtworkRequest;
import com.artverse.entity.User;
import com.artverse.repository.UserRepository;
import com.artverse.service.ArtworkService;

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
    private UserRepository userRepository;

    @Autowired
    private ArtworkService artworkService;


    // =====================================================
    // CHECK WHETHER LOGGED-IN USER IS AN ARTIST
    // =====================================================

    private User requireArtist(Authentication authentication) {

        User user = userRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.UNAUTHORIZED,
                                "User not found"
                        )
                );

        if (!"ARTIST".equalsIgnoreCase(user.getAccountType())) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only artists can perform this action"
            );
        }

        return user;
    }


    // =====================================================
    // UPLOAD ARTWORK
    // ARTIST ONLY
    // =====================================================

    @PostMapping(
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public String uploadArtwork(

            @RequestParam String title,

            @RequestParam(required = false)
            String description,

            @RequestParam String category,

            @RequestPart MultipartFile image,

            Authentication authentication

    ) throws IOException {

        // IMPORTANT:
        // Normal USER accounts cannot upload artwork
        requireArtist(authentication);

        ArtworkRequest request = new ArtworkRequest();

        request.setTitle(title);
        request.setDescription(description);
        request.setCategory(category);

        return artworkService.uploadArtwork(
                request,
                image,
                authentication
        );
    }


    // =====================================================
    // GET ALL ARTWORKS
    // USER + ARTIST
    // =====================================================

    @GetMapping
    public List<ArtworkResponse> getAllArtworks() {

        return artworkService.getAllArtworks();
    }


    // =====================================================
    // GET ARTWORK BY ID
    // USER + ARTIST
    // =====================================================

    @GetMapping("/{id}")
    public ArtworkResponse getArtworkById(
            @PathVariable Long id
    ) {

        return artworkService.getArtworkById(id);
    }


    // =====================================================
    // UPDATE ARTWORK
    // ARTIST ONLY
    // =====================================================

    @PutMapping("/{id}")
    public String updateArtwork(

            @PathVariable Long id,

            @Valid
            @RequestBody ArtworkRequest request,

            Authentication authentication

    ) {

        requireArtist(authentication);

        return artworkService.updateArtwork(
                id,
                request,
                authentication
        );
    }


    // =====================================================
    // DELETE ARTWORK
    // ARTIST ONLY
    // =====================================================

    @DeleteMapping("/{id}")
    public String deleteArtwork(

            @PathVariable Long id,

            Authentication authentication

    ) {

        requireArtist(authentication);

        return artworkService.deleteArtwork(
                id,
                authentication
        );
    }


    // =====================================================
    // ARTWORK FEED
    // USER + ARTIST
    // =====================================================

    @GetMapping("/feed")
    public List<ArtworkFeedResponse> getFeed(
            Authentication authentication
    ) {

        User currentUser = userRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new RuntimeException("User not found")
                );

        return artworkService.getArtworkFeed(currentUser);
    }


    // =====================================================
    // PUT ARTWORK FOR SALE
    // ARTIST ONLY
    // =====================================================

    @PutMapping("/{id}/sell")
    public String sellArtwork(

            @PathVariable Long id,

            @RequestBody SellArtworkRequest request,

            Authentication authentication

    ) {

        requireArtist(authentication);

        return artworkService.putArtworkForSale(
                id,
                request,
                authentication
        );
    }


    // =====================================================
    // REMOVE ARTWORK FROM SALE
    // ARTIST ONLY
    // =====================================================

    @PutMapping("/{id}/remove-sale")
    public String removeArtworkFromSale(

            @PathVariable Long id,

            Authentication authentication

    ) {

        requireArtist(authentication);

        return artworkService.removeArtworkFromSale(
                id,
                authentication
        );
    }
}