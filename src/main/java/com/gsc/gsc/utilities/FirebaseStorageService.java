package com.gsc.gsc.utilities;

import com.google.cloud.storage.*;
import com.google.firebase.cloud.StorageClient;
import com.gsc.gsc.model.StoreImage;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class FirebaseStorageService {

    public String uploadImage(String folder, MultipartFile file) throws IOException {
        // Ensure the folder is not empty and normalize it
        folder = Objects.requireNonNullElse(folder, "").trim().replaceAll("/+", "/");
        String fileName = generateUniqueFileName(folder, file.getOriginalFilename());

        try (InputStream fileStream = file.getInputStream()) {
            StorageClient.getInstance().bucket().create(fileName, fileStream, file.getContentType());
        }
        return fileName;
    }
    private String generateUniqueFileName(String folder, String originalFilename) {
        String extension = originalFilename.substring(originalFilename.lastIndexOf('.'));
        String uniqueName = UUID.randomUUID().toString().replace("-", "") + extension;
        return folder.isEmpty() ? uniqueName : folder + "/" + uniqueName;
    }

    private static final String FIREBASE_BUCKET_NAME = "gsc-appplication.appspot.com";

    public List<String> getImagesInFolder(String folder) {
        Storage storage = StorageOptions.getDefaultInstance().getService();

        List<Blob> blobs =
                StreamSupport.stream(storage.list(FIREBASE_BUCKET_NAME, Storage.BlobListOption.prefix(folder)).iterateAll().spliterator(), false)
                        .collect(Collectors.toList());

        return blobs.stream()
                .filter(blob -> !blob.isDirectory())
                .map(Blob::getName)
                .collect(Collectors.toList());
    }
}