package com.gsc.gsc.certificate.service;

import com.gsc.gsc.constants.ReturnObject;
import com.gsc.gsc.constants.ReturnObjectPaging;
import com.gsc.gsc.model.Certificate;
import com.gsc.gsc.model.User;
import com.gsc.gsc.repo.CertificateRepository;
import com.gsc.gsc.repo.UserRepository;
import com.gsc.gsc.user.service.serviceImplementation.UserService;
import com.gsc.gsc.utilities.ImgBBService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static com.gsc.gsc.constants.UserTypes.ADMIN_TYPE;

@Service
public class CertificateService {

    private static final List<String> SUPPORTED_IMAGE_TYPES = Arrays.asList(
            "image/jpeg", "image/png", "image/gif", "image/bmp", "image/webp", "image/tiff"
    );

    @Autowired
    private CertificateRepository certificateRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private ImgBBService imgBBService;

    public ResponseEntity<?> addCertificate(String token, MultipartFile file,
                                            String name, String description) {
        ReturnObject returnObject = new ReturnObject();
        Integer adminId = userService.getUserIdFromToken(token);
        User admin = userRepository.findUserById(adminId);

        if (admin == null || admin.getAccountTypeId() != ADMIN_TYPE) {
            returnObject.setStatus(false);
            returnObject.setMessage("Unauthorized: Admin access required");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(returnObject);
        }

        String contentType = file.getContentType();
        String fileType;

        if (contentType != null && SUPPORTED_IMAGE_TYPES.contains(contentType)) {
            fileType = "IMAGE";
        } else if ("application/pdf".equals(contentType)) {
            returnObject.setStatus(false);
            returnObject.setMessage("PDF files are not supported by the current storage provider. Please upload an image format (JPG, PNG, etc.).");
            return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(returnObject);
        } else {
            returnObject.setStatus(false);
            returnObject.setMessage("Unsupported file type: " + contentType);
            return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(returnObject);
        }

        String fileUrl;
        try {
            fileUrl = imgBBService.uploadImage(file);
        } catch (IOException e) {
            returnObject.setStatus(false);
            returnObject.setMessage("Failed to upload file: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(returnObject);
        }

        Certificate certificate = new Certificate();
        certificate.setName(name);
        certificate.setDescription(description);
        certificate.setFileUrl(fileUrl);
        certificate.setFileType(fileType);
        certificate.setCreatedBy(adminId);

        certificateRepository.save(certificate);

        returnObject.setStatus(true);
        returnObject.setMessage("Certificate added successfully");
        returnObject.setData(certificate);
        return ResponseEntity.ok(returnObject);
    }

    public ResponseEntity<?> getCertificates(String token, int page, int size) {
        ReturnObject returnObject = new ReturnObject();
        Integer userId = userService.getUserIdFromToken(token);
        User user = userRepository.findUserById(userId);

        if (user == null) {
            returnObject.setStatus(false);
            returnObject.setMessage("Unauthorized");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(returnObject);
        }

        Page<Certificate> certificatePage = certificateRepository
                .findAllByOrderByCreatedAtDesc(PageRequest.of(page, size));

        ReturnObjectPaging paging = new ReturnObjectPaging();
        paging.setStatus(true);
        paging.setMessage("Loaded Successfully");
        paging.setData(certificatePage.getContent());
        paging.setTotalPages(certificatePage.getTotalPages());
        paging.setTotalCount(certificatePage.getTotalElements());
        return ResponseEntity.ok(paging);
    }
}
