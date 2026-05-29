package com.bibliot.bibliotheque.controller;

import com.bibliot.bibliotheque.model.Exemplaire;
import com.bibliot.bibliotheque.service.ExemplaireService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/exemplaires")
@RequiredArgsConstructor
@Tag(name = "Endpoints des exemplaires", description = "Gestion de l'inventaire physique")
public class ExemplaireController {

    private final ExemplaireService exemplaireService;

    @Operation(summary = "Liste de tous les exemplaires")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'BIBLIOTHECAIRE')")
    public List<Exemplaire> findAll() {
        return exemplaireService.getAllExemplaires();
    }

    @Operation(summary = "Trouver un exemplaire par ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BIBLIOTHECAIRE')")
    public ResponseEntity<Exemplaire> findById(@PathVariable Long id) {
        return exemplaireService.getExemplaireById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Ajouter un exemplaire à un livre")
    @PostMapping("/livre/{livreId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BIBLIOTHECAIRE')")
    public ResponseEntity<Exemplaire> create(@PathVariable Long livreId, @RequestBody Exemplaire exemplaire) {
        return new ResponseEntity<>(exemplaireService.ajouterUnExemplaire(livreId, exemplaire), HttpStatus.CREATED);
    }

    @Operation(summary = "Ajouter plusieurs exemplaires par quantité")
    @PostMapping("/livre/{livreId}/add-multiple")
    @PreAuthorize("hasAnyRole('ADMIN', 'BIBLIOTHECAIRE')")
    public ResponseEntity<List<Exemplaire>> addMultipleByQty(@PathVariable Long livreId, @RequestParam int quantite) {
        return new ResponseEntity<>(exemplaireService.ajouterPlusieursExemplairesParQuantite(livreId, quantite), HttpStatus.CREATED);
    }

    @Operation(summary = "Ajouter plusieurs exemplaires (Batch)")
    @PostMapping("/livre/{livreId}/batch")
    @PreAuthorize("hasAnyRole('ADMIN', 'BIBLIOTHECAIRE')")
    public ResponseEntity<List<Exemplaire>> createMultiple(@PathVariable Long livreId, @RequestBody List<Exemplaire> exemplaires) {
        return new ResponseEntity<>(exemplaireService.ajouterPlusieursExemplaires(livreId, exemplaires), HttpStatus.CREATED);
    }

    @Operation(summary = "Modifier un exemplaire")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BIBLIOTHECAIRE')")
    public ResponseEntity<Exemplaire> update(@PathVariable Long id, @RequestBody Exemplaire details) {
        try {
            return ResponseEntity.ok(exemplaireService.modifierExemplaire(id, details));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Supprimer un exemplaire")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BIBLIOTHECAIRE')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        try {
            exemplaireService.supprimerExemplaire(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}