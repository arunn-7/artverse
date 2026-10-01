package com.artverse.controller;

import com.artverse.dto.SellerDashboardResponse;
import com.artverse.service.PurchaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.artverse.dto.BuyerDashboardResponse;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private PurchaseService purchaseService;

    @GetMapping("/seller")
    public SellerDashboardResponse getSellerDashboard(
            Authentication authentication) {

        return purchaseService.getSellerDashboard(authentication);
    }
    @GetMapping("/buyer")
    public BuyerDashboardResponse getBuyerDashboard(
            Authentication authentication) {

        return purchaseService.getBuyerDashboard(authentication);
    }
}