package com.artverse.controller;

import com.artverse.service.CertificateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/certificates")
public class CertificateController {

    @Autowired
    private CertificateService certificateService;

    @PostMapping(
            value = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public String uploadCertificate(
            @RequestParam("file") MultipartFile file,
            @RequestParam("certificateName") String certificateName,
            Authentication authentication
    ) {

        try {

            return certificateService.uploadCertificate(
                    file,
                    certificateName,
                    authentication.getName()
            );

        } catch (Exception e) {

            return "Certificate upload failed: "
                    + e.getMessage();
        }
    }
}