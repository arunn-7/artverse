package com.artverse.controller;

import com.artverse.dto.AdminCertificateResponse;
import com.artverse.entity.User;
import com.artverse.service.CertificateService;
import com.artverse.service.FirestoreUserService;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/admin/certificates")
public class AdminCertificateController {

    private final Firestore firestore;
    private final CertificateService certificateService;
    private final FirestoreUserService userService;

    public AdminCertificateController(
            Firestore firestore,
            CertificateService certificateService,
            FirestoreUserService userService) {
        this.firestore = firestore;
        this.certificateService = certificateService;
        this.userService = userService;
    }

    // GET ALL PENDING CERTIFICATES
    @GetMapping("/pending")
    public List<AdminCertificateResponse> getPendingCertificates() {

        try {
            List<QueryDocumentSnapshot> documents = firestore
                    .collection("certificates")
                    .whereEqualTo("verificationStatus", "PENDING")
                    .get()
                    .get()
                    .getDocuments();

            List<AdminCertificateResponse> responses =
                    new ArrayList<>();

            for (QueryDocumentSnapshot document : documents) {
                String email = document.getString("userEmail");

                if (email == null || email.isBlank()) {
                    continue;
                }

                User artist = userService.getUserByEmail(email);

                if (artist == null) {
                    continue;
                }

                responses.add(new AdminCertificateResponse(
                        document.getId(),
                        artist.getUserUid(),
                        artist.getFullName(),
                        artist.getEmail(),
                        artist.getArtistLevel(),
                        document.getString("certificateName"),
                        document.getString("certificateUrl"),
                        document.getString("verificationStatus"),
                        document.getString("uploadedAt")
                ));
            }

            return responses;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(
                    "Loading pending certificates was interrupted", e);

        } catch (ExecutionException e) {
            throw new RuntimeException(
                    "Failed to load pending certificates", e);
        }
    }

    // APPROVE CERTIFICATE
    @PutMapping("/{id}/approve")
    public String approveCertificate(@PathVariable String id) {
        return certificateService.approveCertificate(id);
    }

    // REJECT CERTIFICATE
    @PutMapping("/{id}/reject")
    public String rejectCertificate(@PathVariable String id) {
        return certificateService.rejectCertificate(id);
    }
}