
package com.artverse.service;

import com.artverse.dto.*;
import com.artverse.entity.*;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutionException;

@Service
public class FirestoreCommissionService {

    private final Firestore firestore;
    private final FirestoreUserService userService;
    private final NotificationService notificationService;

    private static final String COMMISSIONS = "commissions";
    private static final String OFFERS = "commissionOffers";
    private static final String DELIVERIES = "commissionDeliveries";

    public FirestoreCommissionService(
            Firestore firestore,
            FirestoreUserService userService,
            NotificationService notificationService) {
        this.firestore = firestore;
        this.userService = userService;
        this.notificationService = notificationService;
    }

    // ---------------------------------------------------------
    // Common helpers
    // ---------------------------------------------------------

    private User currentUser(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Authentication required");
        }

        return userService.getUserByEmail(
                authentication.getName().trim().toLowerCase(Locale.ROOT));
    }

    private DocumentSnapshot getDocument(
            String collection, String id, String message) {
        try {
            DocumentSnapshot document = firestore.collection(collection)
                    .document(id)
                    .get()
                    .get();

            if (!document.exists()) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND, message);
            }

            return document;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Firestore operation interrupted");
        } catch (ExecutionException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Firestore operation failed");
        }
    }

    private QuerySnapshot query(
            com.google.cloud.firestore.Query query) {
        try {
            return query.get().get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Firestore query interrupted");
        } catch (ExecutionException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Firestore query failed");
        }
    }

    private void save(String collection, String id, Map<String, Object> data) {
        try {
            firestore.collection(collection).document(id)
                    .set(data).get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Firestore save interrupted");
        } catch (ExecutionException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to save Firestore document");
        }
    }

    private void updateField(
            String collection, String id, String field, Object value) {
        try {
            firestore.collection(collection).document(id)
                    .update(field, value).get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Firestore update interrupted");
        } catch (ExecutionException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to update Firestore document");
        }
    }

    private LocalDateTime parseTime(String value) {
        return value == null ? null : LocalDateTime.parse(value);
    }

    private User findUserByUid(String uid) {
        QuerySnapshot result = query(
                firestore.collection("users")
                        .whereEqualTo("userUid", uid)
                        .limit(1));

        if (result.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "User profile not found");
        }

        String email = result.getDocuments().get(0).getString("email");
        if (email == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "User email not found");
        }

        return userService.getUserByEmail(email);
    }

    private FirestoreCommission readCommission(DocumentSnapshot d) {
        FirestoreCommission c = new FirestoreCommission();
        c.setId(d.getId());
        c.setClientUid(d.getString("clientUid"));
        c.setClientEmail(d.getString("clientEmail"));
        c.setClientName(d.getString("clientName"));
        c.setTitle(d.getString("title"));
        c.setDescription(d.getString("description"));
        c.setCategory(d.getString("category"));

        String budget = d.getString("budget");
        if (budget != null) {
            c.setBudget(new BigDecimal(budget));
        }

        Long days = d.getLong("requiredDays");
        if (days != null) {
            c.setRequiredDays(days.intValue());
        }

        c.setDeadline(parseTime(d.getString("deadline")));

        String status = d.getString("status");
        if (status != null) {
            c.setStatus(CommissionStatus.valueOf(status));
        }

        String payment = d.getString("paymentStatus");
        if (payment != null) {
            c.setPaymentStatus(CommissionPaymentStatus.valueOf(payment));
        }

        c.setRazorpayOrderId(d.getString("razorpayOrderId"));
        c.setRazorpayPaymentId(d.getString("razorpayPaymentId"));
        c.setCreatedAt(parseTime(d.getString("createdAt")));
        c.setUpdatedAt(parseTime(d.getString("updatedAt")));
        return c;
    }

    private Map<String, Object> commissionData(FirestoreCommission c) {
        Map<String, Object> data = new HashMap<>();
        data.put("clientUid", c.getClientUid());
        data.put("clientEmail", c.getClientEmail());
        data.put("clientName", c.getClientName());
        data.put("title", c.getTitle());
        data.put("description", c.getDescription());
        data.put("category", c.getCategory());
        data.put("budget",
                c.getBudget() == null ? null : c.getBudget().toPlainString());
        data.put("requiredDays", c.getRequiredDays());
        data.put("deadline",
                c.getDeadline() == null ? null : c.getDeadline().toString());
        data.put("status", c.getStatus().name());
        data.put("paymentStatus", c.getPaymentStatus().name());
        data.put("razorpayOrderId", c.getRazorpayOrderId());
        data.put("razorpayPaymentId", c.getRazorpayPaymentId());
        data.put("createdAt",
                c.getCreatedAt() == null ? null : c.getCreatedAt().toString());
        data.put("updatedAt",
                c.getUpdatedAt() == null ? null : c.getUpdatedAt().toString());
        data.values().removeIf(Objects::isNull);
        return data;
    }

    private FirestoreCommissionOffer readOffer(DocumentSnapshot d) {
        FirestoreCommissionOffer o = new FirestoreCommissionOffer();
        o.setId(d.getId());
        o.setCommissionId(d.getString("commissionId"));
        o.setArtistUid(d.getString("artistUid"));
        o.setArtistName(d.getString("artistName"));

        String fee = d.getString("proposedFee");
        if (fee != null) {
            o.setProposedFee(new BigDecimal(fee));
        }

        Long days = d.getLong("estimatedDays");
        if (days != null) {
            o.setEstimatedDays(days.intValue());
        }

        o.setMessage(d.getString("message"));

        String status = d.getString("status");
        if (status != null) {
            o.setStatus(CommissionOfferStatus.valueOf(status));
        }

        o.setCreatedAt(parseTime(d.getString("createdAt")));
        return o;
    }

    private Map<String, Object> offerData(FirestoreCommissionOffer o) {
        Map<String, Object> data = new HashMap<>();
        data.put("commissionId", o.getCommissionId());
        data.put("artistUid", o.getArtistUid());
        data.put("artistName", o.getArtistName());
        data.put("proposedFee",
                o.getProposedFee() == null
                        ? null : o.getProposedFee().toPlainString());
        data.put("estimatedDays", o.getEstimatedDays());
        data.put("message", o.getMessage());
        data.put("status", o.getStatus().name());
        data.put("createdAt",
                o.getCreatedAt() == null ? null : o.getCreatedAt().toString());
        data.values().removeIf(Objects::isNull);
        return data;
    }

    private FirestoreCommissionDelivery readDelivery(DocumentSnapshot d) {
        FirestoreCommissionDelivery delivery =
                new FirestoreCommissionDelivery();
        delivery.setId(d.getId());
        delivery.setCommissionId(d.getString("commissionId"));
        delivery.setArtistUid(d.getString("artistUid"));
        delivery.setArtistName(d.getString("artistName"));
        delivery.setFileUrl(d.getString("fileUrl"));
        delivery.setMessage(d.getString("message"));
        delivery.setSubmittedAt(parseTime(d.getString("submittedAt")));
        return delivery;
    }

    private Map<String, Object> deliveryData(
            FirestoreCommissionDelivery delivery) {
        Map<String, Object> data = new HashMap<>();
        data.put("commissionId", delivery.getCommissionId());
        data.put("artistUid", delivery.getArtistUid());
        data.put("artistName", delivery.getArtistName());
        data.put("fileUrl", delivery.getFileUrl());
        data.put("message", delivery.getMessage());
        data.put("submittedAt",
                delivery.getSubmittedAt() == null
                        ? null : delivery.getSubmittedAt().toString());
        data.values().removeIf(Objects::isNull);
        return data;
    }

    private CommissionResponse toCommissionResponse(
            FirestoreCommission c) {
        return new CommissionResponse(
                c.getId(), c.getClientUid(), c.getClientName(),
                c.getTitle(), c.getDescription(), c.getCategory(),
                c.getBudget(), c.getRequiredDays(), c.getDeadline(),
                c.getStatus(), c.getCreatedAt());
    }

    private CommissionOfferResponse toOfferResponse(
            FirestoreCommissionOffer o) {
        return new CommissionOfferResponse(
                o.getId(), o.getCommissionId(), o.getArtistUid(),
                o.getArtistName(), o.getProposedFee(),
                o.getEstimatedDays(), o.getMessage(),
                o.getStatus(), o.getCreatedAt());
    }

    private FirestoreCommission getCommission(String id) {
        return readCommission(getDocument(
                COMMISSIONS, id, "Commission not found"));
    }

    private List<FirestoreCommissionOffer> getOffers(String commissionId) {
        QuerySnapshot result = query(
                firestore.collection(OFFERS)
                        .whereEqualTo("commissionId", commissionId));

        List<FirestoreCommissionOffer> offers = new ArrayList<>();
        for (QueryDocumentSnapshot d : result.getDocuments()) {
            offers.add(readOffer(d));
        }
        return offers;
    }

    private FirestoreCommissionOffer selectedOffer(String commissionId) {
        return getOffers(commissionId).stream()
                .filter(o -> o.getStatus() == CommissionOfferStatus.SELECTED)
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Selected artist not found"));
    }

    private void saveCommission(FirestoreCommission c) {
        c.setUpdatedAt(LocalDateTime.now());
        save(COMMISSIONS, c.getId(), commissionData(c));
    }

    private void requireCommissionOwner(
            FirestoreCommission commission, User user) {
        if (user.getUserUid() == null
                || !user.getUserUid().equals(commission.getClientUid())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only the commission owner can perform this action");
        }
    }

    private void requireSelectedArtist(
            FirestoreCommission commission, User artist) {
        FirestoreCommissionOffer selected = selectedOffer(commission.getId());
        if (artist.getUserUid() == null
                || !artist.getUserUid().equals(selected.getArtistUid())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only the selected artist can perform this action");
        }
    }

    // ---------------------------------------------------------
    // 1. Create commission
    // ---------------------------------------------------------

    public CommissionResponse createCommission(
            CreateCommissionRequest request,
            Authentication authentication) {

        User client = currentUser(authentication);

        if (request.getTitle() == null || request.getTitle().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Commission title is required");
        }

        if (request.getDescription() == null
                || request.getDescription().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Commission description is required");
        }

        if (request.getRequiredDays() == null
                || request.getRequiredDays() <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Required days must be greater than zero");
        }

        if (client.getUserUid() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "User profile has no Firebase UID");
        }

        LocalDateTime currentTime = LocalDateTime.now();

        FirestoreCommission c = new FirestoreCommission();
        c.setClientUid(client.getUserUid());
        c.setClientEmail(client.getEmail());
        c.setClientName(client.getFullName());
        c.setTitle(request.getTitle().trim());
        c.setDescription(request.getDescription().trim());
        c.setCategory(request.getCategory());
        c.setBudget(request.getBudget());
        c.setRequiredDays(request.getRequiredDays());
        c.setDeadline(currentTime.plusDays(request.getRequiredDays()));
        c.setStatus(CommissionStatus.OPEN);
        c.setPaymentStatus(CommissionPaymentStatus.PENDING);
        c.setCreatedAt(currentTime);
        c.setUpdatedAt(currentTime);

        String id = firestore.collection(COMMISSIONS).document().getId();
        c.setId(id);
        save(COMMISSIONS, id, commissionData(c));

        QuerySnapshot artists = query(
                firestore.collection("users")
                        .whereEqualTo("accountType", "ARTIST"));

        for (QueryDocumentSnapshot d : artists.getDocuments()) {
            String email = d.getString("email");
            if (email == null || email.equalsIgnoreCase(client.getEmail())) {
                continue;
            }

            try {
                User artist = userService.getUserByEmail(email);
                notificationService.createNotification(
                        artist, client,
                        "New Commission Available",
                        "A new commission \"" + c.getTitle()
                                + "\" has been posted. Submit a proposal if interested.",
                        NotificationType.COMMISSION);
            } catch (RuntimeException ignored) {
                // One invalid artist profile must not undo commission creation.
            }
        }

        return toCommissionResponse(c);
    }

    // ---------------------------------------------------------
    // 2. Open commissions
    // ---------------------------------------------------------

    public List<CommissionResponse> getOpenCommissions() {
        QuerySnapshot result = query(
                firestore.collection(COMMISSIONS)
                        .whereEqualTo("status", CommissionStatus.OPEN.name()));

        List<CommissionResponse> responses = new ArrayList<>();
        for (QueryDocumentSnapshot d : result.getDocuments()) {
            responses.add(toCommissionResponse(readCommission(d)));
        }
        return responses;
    }

    // ---------------------------------------------------------
    // 3. My commissions
    // ---------------------------------------------------------

    public List<CommissionResponse> getMyCommissions(
            Authentication authentication) {

        User client = currentUser(authentication);

        QuerySnapshot result = query(
                firestore.collection(COMMISSIONS)
                        .whereEqualTo("clientUid", client.getUserUid()));

        List<CommissionResponse> responses = new ArrayList<>();
        for (QueryDocumentSnapshot d : result.getDocuments()) {
            responses.add(toCommissionResponse(readCommission(d)));
        }
        return responses;
    }

    // ---------------------------------------------------------
    // 4. Create offer
    // ---------------------------------------------------------

    public CommissionOfferResponse createOffer(
            String commissionId,
            CreateCommissionOfferRequest request,
            Authentication authentication) {

        User artist = currentUser(authentication);
        FirestoreCommission c = getCommission(commissionId);

        if (!"ARTIST".equalsIgnoreCase(artist.getAccountType())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Only artists can submit offers");
        }

        if (c.getStatus() != CommissionStatus.OPEN
                && c.getStatus() != CommissionStatus.OFFER_RECEIVED) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "This commission is no longer accepting offers");
        }

        if (request.getProposedFee() == null
                || request.getProposedFee().signum() <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Proposed fee must be greater than zero");
        }

        if (request.getEstimatedDays() == null
                || request.getEstimatedDays() <= 0
                || request.getEstimatedDays() > c.getRequiredDays()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Estimated days must be positive and cannot exceed required days");
        }

        QuerySnapshot duplicate = query(
                firestore.collection(OFFERS)
                        .whereEqualTo("commissionId", commissionId)
                        .whereEqualTo("artistUid", artist.getUserUid())
                        .limit(1));

        if (!duplicate.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "You have already submitted an offer");
        }

        FirestoreCommissionOffer offer = new FirestoreCommissionOffer();
        offer.setId(firestore.collection(OFFERS).document().getId());
        offer.setCommissionId(commissionId);
        offer.setArtistUid(artist.getUserUid());
        offer.setArtistName(artist.getFullName());
        offer.setProposedFee(request.getProposedFee());
        offer.setEstimatedDays(request.getEstimatedDays());
        offer.setMessage(request.getMessage());
        offer.setStatus(CommissionOfferStatus.OFFERED);
        offer.setCreatedAt(LocalDateTime.now());

        save(OFFERS, offer.getId(), offerData(offer));

        c.setStatus(CommissionStatus.OFFER_RECEIVED);
        saveCommission(c);

        User client = userService.getUserByEmail(c.getClientEmail());
        notificationService.createNotification(
                client, artist,
                "New Commission Proposal",
                artist.getFullName() + " submitted a proposal for \""
                        + c.getTitle() + "\". Proposed fee: ₹"
                        + offer.getProposedFee() + ", estimated time: "
                        + offer.getEstimatedDays() + " days.",
                NotificationType.COMMISSION);

        return toOfferResponse(offer);
    }

    // ---------------------------------------------------------
    // 5. Client views offers
    // ---------------------------------------------------------

    public List<CommissionOfferResponse> getOffersForMyCommission(
            String commissionId, Authentication authentication) {

        User client = currentUser(authentication);
        FirestoreCommission c = getCommission(commissionId);
        requireCommissionOwner(c, client);

        List<CommissionOfferResponse> responses = new ArrayList<>();
        for (FirestoreCommissionOffer offer : getOffers(commissionId)) {
            responses.add(toOfferResponse(offer));
        }
        return responses;
    }

    // ---------------------------------------------------------
    // 6. Select artist
    // ---------------------------------------------------------

    public CommissionOfferResponse selectArtist(
            String commissionId,
            String offerId,
            Authentication authentication) {

        User client = currentUser(authentication);
        FirestoreCommission c = getCommission(commissionId);
        requireCommissionOwner(c, client);

        if (c.getStatus() != CommissionStatus.OPEN
                && c.getStatus() != CommissionStatus.OFFER_RECEIVED) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Artist selection is not available");
        }

        FirestoreCommissionOffer selected = readOffer(
                getDocument(OFFERS, offerId, "Offer not found"));

        if (!commissionId.equals(selected.getCommissionId())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Offer does not belong to this commission");
        }

        List<FirestoreCommissionOffer> offers = getOffers(commissionId);

        for (FirestoreCommissionOffer offer : offers) {
            offer.setStatus(offer.getId().equals(offerId)
                    ? CommissionOfferStatus.SELECTED
                    : CommissionOfferStatus.NOT_SELECTED);
            save(OFFERS, offer.getId(), offerData(offer));
        }

        c.setStatus(CommissionStatus.ARTIST_SELECTED);
        saveCommission(c);

        return toOfferResponse(selected);
    }

    // ---------------------------------------------------------
    // 7. Accept offer and notify artists
    // ---------------------------------------------------------

    public String acceptOffer(
            String offerId, Authentication authentication) {

        User client = currentUser(authentication);

        FirestoreCommissionOffer selected = readOffer(
                getDocument(OFFERS, offerId, "Offer not found"));

        FirestoreCommission c = getCommission(selected.getCommissionId());
        requireCommissionOwner(c, client);

        if (c.getStatus() != CommissionStatus.OPEN
                && c.getStatus() != CommissionStatus.OFFER_RECEIVED) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Commission is no longer accepting offers");
        }

        List<FirestoreCommissionOffer> offers = getOffers(c.getId());

        for (FirestoreCommissionOffer offer : offers) {
            boolean chosen = offer.getId().equals(offerId);
            offer.setStatus(chosen
                    ? CommissionOfferStatus.SELECTED
                    : CommissionOfferStatus.NOT_SELECTED);
            save(OFFERS, offer.getId(), offerData(offer));

            User artist = findUserByUid(offer.getArtistUid());
            notificationService.createNotification(
                    artist, client,
                    chosen ? "Commission Offer Accepted"
                            : "Commission Proposal Not Selected",
                    chosen
                            ? "Your proposal for \"" + c.getTitle()
                            + "\" has been accepted."
                            : "Your proposal for \"" + c.getTitle()
                            + "\" was not selected.",
                    NotificationType.COMMISSION);
        }

        c.setStatus(CommissionStatus.ARTIST_SELECTED);
        saveCommission(c);

        return "Commission offer accepted successfully";
    }

    // ---------------------------------------------------------
    // 8. Start commission
    // ---------------------------------------------------------

    public String startCommission(
            String commissionId, Authentication authentication) {

        User artist = currentUser(authentication);
        FirestoreCommission c = getCommission(commissionId);

        if (c.getStatus() != CommissionStatus.ARTIST_SELECTED) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Commission is not ready to be started");
        }

        if (c.getPaymentStatus() != CommissionPaymentStatus.PAID) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Commission payment has not been completed");
        }

        requireSelectedArtist(c, artist);

        c.setStatus(CommissionStatus.IN_PROGRESS);
        saveCommission(c);

        User client = userService.getUserByEmail(c.getClientEmail());
        notificationService.createNotification(
                client, artist,
                "Commission Started",
                artist.getFullName() + " started working on \""
                        + c.getTitle() + "\".",
                NotificationType.ANNOUNCEMENT);

        return "Commission started successfully";
    }

    // ---------------------------------------------------------
    // 9. Complete commission directly
    // ---------------------------------------------------------

    public String completeCommission(
            String commissionId, Authentication authentication) {

        User artist = currentUser(authentication);
        FirestoreCommission c = getCommission(commissionId);

        if (c.getStatus() != CommissionStatus.IN_PROGRESS) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Commission is not currently in progress");
        }

        requireSelectedArtist(c, artist);

        c.setStatus(CommissionStatus.COMPLETED);
        saveCommission(c);

        User client = userService.getUserByEmail(c.getClientEmail());
        notificationService.createNotification(
                client, artist,
                "Commission Completed",
                artist.getFullName() + " completed \"" + c.getTitle() + "\".",
                NotificationType.COMMISSION);

        return "Commission completed successfully";
    }

    // ---------------------------------------------------------
    // 10. Submit delivery
    // ---------------------------------------------------------

    public String submitDelivery(
            String commissionId,
            CommissionDeliveryRequest request,
            Authentication authentication) {

        User artist = currentUser(authentication);
        FirestoreCommission c = getCommission(commissionId);

        if (c.getStatus() != CommissionStatus.IN_PROGRESS) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Commission is not currently in progress");
        }

        requireSelectedArtist(c, artist);

        if (request.getFileUrl() == null || request.getFileUrl().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Delivery file URL is required");
        }

        FirestoreCommissionDelivery delivery =
                new FirestoreCommissionDelivery();
        delivery.setId(commissionId);
        delivery.setCommissionId(commissionId);
        delivery.setArtistUid(artist.getUserUid());
        delivery.setArtistName(artist.getFullName());
        delivery.setFileUrl(request.getFileUrl());
        delivery.setMessage(request.getMessage());
        delivery.setSubmittedAt(LocalDateTime.now());

        save(DELIVERIES, commissionId, deliveryData(delivery));

        c.setStatus(CommissionStatus.DELIVERED);
        saveCommission(c);

        User client = userService.getUserByEmail(c.getClientEmail());
        notificationService.createNotification(
                client, artist,
                "Commission Delivered",
                artist.getFullName() + " delivered \"" + c.getTitle()
                        + "\". Please review the work.",
                NotificationType.COMMISSION);

        return "Commission delivered successfully";
    }

    // ---------------------------------------------------------
    // 11. Client approves delivery
    // ---------------------------------------------------------

    public String approveDelivery(
            String commissionId, Authentication authentication) {

        User client = currentUser(authentication);
        FirestoreCommission c = getCommission(commissionId);
        requireCommissionOwner(c, client);

        if (c.getStatus() != CommissionStatus.DELIVERED) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Commission is not awaiting approval");
        }

        FirestoreCommissionDelivery delivery = readDelivery(
                getDocument(DELIVERIES, commissionId,
                        "Commission delivery not found"));

        c.setStatus(CommissionStatus.COMPLETED);
        saveCommission(c);

        User artist = findUserByUid(delivery.getArtistUid());
        notificationService.createNotification(
                artist, client,
                "Commission Approved",
                "Your work for \"" + c.getTitle()
                        + "\" has been approved.",
                NotificationType.COMMISSION);

        return "Commission approved successfully";
    }

    // ---------------------------------------------------------
    // 12. Client requests revision
    // ---------------------------------------------------------

    public String requestRevision(
            String commissionId,
            String message,
            Authentication authentication) {

        User client = currentUser(authentication);
        FirestoreCommission c = getCommission(commissionId);
        requireCommissionOwner(c, client);

        if (c.getStatus() != CommissionStatus.DELIVERED) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Commission is not awaiting revision");
        }

        FirestoreCommissionDelivery delivery = readDelivery(
                getDocument(DELIVERIES, commissionId,
                        "Commission delivery not found"));

        c.setStatus(CommissionStatus.IN_PROGRESS);
        saveCommission(c);

        String notificationMessage =
                message == null || message.isBlank()
                        ? "The client requested a revision for your commission."
                        : "The client requested a revision: " + message;

        User artist = findUserByUid(delivery.getArtistUid());
        notificationService.createNotification(
                artist, client,
                "Revision Requested",
                notificationMessage,
                NotificationType.COMMISSION);

        return "Revision requested successfully";
    }

    // ---------------------------------------------------------
    // 13. View delivery privately
    // ---------------------------------------------------------

    public CommissionDeliveryResponse getDelivery(
            String commissionId, Authentication authentication) {

        User user = currentUser(authentication);
        FirestoreCommission c = getCommission(commissionId);

        boolean isClient = user.getUserUid() != null
                && user.getUserUid().equals(c.getClientUid());

        List<FirestoreCommissionOffer> offers = getOffers(commissionId);
        boolean isSelectedArtist = offers.stream()
                .anyMatch(o ->
                        o.getStatus() == CommissionOfferStatus.SELECTED
                                && user.getUserUid() != null
                                && user.getUserUid().equals(o.getArtistUid()));

        if (!isClient && !isSelectedArtist) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You are not authorized to view this delivery");
        }

        FirestoreCommissionDelivery delivery = readDelivery(
                getDocument(DELIVERIES, commissionId,
                        "Commission delivery not found"));

        return new CommissionDeliveryResponse(
                delivery.getId(),
                delivery.getCommissionId(),
                delivery.getArtistName(),
                delivery.getFileUrl(),
                delivery.getMessage(),
                delivery.getSubmittedAt());
    }
}
