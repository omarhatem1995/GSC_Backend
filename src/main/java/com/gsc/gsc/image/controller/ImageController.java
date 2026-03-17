package com.gsc.gsc.image.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/images")
public class ImageController {

    @Autowired
    private FirebaseStorageService storageService;

    @PostMapping("upload")
    public ResponseEntity<String> uploadImage(@RequestParam("folder") String folder,
                                              @RequestParam("file") MultipartFile file) {
        try {
            String fileName = storageService.uploadImage(folder,file);
            return ResponseEntity.ok("File uploaded successfully. File name: " + fileName);
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Failed to upload file.");
        }
    }


    @GetMapping("/brands")
    public ResponseEntity<List<String>> listImagesInFolder(@RequestParam("folder") String folder) {
        try {
            List<String> imageNames = storageService.getImagesInFolder(folder);
            return ResponseEntity.ok(imageNames);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(List.of("Failed to retrieve image list."));
        }
    }
}