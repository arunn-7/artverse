package com.artverse.controller;

import com.artverse.dto.CommissionOfferResponse;
import com.artverse.dto.CommissionResponse;
import com.artverse.dto.CreateCommissionOfferRequest;
import com.artverse.dto.CreateCommissionRequest;
import com.artverse.service.CommissionService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.http.ResponseEntity;
import com.artverse.dto.CommissionDeliveryRequest;
import jakarta.validation.Valid;
import com.artverse.dto.CommissionDeliveryResponse;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/commissions")
public class CommissionController {

    @Autowired
    private CommissionService commissionService;


    // =========================================================
    // 1. CREATE COMMISSION REQUEST
    // =========================================================

    @PostMapping
    public CommissionResponse createCommission(
            @RequestBody CreateCommissionRequest request,
            Authentication authentication) {

        return commissionService.createCommission(
                request,
                authentication
        );
    }


    // =========================================================
    // 2. VIEW OPEN COMMISSION REQUESTS
    // =========================================================

    @GetMapping("/open")
    public List<CommissionResponse> getOpenCommissions() {

        return commissionService.getOpenCommissions();
    }


    // =========================================================
    // 3. VIEW MY COMMISSION REQUESTS
    // =========================================================

    @GetMapping("/my")
    public List<CommissionResponse> getMyCommissions(
            Authentication authentication) {

        return commissionService.getMyCommissions(
                authentication
        );
    }


    // =========================================================
    // 4. ARTIST SUBMITS OFFER
    // =========================================================

    @PostMapping("/{commissionId}/offers")
    public CommissionOfferResponse createOffer(
            @PathVariable Long commissionId,
            @RequestBody CreateCommissionOfferRequest request,
            Authentication authentication) {

        return commissionService.createOffer(
                commissionId,
                request,
                authentication
        );
    }


    // =========================================================
    // 5. CLIENT VIEW OFFERS
    // =========================================================

    @GetMapping("/{commissionId}/offers")
    public List<CommissionOfferResponse> getOffers(
            @PathVariable Long commissionId,
            Authentication authentication) {

        return commissionService.getOffersForMyCommission(
                commissionId,
                authentication
        );
    }


    // =========================================================
    // 6. CLIENT SELECTS ARTIST
    // =========================================================

    @PostMapping("/{commissionId}/offers/{offerId}/select")
    public CommissionOfferResponse selectArtist(
            @PathVariable Long commissionId,
            @PathVariable Long offerId,
            Authentication authentication) {

        return commissionService.selectArtist(
                commissionId,
                offerId,
                authentication
        );
    }

    @PostMapping("/offers/{offerId}/accept")
    public String acceptOffer(
            @PathVariable Long offerId,
            Authentication authentication) {

        return commissionService.acceptOffer(
                offerId,
                authentication
        );
    }
    @PostMapping("/{commissionId}/start")
    public String startCommission(
            @PathVariable Long commissionId,
            Authentication authentication) {

        return commissionService.startCommission(
                commissionId,
                authentication
        );
    }
    @PostMapping("/{commissionId}/complete")
    public ResponseEntity<String> completeCommission(
            @PathVariable Long commissionId,
            Authentication authentication) {

        return ResponseEntity.ok(
                commissionService.completeCommission(
                        commissionId,
                        authentication
                )
        );
    }
    @PostMapping("/{commissionId}/deliver")
    public ResponseEntity<String> submitDelivery(
            @PathVariable Long commissionId,
            @Valid @RequestBody CommissionDeliveryRequest request,
            Authentication authentication) {

        return ResponseEntity.ok(
                commissionService.submitDelivery(
                        commissionId,
                        request,
                        authentication
                )
        );
    }
    @PostMapping("/{commissionId}/approve")
    public ResponseEntity<String> approveDelivery(
            @PathVariable Long commissionId,
            Authentication authentication) {

        return ResponseEntity.ok(
                commissionService.approveDelivery(
                        commissionId,
                        authentication
                )
        );
    }
    @PostMapping("/{commissionId}/revision")
    public ResponseEntity<String> requestRevision(
            @PathVariable Long commissionId,
            @RequestBody(required = false) String message,
            Authentication authentication) {

        return ResponseEntity.ok(
                commissionService.requestRevision(
                        commissionId,
                        message,
                        authentication
                )
        );
    }
    @GetMapping("/{commissionId}/delivery")
    public ResponseEntity<CommissionDeliveryResponse> getDelivery(
            @PathVariable Long commissionId,
            Authentication authentication) {

        return ResponseEntity.ok(
                commissionService.getDelivery(
                        commissionId,
                        authentication
                )
        );
    }
}