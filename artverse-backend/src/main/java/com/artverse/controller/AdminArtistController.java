package com.artverse.controller;

import com.artverse.dto.AdminArtistDetailsResponse;
import com.artverse.entity.User;
import com.artverse.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/artists")
public class AdminArtistController {

    @Autowired
    private UserRepository userRepository;


    // ==========================================
    // GET ALL ARTISTS
    // ==========================================

    @GetMapping
    public List<AdminArtistDetailsResponse> getAllArtists() {

        return userRepository
                .findByAccountType("ARTIST")
                .stream()
                .map(this::convertToResponse)
                .toList();
    }


    // ==========================================
    // GET ARTIST BY ID
    // ==========================================

    @GetMapping("/{id}")
    public AdminArtistDetailsResponse getArtistById(
            @PathVariable Long id) {

        User artist = userRepository
                .findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Artist not found")
                );

        if (!"ARTIST".equals(artist.getAccountType())) {
            throw new RuntimeException("User is not an artist");
        }

        return convertToResponse(artist);
    }


    // ==========================================
    // CONVERT USER → ADMIN ARTIST RESPONSE
    // ==========================================

    private AdminArtistDetailsResponse convertToResponse(
            User artist) {

        return new AdminArtistDetailsResponse(
                artist.getId(),
                artist.getFullName(),
                artist.getEmail(),
                artist.getAccountType(),
                artist.getArtistLevel(),
                artist.getVerificationStatus(),
                artist.getBio(),
                artist.getProfileImageUrl(),
                0L,
                0L,
                0L,
                artist.getCreatedAt()
        );
    }
}