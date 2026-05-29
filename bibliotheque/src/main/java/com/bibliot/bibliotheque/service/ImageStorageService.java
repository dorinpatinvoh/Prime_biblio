package com.bibliot.bibliotheque.service;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class ImageStorageService {

    private final Path storageLocation = Paths.get("uploads");

    public ImageStorageService() {
        try {
            Files.createDirectories(storageLocation);
        } catch (IOException e) {
            throw new RuntimeException("Impossible de créer le dossier d'upload", e);
        }
    }

    public String storeFile(MultipartFile file) {
        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
        String extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        String fileName = UUID.randomUUID().toString() + extension;

        try {
            Path targetLocation = this.storageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            return fileName;
        } catch (IOException e) {
            throw new RuntimeException("Erreur lors de l'enregistrement de l'image", e);
        }
    }
}
