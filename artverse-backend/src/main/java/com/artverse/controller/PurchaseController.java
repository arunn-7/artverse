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

    @PostMapping("/buy/{artworkId}")
    public String buyArtwork(
            @PathVariable String artworkId,
            Authentication authentication) {

        throw new UnsupportedOperationException(
                "Use the Razorpay payment flow: create order, then verify payment."
        );
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