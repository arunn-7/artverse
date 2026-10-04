package com.artverse.controller;

import com.artverse.dto.AdminCertificateResponse;
import com.artverse.entity.Certificate;
import com.artverse.repository.CertificateRepository;
import com.artverse.service.CertificateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/certificates")
public class AdminCertificateController {

    @Autowired
    private CertificateRepository certificateRepository;

    @Autowired
    private CertificateService certificateService;


    // ==========================================
    // GET ALL PENDING CERTIFICATES
    // ==========================================

    @GetMapping("/pending")
    public List<AdminCertificateResponse> getPendingCertificates() {

        return certificateRepository
                .findByVerificationStatus("PENDING")
                .stream()
                .map(this::convertToResponse)
                .toList();
    }


    // ==========================================
    // APPROVE CERTIFICATE
    // ==========================================

    @PutMapping("/{id}/approve")
    public String approveCertificate(
            @PathVariable Long id) {

        return certificateService.approveCertificate(id);
    }


    // ==========================================
    // REJECT CERTIFICATE
    // ==========================================

    @PutMapping("/{id}/reject")
    public String rejectCertificate(
            @PathVariable Long id) {

        return certificateService.rejectCertificate(id);
    }


    // ==========================================
    // CONVERT CERTIFICATE → RESPONSE
    // ==========================================

    private AdminCertificateResponse convertToResponse(
            Certificate certificate) {

        var artist = certificate.getUser();

        return new AdminCertificateResponse(
                certificate.getId(),
                artist.getId(),
                artist.getFullName(),
                artist.getEmail(),
                artist.getArtistLevel(),
                certificate.getCertificateName(),
                certificate.getCertificateUrl(),
                certificate.getVerificationStatus(),
                certificate.getUploadedAt()
        );
    }
}