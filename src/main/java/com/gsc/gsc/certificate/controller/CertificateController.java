package com.gsc.gsc.certificate.controller;

import com.gsc.gsc.certificate.service.CertificateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/certificates")
public class CertificateController {

    @Autowired
    private CertificateService certificateService;

    @PostMapping("")
    public ResponseEntity<?> addCertificate(
            @RequestHeader("Authorization") String token,
            @RequestParam("file") MultipartFile file,
            @RequestParam("name") String name,
            @RequestParam(value = "description", required = false) String description) {
        return certificateService.addCertificate(token, file, name, description);
    }

    @GetMapping("")
    public ResponseEntity<?> getCertificates(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return certificateService.getCertificates(token, page, size);
    }
}
