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
import com.artverse.entity.CommissionDelivery;
import com.artverse.repository.CommissionDeliveryRepository;
import com.artverse.dto.CommissionDeliveryRequest;
import com.artverse.dto.CommissionDeliveryResponse;
import com.artverse.entity.CommissionPaymentStatus;


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

    @Autowired
    private CommissionDeliveryRepository commissionDeliveryRepository;


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

        System.out.println("Commission created by: " + client.getEmail());

        List<User> artists =
                userRepository.findByAccountType("ARTIST");

        System.out.println("Artists found: " + artists.size());

        for (User artist : artists) {

            System.out.println(
                    "Sending commission notification to: "
                            + artist.getEmail()
            );

            notificationService.createNotification(
                    artist,
                    client,
                    "New Commission Available",
                    "A new commission \""
                            + savedCommission.getTitle()
                            + "\" has been posted. You can submit your proposal if interested.",
                    NotificationType.COMMISSION
            );
        }

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
        if (request.getProposedFee() == null
                || request.getProposedFee().signum() <= 0) {

            throw new RuntimeException(
                    "Proposed fee must be greater than zero");
        }

        // Validate days
        if (request.getEstimatedDays() == null
                || request.getEstimatedDays() <= 0) {

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

        // Notify the client about the new proposal
        notificationService.createNotification(
                commission.getClient(),          // receiver = client
                artist,                          // sender = artist
                "New Commission Proposal",
                artist.getFullName()
                        + " has submitted a proposal for your commission \""
                        + commission.getTitle()
                        + "\". Proposed fee: ₹"
                        + savedOffer.getProposedFee()
                        + ", Estimated time: "
                        + savedOffer.getEstimatedDays()
                        + " days.",
                NotificationType.COMMISSION
        );

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

        // 2. Find selected offer
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

        // 5. Commission must still be open
        if (commission.getStatus() != CommissionStatus.OPEN &&
                commission.getStatus() != CommissionStatus.OFFER_RECEIVED) {

            throw new RuntimeException(
                    "Commission is no longer accepting offers"
            );
        }

        // 6. Select this offer
        selectedOffer.setStatus(CommissionOfferStatus.SELECTED);

        // 7. Get all offers
        List<CommissionOffer> offers =
                commissionOfferRepository.findByCommission(commission);

        // 8. Notify selected artist and other artists
        for (CommissionOffer offer : offers) {

            if (offer.getId().equals(selectedOffer.getId())) {

                // Selected artist notification
                notificationService.createNotification(
                        selectedOffer.getArtist(),   // receiver = selected artist
                        client,                      // sender = client
                        "Commission Offer Accepted",
                        "Your proposal for commission \""
                                + commission.getTitle()
                                + "\" has been accepted by the client. "
                                + "Fee: ₹"
                                + selectedOffer.getProposedFee()
                                + ", Estimated time: "
                                + selectedOffer.getEstimatedDays()
                                + " days.",
                        NotificationType.COMMISSION
                );

            } else {

                // Other artists are not selected
                offer.setStatus(CommissionOfferStatus.NOT_SELECTED);

                notificationService.createNotification(
                        offer.getArtist(),
                        client,
                        "Commission Proposal Not Selected",
                        "Your proposal for commission \""
                                + commission.getTitle()
                                + "\" was not selected by the client.",
                        NotificationType.COMMISSION
                );
            }
        }

        // 9. Save all offer status changes
        commissionOfferRepository.saveAll(offers);

        // 10. Update commission status
        commission.setStatus(CommissionStatus.ARTIST_SELECTED);

        commissionRepository.save(commission);

        return "Commission offer accepted successfully";
    }
    public String startCommission(
            Long commissionId,
            Authentication authentication) {

        User artist = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new UserNotFoundException("User not found"));

        Commission commission = commissionRepository.findById(commissionId)
                .orElseThrow(() ->
                        new RuntimeException("Commission not found"));

        // Commission must have an artist selected
        if (commission.getStatus() != CommissionStatus.ARTIST_SELECTED) {
            throw new RuntimeException(
                    "Commission is not ready to be started"
            );
        }

        if (commission.getPaymentStatus()
                != CommissionPaymentStatus.PAID) {

            throw new RuntimeException(
                    "Commission payment has not been completed"
            );
        }

        // Find the selected offer
        List<CommissionOffer> offers =
                commissionOfferRepository.findByCommission(commission);

        CommissionOffer selectedOffer = offers.stream()
                .filter(offer ->
                        offer.getStatus() == CommissionOfferStatus.SELECTED)
                .findFirst()
                .orElseThrow(() ->
                        new RuntimeException("Selected artist not found"));

        // Only selected artist can start
        if (!selectedOffer.getArtist().getId().equals(artist.getId())) {
            throw new RuntimeException(
                    "Only the selected artist can start this commission"
            );
        }

        // Change status
        commission.setStatus(CommissionStatus.IN_PROGRESS);

        commissionRepository.save(commission);

        // Notify client
        notificationService.createNotification(
                commission.getClient(),
                artist,
                "Commission Started",
                artist.getFullName()
                        + " has started working on your commission \""
                        + commission.getTitle()
                        + "\".",
                NotificationType.ANNOUNCEMENT
        );

        return "Commission started successfully";
    }
    public String completeCommission(
            Long commissionId,
            Authentication authentication) {

        User artist = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new UserNotFoundException("User not found"));

        Commission commission = commissionRepository.findById(commissionId)
                .orElseThrow(() ->
                        new RuntimeException("Commission not found"));

        // Commission must be in progress
        if (commission.getStatus() != CommissionStatus.IN_PROGRESS) {
            throw new RuntimeException(
                    "Commission is not currently in progress"
            );
        }

        // Find the selected offer
        List<CommissionOffer> offers =
                commissionOfferRepository.findByCommission(commission);

        CommissionOffer selectedOffer = offers.stream()
                .filter(offer ->
                        offer.getStatus() == CommissionOfferStatus.SELECTED)
                .findFirst()
                .orElseThrow(() ->
                        new RuntimeException("Selected artist not found"));

        // Only selected artist can complete
        if (!selectedOffer.getArtist().getId().equals(artist.getId())) {
            throw new RuntimeException(
                    "Only the selected artist can complete this commission"
            );
        }

        // Change status
        commission.setStatus(CommissionStatus.COMPLETED);

        commissionRepository.save(commission);

        // Notify client
        notificationService.createNotification(
                commission.getClient(),
                artist,
                "Commission Completed",
                artist.getFullName()
                        + " has completed your commission \""
                        + commission.getTitle()
                        + "\".",
                NotificationType.COMMISSION
        );

        return "Commission completed successfully";
    }
    public String submitDelivery(
            Long commissionId,
            CommissionDeliveryRequest request,
            Authentication authentication) {

        User artist = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new UserNotFoundException("User not found"));

        Commission commission = commissionRepository.findById(commissionId)
                .orElseThrow(() ->
                        new RuntimeException("Commission not found"));

        if (commission.getStatus() != CommissionStatus.IN_PROGRESS) {
            throw new RuntimeException(
                    "Commission is not currently in progress"
            );
        }

        List<CommissionOffer> offers =
                commissionOfferRepository.findByCommission(commission);

        CommissionOffer selectedOffer = offers.stream()
                .filter(offer ->
                        offer.getStatus() == CommissionOfferStatus.SELECTED)
                .findFirst()
                .orElseThrow(() ->
                        new RuntimeException("Selected artist not found"));

        if (!selectedOffer.getArtist().getId().equals(artist.getId())) {
            throw new RuntimeException(
                    "Only the selected artist can submit the delivery"
            );
        }

        CommissionDelivery delivery = new CommissionDelivery();

        delivery.setCommission(commission);
        delivery.setArtist(artist);
        delivery.setFileUrl(request.getFileUrl());
        delivery.setMessage(request.getMessage());

        commissionDeliveryRepository.save(delivery);

        commission.setStatus(CommissionStatus.DELIVERED);
        commissionRepository.save(commission);

        notificationService.createNotification(
                commission.getClient(),
                artist,
                "Commission Delivered",
                artist.getFullName()
                        + " has delivered your commission \""
                        + commission.getTitle()
                        + "\". Please review and approve the work.",
                NotificationType.COMMISSION
        );

        return "Commission delivered successfully";
    }
    public String approveDelivery(
            Long commissionId,
            Authentication authentication) {

        User client = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new UserNotFoundException("User not found"));

        Commission commission = commissionRepository.findById(commissionId)
                .orElseThrow(() ->
                        new RuntimeException("Commission not found"));

        // Commission must be delivered
        if (commission.getStatus() != CommissionStatus.DELIVERED) {
            throw new RuntimeException(
                    "Commission is not awaiting client approval"
            );
        }

        // Only commission owner can approve
        if (!commission.getClient().getId().equals(client.getId())) {
            throw new RuntimeException(
                    "Only the commission owner can approve the delivery"
            );
        }

        // Make sure delivery exists
        CommissionDelivery delivery =
                commissionDeliveryRepository.findByCommission(commission)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Commission delivery not found"
                                ));

        // Complete commission
        commission.setStatus(CommissionStatus.COMPLETED);

        commissionRepository.save(commission);

        // Notify artist
        notificationService.createNotification(
                delivery.getArtist(),
                client,
                "Commission Approved",
                "The client has approved your completed work for commission \""
                        + commission.getTitle()
                        + "\".",
                NotificationType.COMMISSION
        );

        return "Commission approved successfully";
    }
    public String requestRevision(
            Long commissionId,
            String message,
            Authentication authentication) {

        User client = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new UserNotFoundException("User not found"));

        Commission commission = commissionRepository.findById(commissionId)
                .orElseThrow(() ->
                        new RuntimeException("Commission not found"));

        if (commission.getStatus() != CommissionStatus.DELIVERED) {
            throw new RuntimeException(
                    "Commission is not awaiting revision"
            );
        }

        // Only commission owner can request revision
        if (!commission.getClient().getId().equals(client.getId())) {
            throw new RuntimeException(
                    "Only the commission owner can request a revision"
            );
        }

        CommissionDelivery delivery =
                commissionDeliveryRepository.findByCommission(commission)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Commission delivery not found"
                                ));

        commission.setStatus(CommissionStatus.IN_PROGRESS);
        commissionRepository.save(commission);

        String revisionMessage =
                (message == null || message.trim().isEmpty())
                        ? "The client has requested a revision for your commission."
                        : "The client requested a revision: " + message;

        notificationService.createNotification(
                delivery.getArtist(),
                client,
                "Revision Requested",
                revisionMessage,
                NotificationType.COMMISSION
        );

        return "Revision requested successfully";
    }
    public CommissionDeliveryResponse getDelivery(
            Long commissionId,
            Authentication authentication) {

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new UserNotFoundException("User not found"));

        Commission commission = commissionRepository.findById(commissionId)
                .orElseThrow(() ->
                        new RuntimeException("Commission not found"));

        // Only the client or selected artist can view the delivery
        boolean isClient =
                commission.getClient().getId().equals(user.getId());

        List<CommissionOffer> offers =
                commissionOfferRepository.findByCommission(commission);

        boolean isSelectedArtist = offers.stream()
                .anyMatch(offer ->
                        offer.getStatus() == CommissionOfferStatus.SELECTED
                                && offer.getArtist().getId().equals(user.getId()));

        if (!isClient && !isSelectedArtist) {
            throw new RuntimeException(
                    "You are not authorized to view this delivery"
            );
        }

        CommissionDelivery delivery =
                commissionDeliveryRepository.findByCommission(commission)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Commission delivery not found"
                                ));

        return new CommissionDeliveryResponse(
                delivery.getId(),
                commission.getId(),
                delivery.getArtist().getFullName(),
                delivery.getFileUrl(),
                delivery.getMessage(),
                delivery.getSubmittedAt()
        );
    }

}