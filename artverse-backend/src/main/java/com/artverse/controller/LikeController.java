package com.artverse.controller;

import com.artverse.service.LikeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/artworks")
public class LikeController {

    @Autowired
    private LikeService likeService;

    @PostMapping("/{artworkId}/like")
    public String likeArtwork(@PathVariable Long artworkId,
                              Authentication authentication) {

        return likeService.likeArtwork(
                artworkId,
                authentication.getName()
        );
    }
    @PostMapping("/{artworkId}/unlike")
    public String unlikeArtwork(@PathVariable Long artworkId,
                                Authentication authentication) {

        return likeService.unlikeArtwork(
                artworkId,
                authentication.getName()
        );
    }
}