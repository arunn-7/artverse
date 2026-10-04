package com.artverse.config;

import com.artverse.entity.User;
import com.artverse.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {

        String adminEmail = "admin@artverse.com";

        if (!userRepository.existsByEmail(adminEmail)) {

            User admin = new User();

            admin.setFullName("ArtVerse Admin");
            admin.setEmail(adminEmail);

            admin.setPassword(
                    passwordEncoder.encode("arun@123")
            );

            admin.setRole("ADMIN");
            admin.setAccountType("ADMIN");
            admin.setArtistLevel(null);
            admin.setVerificationStatus("NOT_APPLICABLE");

            userRepository.save(admin);

            System.out.println("=================================");
            System.out.println("ArtVerse Admin created successfully");
            System.out.println("Email: admin@artverse.com");
            System.out.println("=================================");

        } else {
            System.out.println("ArtVerse Admin already exists.");
        }
    }
}