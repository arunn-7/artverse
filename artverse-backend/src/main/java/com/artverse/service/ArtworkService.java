package com.artverse.service;

import com.artverse.dto.ArtworkFeedResponse;
import com.artverse.dto.ArtworkRequest;
import com.artverse.dto.ArtworkResponse;
import com.artverse.dto.MarketplaceArtworkResponse;
import com.artverse.dto.SellArtworkRequest;
import com.artverse.entity.User;
import com.artverse.model.FirestoreArtwork;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class ArtworkService {



    @Autowired
    private FirestoreUserService firestoreUserService;

    @Autowired
    private FirestoreArtworkService firestoreArtworkService;

    @Autowired
    private CloudinaryService cloudinaryService;



    // Get the current user profile
    private User getCurrentUser(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authentication required");
        }

        return firestoreUserService.getUserByEmail(
                authentication.getName());
    }

    // Verify that the user is an artist
    private User requireArtist(Authentication authentication) {

        User user = getCurrentUser(authentication);

        if (!"ARTIST".equalsIgnoreCase(user.getAccountType())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only artists can perform this action");
        }

        return user;
    }

    // Get Firebase UID from the user profile
    private String getArtistUid(User user) {

        if (user.getUserUid() == null || user.getUserUid().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Artist Firebase UID is missing from the profile");
        }

        return user.getUserUid();
    }

    // Upload artwork to Cloudinary and save metadata in Firestore
    public String uploadArtwork(
            ArtworkRequest request,
            MultipartFile image,
            Authentication authentication) throws IOException {

        User user = requireArtist(authentication);

        if (image == null || image.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Artwork image is required");
        }

        String imageUrl = cloudinaryService.uploadImage(image);

        FirestoreArtwork artwork = new FirestoreArtwork();

        artwork.setTitle(request.getTitle());
        artwork.setDescription(request.getDescription());
        artwork.setCategory(request.getCategory());
        artwork.setImageUrl(imageUrl);
        artwork.setArtistUid(getArtistUid(user));
        artwork.setArtistName(user.getFullName());
        artwork.setArtistProfileImageUrl(user.getProfileImageUrl());
        artwork.setForSale(false);
        artwork.setPrice(BigDecimal.ZERO);
        artwork.setCurrency("INR");
        artwork.setStatus("AVAILABLE");

        try {
            String id = firestoreArtworkService.createArtwork(artwork);

            return "Artwork uploaded successfully. ID: " + id;

        } catch (ExecutionException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Unable to save artwork to Firestore", e);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Artwork upload interrupted", e);
        }
    }

    // Get all artworks
    public List<ArtworkResponse> getAllArtworks() {

        List<ArtworkResponse> result = new ArrayList<>();

        for (FirestoreArtwork artwork : getAllFirestoreArtworks()) {
            result.add(toArtworkResponse(artwork));
        }

        return result;
    }

    // Get artwork by Firestore document ID
    public ArtworkResponse getArtworkById(String id) {

        return toArtworkResponse(getById(id));
    }

    // Update artwork
    public String updateArtwork(
            String id,
            ArtworkRequest request,
            Authentication authentication) {

        User user = requireArtist(authentication);

        FirestoreArtwork artwork = getById(id);

        checkOwnership(artwork, user);

        artwork.setTitle(request.getTitle());
        artwork.setDescription(request.getDescription());
        artwork.setCategory(request.getCategory());

        saveUpdate(artwork);

        return "Artwork updated successfully";
    }

    // Delete artwork
    public String deleteArtwork(
            String id,
            Authentication authentication) {

        User user = requireArtist(authentication);

        FirestoreArtwork artwork = getById(id);

        checkOwnership(artwork, user);

        try {
            firestoreArtworkService.deleteArtwork(artwork.getId());

            return "Artwork deleted successfully";

        } catch (ExecutionException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Unable to delete artwork", e);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Artwork deletion interrupted", e);
        }
    }

    // Get artwork feed
    // Like and comment counts are placeholders until those modules migrate.
    public List<ArtworkFeedResponse> getArtworkFeed(User currentUser) {

        List<ArtworkFeedResponse> result = new ArrayList<>();

        for (FirestoreArtwork artwork : getAllFirestoreArtworks()) {
            result.add(toFeedResponse(artwork));
        }

        return result;
    }

    // Get artworks belonging to the current artist
    public List<ArtworkFeedResponse> getMyArtworks(User user) {

        List<ArtworkFeedResponse> result = new ArrayList<>();

        String artistUid = getArtistUid(user);

        for (FirestoreArtwork artwork : getAllFirestoreArtworks()) {

            if (artistUid.equals(artwork.getArtistUid())) {
                result.add(toFeedResponse(artwork));
            }
        }

        return result;
    }

    // Put artwork for sale
    public String putArtworkForSale(
            String artworkId,
            SellArtworkRequest request,
            Authentication authentication) {

        User user = requireArtist(authentication);

        FirestoreArtwork artwork = getById(artworkId);

        checkOwnership(artwork, user);

        if ("SOLD".equalsIgnoreCase(artwork.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Sold artwork cannot be listed again");
        }

        if (request.getPrice() == null
                || request.getPrice().compareTo(BigDecimal.ZERO) <= 0) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Artwork price must be greater than zero");
        }

        artwork.setForSale(true);
        artwork.setPrice(request.getPrice());
        artwork.setCurrency("INR");
        artwork.setStatus("AVAILABLE");

        saveUpdate(artwork);

        return "Artwork is now available for sale";
    }

    // Get marketplace artworks
    public List<MarketplaceArtworkResponse> getMarketplaceArtworks() {

        List<MarketplaceArtworkResponse> result = new ArrayList<>();

        for (FirestoreArtwork artwork : getAllFirestoreArtworks()) {

            if (artwork.isForSale()
                    && "AVAILABLE".equalsIgnoreCase(artwork.getStatus())) {

                result.add(new MarketplaceArtworkResponse(
                        artwork.getId(),
                        artwork.getTitle(),
                        artwork.getDescription(),
                        artwork.getImageUrl(),
                        artwork.getCategory(),
                        artwork.getArtistName(),
                        artwork.getArtistProfileImageUrl(),
                        artwork.getPrice(),
                        artwork.getCurrency()));
            }
        }

        return result;
    }

    // Remove artwork from sale
    public String removeArtworkFromSale(
            String artworkId,
            Authentication authentication) {

        User user = requireArtist(authentication);

        FirestoreArtwork artwork = getById(artworkId);

        checkOwnership(artwork, user);

        if ("SOLD".equalsIgnoreCase(artwork.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Sold artwork cannot be removed from sale");
        }

        artwork.setForSale(false);
        artwork.setPrice(BigDecimal.ZERO);
        artwork.setCurrency("INR");
        artwork.setStatus("AVAILABLE");

        saveUpdate(artwork);

        return "Artwork removed from marketplace";
    }

    // Fetch all artworks from Firestore
    private List<FirestoreArtwork> getAllFirestoreArtworks() {

        try {
            return firestoreArtworkService.getAllArtworks();

        } catch (ExecutionException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Unable to retrieve artworks", e);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Artwork retrieval interrupted", e);
        }
    }

    // Fetch one artwork by document ID
    private FirestoreArtwork getById(String id) {

        try {
            FirestoreArtwork artwork =
                    firestoreArtworkService.getArtworkById(id);

            if (artwork == null) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Artwork not found");
            }

            return artwork;

        } catch (ExecutionException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Unable to retrieve artwork", e);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Artwork retrieval interrupted", e);
        }
    }

    // Verify artwork ownership using Firebase UID
    private void checkOwnership(FirestoreArtwork artwork, User user) {

        if (!getArtistUid(user).equals(artwork.getArtistUid())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You can only modify your own artwork");
        }
    }

    // Save changes to Firestore
    private void saveUpdate(FirestoreArtwork artwork) {

        try {
            firestoreArtworkService.updateArtwork(
                    artwork.getId(), artwork);

        } catch (ExecutionException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Unable to update artwork", e);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Artwork update interrupted", e);
        }
    }

    // Convert Firestore model to API response
    private ArtworkResponse toArtworkResponse(FirestoreArtwork artwork) {

        return new ArtworkResponse(
                artwork.getId(),
                artwork.getTitle(),
                artwork.getDescription(),
                artwork.getImageUrl(),
                artwork.getCategory(),
                artwork.getArtistName());
    }

    // Convert Firestore model to feed response
    private ArtworkFeedResponse toFeedResponse(FirestoreArtwork artwork) {

        return new ArtworkFeedResponse(
                artwork.getId(),
                artwork.getTitle(),
                artwork.getDescription(),
                artwork.getCategory(),
                artwork.getImageUrl(),
                artwork.getArtistName(),
                0,
                0,
                false);
    }
}