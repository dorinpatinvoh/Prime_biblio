package com.bibliot.bibliotheque.controller;

import com.bibliot.bibliotheque.dto.LivreDTO;
import com.bibliot.bibliotheque.model.Livre;
import com.bibliot.bibliotheque.service.LivreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/livres")
@RequiredArgsConstructor
@Tag(name = "Endpoints des livres", description = "Gestion du catalogue de livres")
public class LivreController {

    private final LivreService livreService;

    // --- LECTURE PUBLIQUE ---

    @Operation(summary = "Liste des livres", description = "Afficher tous les livres disponibles")
    @GetMapping
    public List<LivreDTO> getAll() {
        return livreService.getAllLivres();
    }

    @Operation(summary = "Trouver un livre par ID", description = "Retourne un livre spécifique via son ID")
    @GetMapping("/{id}")
    public ResponseEntity<Livre> findById(@PathVariable Long id) {
        return livreService.getLivreById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // --- GESTION (Admin & Bibliothecaire) ---

    @Operation(summary = "Créer un livre solo", description = "Ajouter un nouveau livre au catalogue")
    @PostMapping("/solo")
    @PreAuthorize("hasAnyRole('ADMIN', 'BIBLIOTHECAIRE')")
    public ResponseEntity<Livre> saveOne(@RequestBody Livre livre) {
        Livre savedLivre = livreService.createLivre(livre);
        return new ResponseEntity<>(savedLivre, HttpStatus.CREATED);
    }

    @Operation(summary = "Créer plusieurs livres", description = "Importer une liste de livres")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'BIBLIOTHECAIRE')")
    public ResponseEntity<List<Livre>> saveAll(@RequestBody List<Livre> livres) {
        List<Livre> savedLivres = livreService.createLivres(livres);
        return new ResponseEntity<>(savedLivres, HttpStatus.CREATED);
    }

    @Operation(summary = "Modifier un livre", description = "Met à jour les détails d'un livre")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BIBLIOTHECAIRE')")
    public ResponseEntity<Livre> update(@PathVariable Long id, @RequestBody Livre livreDetails) {
        try {
            Livre updatedLivre = livreService.updateLivre(id, livreDetails);
            return ResponseEntity.ok(updatedLivre);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Supprimer un livre", description = "Retirer un livre du catalogue")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Livre supprimé"),
            @ApiResponse(responseCode = "404", description = "Livre non trouvé")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BIBLIOTHECAIRE')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        try {
            livreService.deleteLivre(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}