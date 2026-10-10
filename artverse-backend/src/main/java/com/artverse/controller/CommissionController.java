
package com.artverse.controller;

import com.artverse.dto.CommissionDeliveryRequest;
import com.artverse.dto.CommissionDeliveryResponse;
import com.artverse.dto.CommissionOfferResponse;
import com.artverse.dto.CommissionResponse;
import com.artverse.dto.CreateCommissionOfferRequest;
import com.artverse.dto.CreateCommissionRequest;
import com.artverse.service.FirestoreCommissionService;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/commissions")
public class CommissionController {

    @Autowired
    private FirestoreCommissionService commissionService;

    // 1. Create commission
    @PostMapping
    public CommissionResponse createCommission(
            @RequestBody @Valid CreateCommissionRequest request,
            Authentication authentication) {

        return commissionService.createCommission(request, authentication);
    }

    // 2. View open commissions
    @GetMapping("/open")
    public List<CommissionResponse> getOpenCommissions() {
        return commissionService.getOpenCommissions();
    }

    // 3. View my commissions
    @GetMapping("/my")
    public List<CommissionResponse> getMyCommissions(
            Authentication authentication) {

        return commissionService.getMyCommissions(authentication);
    }

    // 4. Artist submits offer
    @PostMapping("/{commissionId}/offers")
    public CommissionOfferResponse createOffer(
            @PathVariable String commissionId,
            @RequestBody @Valid CreateCommissionOfferRequest request,
            Authentication authentication) {

        return commissionService.createOffer(
                commissionId, request, authentication);
    }

    // 5. Client views offers for their commission
    @GetMapping("/{commissionId}/offers")
    public List<CommissionOfferResponse> getOffers(
            @PathVariable String commissionId,
            Authentication authentication) {

        return commissionService.getOffersForMyCommission(
                commissionId, authentication);
    }

    // 6. Client selects an artist
    @PostMapping("/{commissionId}/offers/{offerId}/select")
    public CommissionOfferResponse selectArtist(
            @PathVariable String commissionId,
            @PathVariable String offerId,
            Authentication authentication) {

        return commissionService.selectArtist(
                commissionId, offerId, authentication);
    }

    // 7. Client accepts an offer
    @PostMapping("/offers/{offerId}/accept")
    public String acceptOffer(
            @PathVariable String offerId,
            Authentication authentication) {

        return commissionService.acceptOffer(offerId, authentication);
    }

    // 8. Selected artist starts commission
    @PostMapping("/{commissionId}/start")
    public String startCommission(
            @PathVariable String commissionId,
            Authentication authentication) {

        return commissionService.startCommission(
                commissionId, authentication);
    }

    // 9. Selected artist completes commission
    @PostMapping("/{commissionId}/complete")
    public ResponseEntity<String> completeCommission(
            @PathVariable String commissionId,
            Authentication authentication) {

        return ResponseEntity.ok(
                commissionService.completeCommission(
                        commissionId, authentication));
    }

    // 10. Selected artist submits delivery
    @PostMapping("/{commissionId}/deliver")
    public ResponseEntity<String> submitDelivery(
            @PathVariable String commissionId,
            @Valid @RequestBody CommissionDeliveryRequest request,
            Authentication authentication) {

        return ResponseEntity.ok(
                commissionService.submitDelivery(
                        commissionId, request, authentication));
    }

    // 11. Client approves delivery
    @PostMapping("/{commissionId}/approve")
    public ResponseEntity<String> approveDelivery(
            @PathVariable String commissionId,
            Authentication authentication) {

        return ResponseEntity.ok(
                commissionService.approveDelivery(
                        commissionId, authentication));
    }

    // 12. Client requests revision
    @PostMapping("/{commissionId}/revision")
    public ResponseEntity<String> requestRevision(
            @PathVariable String commissionId,
            @RequestBody(required = false) String message,
            Authentication authentication) {

        return ResponseEntity.ok(
                commissionService.requestRevision(
                        commissionId, message, authentication));
    }

    // 13. Client or selected artist views delivery
    @GetMapping("/{commissionId}/delivery")
    public ResponseEntity<CommissionDeliveryResponse> getDelivery(
            @PathVariable String commissionId,
            Authentication authentication) {

        return ResponseEntity.ok(
                commissionService.getDelivery(
                        commissionId, authentication));
    }
}
