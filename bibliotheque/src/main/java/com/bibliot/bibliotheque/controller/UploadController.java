package com.bibliot.bibliotheque.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/uploads")
@Tag(name = "Uploads", description = "Gestion des fichiers et images uploadés localement")
public class UploadController {

    private final Path fileStorageLocation;

    public UploadController() {
        // Crée automatiquement le dossier "uploads" à la racine du projet backend.
        // Ce dossier n'est pas effacé quand on recompile le projet, ce qui est parfait pour la persistance locale.
        this.fileStorageLocation = Paths.get("uploads").toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Impossible de créer le répertoire où les fichiers téléchargés seront stockés.", ex);
        }
    }

    @PostMapping("/image")
    @Operation(summary = "Uploader une image de couverture de livre")
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file) {
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());

        try {
            if (fileName.contains("..")) {
                return ResponseEntity.badRequest().body(Map.of("message", "Nom de fichier invalide " + fileName));
            }

            // Pour éviter les doublons de noms, on rajoute un ID unique
            String extension = fileName.substring(fileName.lastIndexOf("."));
            String uniqueFileName = UUID.randomUUID().toString() + extension;

            Path targetLocation = this.fileStorageLocation.resolve(uniqueFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            // On construit l'URL complète pour lire l'image ensuite
            String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/api/uploads/")
                    .path(uniqueFileName)
                    .toUriString();

            return ResponseEntity.ok(Map.of("url", fileDownloadUri));

        } catch (IOException ex) {
            return ResponseEntity.internalServerError().body(Map.of("message", "Impossible d'enregistrer le fichier."));
        }
    }

    @GetMapping("/{fileName:.+}")
    @Operation(summary = "Lire une image (Accès public)")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists()) {
                String contentType = "image/jpeg";
                if(fileName.toLowerCase().endsWith(".png")) contentType = "image/png";
                if(fileName.toLowerCase().endsWith(".gif")) contentType = "image/gif";
                if(fileName.toLowerCase().endsWith(".webp")) contentType = "image/webp";
                
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_TYPE, contentType)
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException ex) {
            return ResponseEntity.notFound().build();
        }
    }
}
