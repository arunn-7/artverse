package com.artverse.controller;

import com.artverse.dto.MarketplaceArtworkResponse;
import com.artverse.service.ArtworkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/marketplace")
public class MarketplaceController {

    @Autowired
    private ArtworkService artworkService;

    @GetMapping
    public List<MarketplaceArtworkResponse> getMarketplace() {

        return artworkService.getMarketplaceArtworks();
    }
}