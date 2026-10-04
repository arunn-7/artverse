package com.artverse.service;

import com.artverse.dto.CommissionOfferResponse;
import com.artverse.dto.CommissionResponse;
import com.artverse.dto.CreateCommissionOfferRequest;
import com.artverse.dto.CreateCommissionRequest;
import com.artverse.entity.Commission;
import com.artverse.entity.CommissionOffer;
import com.artverse.entity.CommissionStatus;
import com.artverse.entity.CommissionOfferStatus;
import com.artverse.entity.User;
import com.artverse.repository.CommissionOfferRepository;
import com.artverse.repository.CommissionRepository;
import com.artverse.repository.UserRepository;
import com.artverse.exception.UserNotFoundException ;
import com.artverse.entity.NotificationType;
import com.artverse.service.NotificationService;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class CommissionService {

    @Autowired
    private CommissionRepository commissionRepository;

    @Autowired
    private CommissionOfferRepository commissionOfferRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationService notificationService;


    // =========================================================
    // 1. CREATE COMMISSION REQUEST
    // =========================================================

    public CommissionResponse createCommission(
            CreateCommissionRequest request,
            Authentication authentication) {

        User client = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new RuntimeException("User not found"));


        // Basic validation

        if (request.getTitle() == null ||
                request.getTitle().trim().isEmpty()) {

            throw new RuntimeException("Commission title is required");
        }

        if (request.getDescription() == null ||
                request.getDescription().trim().isEmpty()) {

            throw new RuntimeException("Commission description is required");
        }

        if (request.getRequiredDays() == null ||
                request.getRequiredDays() <= 0) {

            throw new RuntimeException(
                    "Required days must be greater than zero");
        }


        // Create commission

        Commission commission = new Commission();

        commission.setClient(client);
        commission.setTitle(request.getTitle());
        commission.setDescription(request.getDescription());
        commission.setCategory(request.getCategory());
        commission.setBudget(request.getBudget());
        commission.setRequiredDays(request.getRequiredDays());


        // Calculate deadline

        commission.setDeadline(
                LocalDateTime.now()
                        .plusDays(request.getRequiredDays())
        );


        commission.setStatus(CommissionStatus.OPEN);


        Commission savedCommission =
                commissionRepository.save(commission);


        return convertToCommissionResponse(savedCommission);
    }


    // =========================================================
    // 2. GET OPEN COMMISSIONS
    // =========================================================

    public List<CommissionResponse> getOpenCommissions() {

        return commissionRepository
                .findByStatus(CommissionStatus.OPEN)
                .stream()
                .map(this::convertToCommissionResponse)
                .toList();
    }


    // =========================================================
    // 3. GET MY COMMISSIONS
    // =========================================================

    public List<CommissionResponse> getMyCommissions(
            Authentication authentication) {

        User client = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new RuntimeException("User not found"));


        return commissionRepository
                .findByClient(client)
                .stream()
                .map(this::convertToCommissionResponse)
                .toList();
    }


    // =========================================================
    // 4. ARTIST SUBMITS AN OFFER
    // =========================================================

    public CommissionOfferResponse createOffer(
            Long commissionId,
            CreateCommissionOfferRequest request,
            Authentication authentication) {

        User artist = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new RuntimeException("User not found"));


        // Find commission

        Commission commission = commissionRepository
                .findById(commissionId)
                .orElseThrow(() ->
                        new RuntimeException("Commission not found"));


        // Commission must still be open

        if (commission.getStatus() != CommissionStatus.OPEN
                && commission.getStatus() != CommissionStatus.OFFER_RECEIVED) {

            throw new RuntimeException(
                    "This commission is no longer open for offers"
            );
        }


        // Artist cannot submit twice

        if (commissionOfferRepository
                .existsByCommissionAndArtist(commission, artist)) {

            throw new RuntimeException(
                    "You have already submitted an offer for this commission");
        }


        // Validate fee

        if (request.getProposedFee() == null ||
                request.getProposedFee().signum() <= 0) {

            throw new RuntimeException(
                    "Proposed fee must be greater than zero");
        }


        // Validate days

        if (request.getEstimatedDays() == null ||
                request.getEstimatedDays() <= 0) {

            throw new RuntimeException(
                    "Estimated days must be greater than zero");
        }


        // Artist cannot offer more days than client requested

        if (request.getEstimatedDays() >
                commission.getRequiredDays()) {

            throw new RuntimeException(
                    "Estimated days cannot exceed the required days");
        }


        // Create offer

        CommissionOffer offer = new CommissionOffer();

        offer.setCommission(commission);
        offer.setArtist(artist);
        offer.setProposedFee(request.getProposedFee());
        offer.setEstimatedDays(request.getEstimatedDays());
        offer.setMessage(request.getMessage());


        CommissionOffer savedOffer =
                commissionOfferRepository.save(offer);

        // Mark commission as having received an offer
        commission.setStatus(CommissionStatus.OFFER_RECEIVED);

        commissionRepository.save(commission);

        return convertToOfferResponse(savedOffer);
    }

    // =========================================================
    // 5. CLIENT SELECTS AN ARTIST
    // =========================================================

    public CommissionOfferResponse selectArtist(
            Long commissionId,
            Long offerId,
            Authentication authentication) {

        User client = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        // Find commission
        Commission commission = commissionRepository
                .findById(commissionId)
                .orElseThrow(() ->
                        new RuntimeException("Commission not found"));

        // Only the client who created the commission
        // can select an artist
        if (!commission.getClient().getId().equals(client.getId())) {

            throw new RuntimeException(
                    "You are not allowed to select an artist for this commission");
        }

        // Commission must be open for selection
        if (commission.getStatus() != CommissionStatus.OPEN &&
                commission.getStatus() != CommissionStatus.OFFER_RECEIVED) {

            throw new RuntimeException(
                    "Artist selection is not available for this commission");
        }

        // Find selected offer
        CommissionOffer selectedOffer = commissionOfferRepository
                .findById(offerId)
                .orElseThrow(() ->
                        new RuntimeException("Offer not found"));

        // Make sure offer belongs to this commission
        if (!selectedOffer.getCommission()
                .getId()
                .equals(commission.getId())) {

            throw new RuntimeException(
                    "This offer does not belong to this commission");
        }

        // Get all offers
        List<CommissionOffer> offers =
                commissionOfferRepository
                        .findByCommission(commission);

        // Mark selected offer
        selectedOffer.setStatus(
                CommissionOfferStatus.SELECTED
        );

        // Reject all other offers
        for (CommissionOffer offer : offers) {

            if (!offer.getId().equals(selectedOffer.getId())) {

                offer.setStatus(
                        CommissionOfferStatus.NOT_SELECTED
                );
            }
        }

        // Save all offers
        commissionOfferRepository.saveAll(offers);

        // Update commission status
        commission.setStatus(
                CommissionStatus.ARTIST_SELECTED
        );

        commissionRepository.save(commission);

        return convertToOfferResponse(selectedOffer);
    }


    // =========================================================
    // 5. CLIENT CAN VIEW OFFERS FOR THEIR OWN COMMISSION
    // =========================================================

    public List<CommissionOfferResponse> getOffersForMyCommission(
            Long commissionId,
            Authentication authentication) {

        User client = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new RuntimeException("User not found"));


        Commission commission = commissionRepository
                .findById(commissionId)
                .orElseThrow(() ->
                        new RuntimeException("Commission not found"));


        // IMPORTANT:
        // Only the client who created this commission
        // can see the offers.

        if (!commission.getClient().getId().equals(client.getId())) {

            throw new RuntimeException(
                    "You are not allowed to view these offers");
        }


        return commissionOfferRepository
                .findByCommission(commission)
                .stream()
                .map(this::convertToOfferResponse)
                .toList();
    }


    // =========================================================
    // 6. CONVERT COMMISSION -> RESPONSE DTO
    // =========================================================

    private CommissionResponse convertToCommissionResponse(
            Commission commission) {

        return new CommissionResponse(

                commission.getId(),

                commission.getClient().getId(),

                commission.getClient().getFullName(),

                commission.getTitle(),

                commission.getDescription(),

                commission.getCategory(),

                commission.getBudget(),

                commission.getRequiredDays(),

                commission.getDeadline(),

                commission.getStatus(),

                commission.getCreatedAt()
        );
    }


    // =========================================================
    // 7. CONVERT OFFER -> RESPONSE DTO
    // =========================================================

    private CommissionOfferResponse convertToOfferResponse(
            CommissionOffer offer) {

        return new CommissionOfferResponse(

                offer.getId(),

                offer.getCommission().getId(),

                offer.getArtist().getId(),

                offer.getArtist().getFullName(),

                offer.getProposedFee(),

                offer.getEstimatedDays(),

                offer.getMessage(),

                offer.getStatus(),

                offer.getCreatedAt()
        );
    }
    public String acceptOffer(
            Long offerId,
            Authentication authentication) {

        // 1. Get logged-in client
        User client = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new UserNotFoundException("User not found"));

        // 2. Find offer
        CommissionOffer selectedOffer =
                commissionOfferRepository.findById(offerId)
                        .orElseThrow(() ->
                                new RuntimeException("Offer not found"));

        // 3. Get commission
        Commission commission = selectedOffer.getCommission();

        // 4. Only commission owner can accept
        if (!commission.getClient().getId().equals(client.getId())) {
            throw new RuntimeException(
                    "Only the commission owner can accept an offer"
            );
        }

        // 5. Commission must still accept offers
        if (commission.getStatus() != CommissionStatus.OPEN &&
                commission.getStatus() != CommissionStatus.OFFER_RECEIVED) {

            throw new RuntimeException(
                    "Commission is no longer accepting offers"
            );
        }

        // 6. Select the chosen offer
        selectedOffer.setStatus(CommissionOfferStatus.SELECTED);

        // 7. Get all offers
        List<CommissionOffer> offers =
                commissionOfferRepository.findByCommission(commission);

        // 8. Notify selected and other artists
        for (CommissionOffer offer : offers) {

            if (offer.getId().equals(selectedOffer.getId())) {

                // Selected artist notification
                notificationService.createNotification(
                        offer.getArtist(),
                        client,
                        "Commission Offer Accepted",
                        "Your proposal for commission \""
                                + commission.getTitle()
                                + "\" has been accepted by the client. "
                                + "Fee: ₹"
                                + offer.getProposedFee()
                                + ", Estimated time: "
                                + offer.getEstimatedDays()
                                + " days.",
                        NotificationType.COMMISSION
                );

            } else {

                // Other artists
                offer.setStatus(CommissionOfferStatus.NOT_SELECTED);

                notificationService.createNotification(
                        offer.getArtist(),
                        client,
                        "Commission Proposal Update",
                        "Your proposal for commission \""
                                + commission.getTitle()
                                + "\" was not selected by the client.",
                        NotificationType.COMMISSION
                );
            }
        }

        // 9. Save all offers
        commissionOfferRepository.saveAll(offers);

        // 10. Update commission status
        commission.setStatus(CommissionStatus.ARTIST_SELECTED);

        commissionRepository.save(commission);

        return "Commission offer accepted successfully";
    }
}