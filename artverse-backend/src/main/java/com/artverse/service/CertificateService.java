package com.artverse.service;

import com.artverse.entity.User;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service
public class CertificateService {

    private final Firestore firestore;
    private final FirestoreUserService userService;
    private final CloudinaryService cloudinaryService;

    public CertificateService(
            Firestore firestore,
            FirestoreUserService userService,
            CloudinaryService cloudinaryService) {
        this.firestore = firestore;
        this.userService = userService;
        this.cloudinaryService = cloudinaryService;
    }

    public String uploadCertificate(
            MultipartFile file,
            String certificateName,
            String userEmail) throws IOException {

        User user = userService.getUserByEmail(
                userEmail.trim().toLowerCase()
        );

        if (user == null) {
            throw new RuntimeException("User not found");
        }

        if (!"ARTIST".equalsIgnoreCase(user.getAccountType())) {
            throw new RuntimeException(
                    "Only artists can upload certificates"
            );
        }

        if ("BEGINNER".equalsIgnoreCase(user.getArtistLevel())) {
            throw new RuntimeException(
                    "Beginner artists do not require certificates"
            );
        }

        if (user.getUserUid() == null
                || user.getUserUid().isBlank()) {
            throw new RuntimeException(
                    "Artist Firebase UID is missing"
            );
        }

        if (file == null || file.isEmpty()) {
            throw new RuntimeException(
                    "Certificate file is required"
            );
        }

        if (certificateName == null
                || certificateName.isBlank()) {
            throw new RuntimeException(
                    "Certificate name is required"
            );
        }

        // Upload file using the existing Cloudinary service
        String certificateUrl =
                cloudinaryService.uploadCertificate(file);

        // Save certificate metadata in Firestore
        Map<String, Object> data = new HashMap<>();
        data.put("userUid", user.getUserUid());
        data.put("userEmail", user.getEmail());
        data.put("certificateName", certificateName.trim());
        data.put("certificateUrl", certificateUrl);
        data.put("verificationStatus", "PENDING");
        data.put("uploadedAt", Instant.now().toString());

        try {
            firestore.collection("certificates")
                    .add(data)
                    .get();

            user.setVerificationStatus("PENDING");
            userService.save(user);

            return "Certificate uploaded successfully";

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(
                    "Certificate upload was interrupted", e
            );

        } catch (ExecutionException e) {
            throw new RuntimeException(
                    "Failed to save certificate in Firestore", e
            );
        }
    }

    public String approveCertificate(String certificateId) {
        return updateCertificateStatus(
                certificateId, "APPROVED"
        );
    }

    public String rejectCertificate(String certificateId) {
        return updateCertificateStatus(
                certificateId, "REJECTED"
        );
    }

    private String updateCertificateStatus(
            String certificateId,
            String status) {

        try {
            var certificateRef = firestore
                    .collection("certificates")
                    .document(certificateId);

            DocumentSnapshot document =
                    certificateRef.get().get();

            if (!document.exists()) {
                throw new RuntimeException(
                        "Certificate not found"
                );
            }

            String userEmail = document.getString("userEmail");

            if (userEmail == null || userEmail.isBlank()) {
                throw new RuntimeException(
                        "Certificate owner email is missing"
                );
            }

            User artist = userService.getUserByEmail(userEmail);

            if (artist == null
                    || !"ARTIST".equalsIgnoreCase(
                    artist.getAccountType())) {
                throw new RuntimeException(
                        "Certificate owner is not a valid artist"
                );
            }

            // Update certificate status
            certificateRef.update(
                    "verificationStatus", status
            ).get();

            // Update artist verification status
            artist.setVerificationStatus(status);
            userService.save(artist);

            return status.equals("APPROVED")
                    ? "Certificate approved successfully"
                    : "Certificate rejected successfully";

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(
                    "Certificate verification was interrupted", e
            );

        } catch (ExecutionException e) {
            throw new RuntimeException(
                    "Failed to update certificate status", e
            );
        }
    }
}