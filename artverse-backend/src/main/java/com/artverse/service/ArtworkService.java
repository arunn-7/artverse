package com.artverse.service;

import com.artverse.dto.ArtworkFeedResponse;
import com.artverse.dto.ArtworkRequest;
import com.artverse.dto.ArtworkResponse;
import com.artverse.dto.MarketplaceArtworkResponse;
import com.artverse.dto.SellArtworkRequest;
import com.artverse.entity.Artwork;
import com.artverse.entity.ArtworkStatus;
import com.artverse.entity.User;
import com.artverse.exception.ArtworkNotFoundException;
import com.artverse.exception.UserNotFoundException;
import com.artverse.repository.ArtworkRepository;
import com.artverse.repository.CommentRepository;
import com.artverse.repository.LikeRepository;
import com.artverse.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ArtworkService {

    @Autowired
    private CloudinaryService cloudinaryService;

    @Autowired
    private ArtworkRepository artworkRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private CommentRepository commentRepository;


    // =====================================================
    // CHECK ARTIST
    // =====================================================

    private User getArtist(Authentication authentication) {

        User user = userRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new UserNotFoundException("User not found"));

        if (!"ARTIST".equalsIgnoreCase(user.getAccountType())) {

            throw new RuntimeException(
                    "Only artists can perform this action"
            );
        }

        return user;
    }


    // =====================================================
    // UPLOAD ARTWORK
    // ARTIST ONLY
    // =====================================================

    public String uploadArtwork(
            ArtworkRequest request,
            MultipartFile image,
            Authentication authentication
    ) throws IOException {

        User user = getArtist(authentication);

        String imageUrl =
                cloudinaryService.uploadImage(image);

        Artwork artwork = new Artwork();

        artwork.setTitle(request.getTitle());
        artwork.setDescription(request.getDescription());
        artwork.setImageUrl(imageUrl);
        artwork.setCategory(request.getCategory());
        artwork.setUser(user);

        artworkRepository.save(artwork);

        return "Artwork uploaded successfully";
    }


    // =====================================================
    // GET ALL ARTWORKS
    // USER + ARTIST
    // =====================================================

    public List<ArtworkResponse> getAllArtworks() {

        return artworkRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }


    // =====================================================
    // GET ARTWORK BY ID
    // USER + ARTIST
    // =====================================================

    public ArtworkResponse getArtworkById(Long id) {

        Artwork artwork = artworkRepository.findById(id)
                .orElseThrow(() ->
                        new ArtworkNotFoundException(
                                "Artwork not found"
                        ));

        return mapToResponse(artwork);
    }


    // =====================================================
    // UPDATE ARTWORK
    // ARTIST ONLY
    // =====================================================

    public String updateArtwork(
            Long id,
            ArtworkRequest request,
            Authentication authentication
    ) {

        User user = getArtist(authentication);

        Artwork artwork = artworkRepository.findById(id)
                .orElseThrow(() ->
                        new ArtworkNotFoundException(
                                "Artwork not found"
                        ));

        // Check ownership
        if (!artwork.getUser().getId().equals(user.getId())) {

            throw new RuntimeException(
                    "You are not allowed to update this artwork"
            );
        }

        artwork.setTitle(request.getTitle());
        artwork.setDescription(request.getDescription());
        artwork.setCategory(request.getCategory());

        artworkRepository.save(artwork);

        return "Artwork updated successfully";
    }


    // =====================================================
    // DELETE ARTWORK
    // ARTIST ONLY
    // =====================================================

    public String deleteArtwork(
            Long id,
            Authentication authentication
    ) {

        User user = getArtist(authentication);

        Artwork artwork = artworkRepository.findById(id)
                .orElseThrow(() ->
                        new ArtworkNotFoundException(
                                "Artwork not found"
                        ));

        // Check ownership
        if (!artwork.getUser().getId().equals(user.getId())) {

            throw new RuntimeException(
                    "You are not allowed to delete this artwork"
            );
        }

        artworkRepository.delete(artwork);

        return "Artwork deleted successfully";
    }


    // =====================================================
    // MAP ARTWORK RESPONSE
    // =====================================================

    private ArtworkResponse mapToResponse(Artwork artwork) {

        return new ArtworkResponse(
                artwork.getId(),
                artwork.getTitle(),
                artwork.getDescription(),
                artwork.getImageUrl(),
                artwork.getCategory(),
                artwork.getUser().getFullName()
        );
    }


    // =====================================================
    // ARTWORK FEED
    // USER + ARTIST
    // =====================================================

    public List<ArtworkFeedResponse> getArtworkFeed(
            User currentUser
    ) {

        return artworkRepository.findAll()
                .stream()
                .map(artwork -> {

                    long likeCount =
                            likeRepository.countByArtwork(
                                    artwork
                            );

                    long commentCount =
                            commentRepository.countByArtwork(
                                    artwork
                            );

                    boolean liked =
                            likeRepository.existsByUserAndArtwork(
                                    currentUser,
                                    artwork
                            );

                    return new ArtworkFeedResponse(
                            artwork.getId(),
                            artwork.getTitle(),
                            artwork.getDescription(),
                            artwork.getCategory(),
                            artwork.getImageUrl(),
                            artwork.getUser().getFullName(),
                            likeCount,
                            commentCount,
                            liked
                    );
                })
                .collect(Collectors.toList());
    }


    // =====================================================
    // MY ARTWORKS
    // =====================================================

    public List<ArtworkFeedResponse> getMyArtworks(
            User user
    ) {

        return artworkRepository.findByUser(user)
                .stream()
                .map(artwork -> new ArtworkFeedResponse(
                        artwork.getId(),
                        artwork.getTitle(),
                        artwork.getDescription(),
                        artwork.getCategory(),
                        artwork.getImageUrl(),
                        artwork.getUser().getFullName(),
                        likeRepository.countByArtwork(artwork),
                        commentRepository.countByArtwork(artwork),
                        likeRepository.existsByUserAndArtwork(
                                user,
                                artwork
                        )
                ))
                .collect(Collectors.toList());
    }


    // =====================================================
    // PUT ARTWORK FOR SALE
    // ARTIST ONLY
    // =====================================================

    public String putArtworkForSale(
            Long artworkId,
            SellArtworkRequest request,
            Authentication authentication
    ) {

        User user = getArtist(authentication);

        Artwork artwork = artworkRepository.findById(artworkId)
                .orElseThrow(() ->
                        new ArtworkNotFoundException(
                                "Artwork not found"
                        ));


        // Check ownership
        if (!artwork.getUser().getId().equals(user.getId())) {

            throw new RuntimeException(
                    "You can only sell your own artwork"
            );
        }


        // Prevent sold artwork from being listed again
        if (artwork.getStatus() == ArtworkStatus.SOLD) {

            throw new RuntimeException(
                    "Sold artwork cannot be listed for sale again"
            );
        }


        // Validate price
        if (request.getPrice() == null ||
                request.getPrice().compareTo(BigDecimal.ZERO) <= 0) {

            throw new RuntimeException(
                    "Artwork price must be greater than zero"
            );
        }


        artwork.setForSale(true);
        artwork.setPrice(request.getPrice());
        artwork.setCurrency("INR");
        artwork.setStatus(ArtworkStatus.AVAILABLE);

        artworkRepository.save(artwork);

        return "Artwork is now available for sale";
    }


    // =====================================================
    // GET MARKETPLACE ARTWORKS
    // USER + ARTIST
    // =====================================================

    public List<MarketplaceArtworkResponse>
    getMarketplaceArtworks() {

        return artworkRepository
                .findByForSaleTrueAndStatus(
                        ArtworkStatus.AVAILABLE
                )
                .stream()
                .map(artwork ->
                        new MarketplaceArtworkResponse(
                                artwork.getId(),
                                artwork.getTitle(),
                                artwork.getDescription(),
                                artwork.getImageUrl(),
                                artwork.getCategory(),
                                artwork.getUser().getFullName(),
                                artwork.getUser().getProfileImageUrl(),
                                artwork.getPrice(),
                                artwork.getCurrency()
                        )
                )
                .toList();
    }


    // =====================================================
    // REMOVE ARTWORK FROM SALE
    // ARTIST ONLY
    // =====================================================

    public String removeArtworkFromSale(
            Long artworkId,
            Authentication authentication
    ) {

        User user = getArtist(authentication);

        Artwork artwork = artworkRepository.findById(artworkId)
                .orElseThrow(() ->
                        new ArtworkNotFoundException(
                                "Artwork not found"
                        ));


        // Check ownership
        if (!artwork.getUser().getId().equals(user.getId())) {

            throw new RuntimeException(
                    "You can only modify your own artwork"
            );
        }


        // Sold artwork cannot be removed from sale
        if (artwork.getStatus() == ArtworkStatus.SOLD) {

            throw new RuntimeException(
                    "Sold artwork is not available for removal from sale"
            );
        }


        artwork.setForSale(false);
        artwork.setPrice(BigDecimal.ZERO);
        artwork.setCurrency("INR");
        artwork.setStatus(ArtworkStatus.AVAILABLE);

        artworkRepository.save(artwork);

        return "Artwork removed from marketplace";
    }
}