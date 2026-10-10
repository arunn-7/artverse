package com.artverse.controller;

import com.artverse.service.FirestoreLikeService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/artworks")
public class LikeController {

    private final FirestoreLikeService likeService;

    public LikeController(FirestoreLikeService likeService) {
        this.likeService = likeService;
    }

    @PostMapping("/{artworkId}/like")
    public String likeArtwork(
            @PathVariable String artworkId,
            Authentication authentication) {

        return likeService.likeArtwork(
                artworkId, authentication.getName());
    }

    @PostMapping("/{artworkId}/unlike")
    public String unlikeArtwork(
            @PathVariable String artworkId,
            Authentication authentication) {

        return likeService.unlikeArtwork(
                artworkId, authentication.getName());
    }
}