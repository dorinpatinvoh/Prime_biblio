package com.bibliot.bibliotheque.controller;

import com.bibliot.bibliotheque.enums.Statut;
import com.bibliot.bibliotheque.model.Emprunt;
import com.bibliot.bibliotheque.service.EmpruntService;
import com.bibliot.bibliotheque.service.ExemplaireService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/guichet")
@RequiredArgsConstructor
@Tag(name = "Guichet Bibliothécaire", description = "Opérations quotidiennes du personnel")
// On protège TOUT le contrôleur pour le staff uniquement
@PreAuthorize("hasAnyRole('BIBLIOTHECAIRE', 'ADMIN')")
public class GuichetController {

    private final EmpruntService empruntService;
    private final ExemplaireService exemplaireService;

    @GetMapping("/scan/{codebarre}")
    @Operation(summary = "Douchette : Scanner un livre physique")
    public ResponseEntity<Map<String, Object>> scannerLivre(@PathVariable String codebarre) {
        return ResponseEntity.ok(exemplaireService.scannerCodeBarre(codebarre));
    }

    @PutMapping("/perte/{empruntId}")
    @Operation(summary = "Déclarer un exemplaire comme PERDU par le client")
    public ResponseEntity<Emprunt> declarerPerte(@PathVariable Long empruntId) {
        return ResponseEntity.ok(empruntService.declarerPerte(empruntId));
    }

    @GetMapping("/relances")
    @Operation(summary = "Voir tous les clients en retard pour les appeler")
    public ResponseEntity<List<Emprunt>> getRelances() {
        return ResponseEntity.ok(empruntService.getListeRetards());
    }
}