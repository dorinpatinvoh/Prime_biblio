package com.bibliot.bibliotheque.controller;

import com.bibliot.bibliotheque.service.ImageStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Tag(name = "Fichiers", description = "Gestion des uploads d'images")
public class FileController {

    private final ImageStorageService storageService;

    @PostMapping("/upload")
    @PreAuthorize("hasAnyRole('ADMIN', 'BIBLIOTHECAIRE')")
    @Operation(summary = "Uploader une image de couverture")
    public ResponseEntity<Map<String, String>> uploadFile(@RequestParam("file") MultipartFile file) {
        String fileName = storageService.storeFile(file);
        
        // On retourne l'URL complète pour que le front puisse l'utiliser directement
        String fileUrl = "http://localhost:8080/uploads/" + fileName;
        
        return ResponseEntity.ok(Map.of("url", fileUrl));
    }
}
