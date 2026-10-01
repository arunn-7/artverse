package com.artverse.controller;

import java.util.List;
import com.artverse.dto.ArtworkRequest;
import com.artverse.service.ArtworkService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.artverse.dto.ArtworkResponse;
import com.artverse.dto.ArtworkFeedResponse;
import com.artverse.entity.User;
import com.artverse.repository.UserRepository;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.RequestPart;
import java.io.IOException;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import com.artverse.dto.SellArtworkRequest;


@RestController
@RequestMapping("/api/artworks")
@SecurityRequirement(name = "Bearer Authentication")
public class ArtworkController {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ArtworkService artworkService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String uploadArtwork(

            @RequestParam String title,
            @RequestParam(required = false) String description,
            @RequestParam String category,
            @RequestPart MultipartFile image,
            Authentication authentication

    ) throws IOException {

        ArtworkRequest request = new ArtworkRequest();
        request.setTitle(title);
        request.setDescription(description);
        request.setCategory(category);

        return artworkService.uploadArtwork(request, image, authentication);
    }
    @GetMapping
    public List<ArtworkResponse> getAllArtworks() {
        return artworkService.getAllArtworks();
    }
    @GetMapping("/{id}")
    public ArtworkResponse getArtworkById(@PathVariable Long id) {
        return artworkService.getArtworkById(id);
    }
    @PutMapping("/{id}")
    public String updateArtwork(@PathVariable Long id,
                                @Valid @RequestBody ArtworkRequest request,
                                Authentication authentication) {

        return artworkService.updateArtwork(id, request, authentication);
    }
    @DeleteMapping("/{id}")
    public String deleteArtwork(@PathVariable Long id,
                                Authentication authentication) {

        return artworkService.deleteArtwork(id, authentication);
    }
    @GetMapping("/feed")
    public List<ArtworkFeedResponse> getFeed(Authentication authentication) {

        User currentUser = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return artworkService.getArtworkFeed(currentUser);
    }
    @PutMapping("/{id}/sell")
    public String sellArtwork(@PathVariable Long id,
                              @RequestBody SellArtworkRequest request,
                              Authentication authentication) {

        return artworkService.putArtworkForSale(id, request, authentication);
    }
    @PutMapping("/{id}/remove-sale")
    public String removeArtworkFromSale(@PathVariable Long id,
                                        Authentication authentication) {

        return artworkService.removeArtworkFromSale(id, authentication);
    }
}