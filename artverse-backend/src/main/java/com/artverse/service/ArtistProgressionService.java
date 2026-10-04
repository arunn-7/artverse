package com.artverse.service;

import com.artverse.entity.User;
import com.artverse.repository.FollowRepository;
import com.artverse.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class ArtistProgressionService {

    @Autowired
    private FollowRepository followRepository;

    @Autowired
    private UserRepository userRepository;


    // =====================================================
    // CHECK ONE ARTIST
    // =====================================================

    public void checkAndUpgradeArtist(User artist) {

        // Only artists can progress
        if (!"ARTIST".equals(artist.getAccountType())) {
            return;
        }

        long followers =
                followRepository.countByFollowing(artist);


        // BEGINNER → INTERMEDIATE
        if ("BEGINNER".equals(artist.getArtistLevel())
                && followers >= 1000) {

            artist.setArtistLevel("INTERMEDIATE");

            userRepository.save(artist);

            return;
        }


        // INTERMEDIATE → PROFESSIONAL
        if ("INTERMEDIATE".equals(artist.getArtistLevel())
                && followers >= 5000) {

            artist.setArtistLevel("PROFESSIONAL");

            userRepository.save(artist);
        }
    }


    // =====================================================
    // AUTOMATIC PERIODIC CHECK
    // =====================================================

    @Scheduled(fixedRate = 3600000)
    public void checkAllArtists() {

        userRepository
                .findByAccountType("ARTIST")
                .forEach(this::checkAndUpgradeArtist);
    }
}