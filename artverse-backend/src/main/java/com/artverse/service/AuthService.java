package com.artverse.service;

import com.artverse.dto.LoginRequest;
import com.artverse.dto.LoginResponse;
import com.artverse.dto.RegisterRequest;
import com.artverse.entity.User;
import com.artverse.repository.UserRepository;
import com.artverse.security.jwt.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    private final BCryptPasswordEncoder passwordEncoder =
            new BCryptPasswordEncoder();


    // =====================================================
    // REGISTER
    // =====================================================

    public String register(RegisterRequest request) {

        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            return "Email already exists";
        }

        // Create new user
        User user = new User();

        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());

        user.setPassword(
                passwordEncoder.encode(request.getPassword())
        );

        String accountType =
                request.getAccountType().toUpperCase();

        // =================================================
        // NORMAL USER
        // =================================================

        if (accountType.equals("USER")) {

            user.setAccountType("USER");
            user.setRole("USER");

            user.setArtistLevel(null);
            user.setVerificationStatus("NOT_APPLICABLE");

        }

        // =================================================
        // ARTIST
        // =================================================

        else if (accountType.equals("ARTIST")) {

            user.setAccountType("ARTIST");
            user.setRole("ARTIST");

            // Artist level is required
            if (request.getArtistLevel() == null ||
                    request.getArtistLevel().isBlank()) {

                return "Artist level is required";
            }

            String artistLevel =
                    request.getArtistLevel().toUpperCase();

            // Validate artist level
            if (!artistLevel.equals("BEGINNER")
                    && !artistLevel.equals("INTERMEDIATE")
                    && !artistLevel.equals("PROFESSIONAL")) {

                return "Invalid artist level";
            }

            user.setArtistLevel(artistLevel);

            // Beginner doesn't require verification
            if (artistLevel.equals("BEGINNER")) {

                user.setVerificationStatus(
                        "NOT_REQUIRED"
                );

            }

            // Intermediate and Professional
            // require certificate verification
            else {

                user.setVerificationStatus(
                        "PENDING"
                );
            }

        }

        // =================================================
        // INVALID ACCOUNT TYPE
        // =================================================

        else {

            return "Invalid account type";
        }

        user.setCreatedAt(LocalDateTime.now());

        // Save user
        userRepository.save(user);

        return "Registration Successful";
    }


    // =====================================================
    // LOGIN
    // =====================================================

    public LoginResponse login(LoginRequest request) {

        User user = userRepository
                .findByEmail(request.getEmail())
                .orElse(null);

        if (user == null) {
            throw new RuntimeException("User not found");
        }

        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword())) {

            throw new RuntimeException("Invalid password");
        }

        String token =
                jwtService.generateToken(user.getEmail());

        return new LoginResponse(
                token,
                user.getFullName(),
                user.getEmail(),
                user.getRole()
        );
    }
}