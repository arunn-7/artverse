package com.artverse.service;

import com.artverse.entity.Certificate;
import com.artverse.entity.User;
import com.artverse.repository.CertificateRepository;
import com.artverse.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class CertificateService {

    @Autowired
    private CertificateRepository certificateRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CloudinaryService cloudinaryService;


    public String uploadCertificate(
            MultipartFile file,
            String certificateName,
            String userEmail
    ) throws IOException {

        // Find logged-in user
        User user = userRepository
                .findByEmail(userEmail)
                .orElseThrow(() ->
                        new RuntimeException("User not found")
                );


        // Make sure the user is an artist
        if (!"ARTIST".equals(user.getAccountType())) {
            throw new RuntimeException(
                    "Only artists can upload certificates"
            );
        }


        // Make sure certificate is required
        if ("BEGINNER".equals(user.getArtistLevel())) {
            throw new RuntimeException(
                    "Beginner artists do not require certificates"
            );
        }


        // Validate file
        if (file == null || file.isEmpty()) {
            throw new RuntimeException(
                    "Certificate file is required"
            );
        }


        // Upload certificate to Cloudinary
        String certificateUrl =
                cloudinaryService.uploadCertificate(file);


        // Create certificate record
        Certificate certificate = new Certificate();

        certificate.setUser(user);
        certificate.setCertificateName(certificateName);
        certificate.setCertificateUrl(certificateUrl);
        certificate.setVerificationStatus("PENDING");


        // Save certificate information
        certificateRepository.save(certificate);


        // Keep artist verification pending
        user.setVerificationStatus("PENDING");

        userRepository.save(user);


        return "Certificate uploaded successfully";
    }
}