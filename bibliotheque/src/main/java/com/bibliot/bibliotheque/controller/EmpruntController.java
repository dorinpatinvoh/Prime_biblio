package com.bibliot.bibliotheque.controller;

import com.bibliot.bibliotheque.dto.GuichetPretRequest;
import com.bibliot.bibliotheque.enums.Etat;
import com.bibliot.bibliotheque.model.Emprunt;
import com.bibliot.bibliotheque.security.services.UserDetailsImpl;
import com.bibliot.bibliotheque.service.EmpruntService;
import com.bibliot.bibliotheque.service.UtilisateurService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/emprunts")
@RequiredArgsConstructor
@Tag(name = "Emprunts", description = "Gestion des prêts et retours de livres")
public class EmpruntController {

    private final EmpruntService empruntService;
    private final UtilisateurService utilisateurService;

    @PostMapping("/emprunter/{livreId}")
    @PreAuthorize("hasAnyRole('MEMBRE', 'ADMIN', 'BIBLIOTHECAIRE')")
    @Operation(summary = "Auto-emprunt par le membre depuis le catalogue")
    public ResponseEntity<Emprunt> emprunter(@PathVariable Long livreId) {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(empruntService.enregistrerEmpruntAuto(userDetails.id(), livreId));
    }

    @PostMapping("/guichet/prets")
    @PreAuthorize("hasAnyRole('ADMIN', 'BIBLIOTHECAIRE')")
    @Operation(summary = "Enregistrer un prêt au guichet (Staff)")
    public ResponseEntity<Emprunt> guichetPret(@RequestBody GuichetPretRequest request) {
        return ResponseEntity.ok(empruntService.enregistrerEmprunt(request.getUtilisateurId(), request.getExemplaireId()));
    }

    @PutMapping("/retourner/{empruntId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BIBLIOTHECAIRE')")
    @Operation(summary = "Enregistrer le retour d'un livre physique")
    public ResponseEntity<Emprunt> retourner(@PathVariable Long empruntId) {
        return ResponseEntity.ok(empruntService.retournerLivre(empruntId));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'BIBLIOTHECAIRE')")
    @Operation(summary = "Liste de tous les emprunts actifs")
    public ResponseEntity<List<Emprunt>> getAll() {
        return ResponseEntity.ok(empruntService.getAllEmprunts());
    }

    @GetMapping("/historique/complet")
    @PreAuthorize("hasAnyRole('ADMIN', 'BIBLIOTHECAIRE')")
    @Operation(summary = "Historique complet de tous les emprunts (actifs et passés)")
    public ResponseEntity<List<Emprunt>> getHistoriqueComplet() {
        return ResponseEntity.ok(empruntService.getAllEmpruntsHistory());
    }

    @GetMapping("/mes-emprunts")
    @PreAuthorize("hasRole('MEMBRE')")
    public ResponseEntity<List<Emprunt>> getMesEmprunts() {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<Emprunt> mesEmprunts = empruntService.getEmpruntsByUtilisateur(userDetails.id());
        return ResponseEntity.ok(mesEmprunts);
    }

    @PutMapping("/prolonger/{empruntId}")
    @PreAuthorize("hasRole('MEMBRE')")
    @Operation(summary = "Prolonger un prêt de 7 jours (Membre)")
    public ResponseEntity<Emprunt> prolongerEmprunt(@PathVariable Long empruntId) {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(empruntService.prolongerEmprunt(empruntId, userDetails.id()));
    }

    @PutMapping("/{empruntId}/retourner-avec-constat")
    @PreAuthorize("hasAnyRole('BIBLIOTHECAIRE', 'ADMIN')")
    @Operation(summary = "Retourner un livre et mettre à jour son état physique")
    public ResponseEntity<Emprunt> retournerAvecConstat(
            @PathVariable Long empruntId,
            @RequestParam Etat nouvelEtat) {
        return ResponseEntity.ok(empruntService.retournerLivreAvecConstat(empruntId, nouvelEtat));
    }

    @PutMapping("/{id}/perdu")
    @PreAuthorize("hasAnyRole('BIBLIOTHECAIRE', 'ADMIN')")
    @Operation(summary = "Déclarer un livre comme perdu par un membre")
    public ResponseEntity<Emprunt> declarerPerdu(@PathVariable Long id) {
        return ResponseEntity.ok(empruntService.declarerPerte(id));
    }

    @GetMapping("/statistiques/retards")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Tableau de bord : voir tous les livres en retard")
    public ResponseEntity<Map<String, Object>> getStatistiquesRetards() {
        return ResponseEntity.ok(empruntService.getStatistiquesRetards());
    }

    @GetMapping("/historique")
    @PreAuthorize("hasRole('MEMBRE')")
    @Operation(summary = "Voir l'historique des livres déjà lus et rendus")
    public ResponseEntity<List<Emprunt>> getHistorique() {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(empruntService.getHistoriqueMembre(userDetails.id()));
    }

    @GetMapping("/historique-global")
    @PreAuthorize("hasAnyRole('ADMIN', 'BIBLIOTHECAIRE')")
    @Operation(summary = "Recherche d'historique global pour le staff avec filtres de date")
    public ResponseEntity<List<Emprunt>> getHistoriqueGlobal(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        
        java.time.LocalDate debut = (startDate != null && !startDate.isEmpty()) ? java.time.LocalDate.parse(startDate) : java.time.LocalDate.of(2000, 1, 1);
        java.time.LocalDate fin = (endDate != null && !endDate.isEmpty()) ? java.time.LocalDate.parse(endDate) : java.time.LocalDate.now();
        
        return ResponseEntity.ok(empruntService.getHistoriqueEmpruntsBetweenDates(debut, fin));
    }
}