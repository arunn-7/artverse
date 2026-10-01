package com.artverse.controller;

import com.artverse.service.PurchaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.artverse.dto.PurchaseHistoryResponse;
import com.artverse.dto.SalesHistoryResponse;
import java.util.List;

@RestController
@RequestMapping("/api/marketplace")
public class PurchaseController {

    @Autowired
    private PurchaseService purchaseService;

    @PostMapping("/{artworkId}/buy")
    public String buyArtwork(@PathVariable Long artworkId,
                             Authentication authentication) {

        return purchaseService.buyArtwork(artworkId, authentication);
    }
    @GetMapping("/my")
    public List<PurchaseHistoryResponse> getMyPurchases(
            Authentication authentication) {

        return purchaseService.getMyPurchases(authentication);
    }
    @GetMapping("/sales")
    public List<SalesHistoryResponse> getMySales(
            Authentication authentication) {

        return purchaseService.getMySales(authentication);
    }
}