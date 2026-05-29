package com.bibliot.bibliotheque.controller;

import com.bibliot.bibliotheque.model.Reservation;
import com.bibliot.bibliotheque.security.services.UserDetailsImpl;
import com.bibliot.bibliotheque.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
@Tag(name = "Reservations", description = "Gestion des réservations en file d'attente")
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping("/reserver/{livreId}")
    @PreAuthorize("hasAnyRole('MEMBRE', 'ADMIN', 'BIBLIOTHECAIRE')")
    @Operation(summary = "Réserver un livre lorsqu'aucun n'est disponible")
    public ResponseEntity<Reservation> reserver(@PathVariable Long livreId) {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(reservationService.reserverLivre(userDetails.id(), livreId));
    }

    @PutMapping("/annuler/{reservationId}")
    @PreAuthorize("hasRole('MEMBRE')")
    @Operation(summary = "Annuler une réservation en attente")
    public ResponseEntity<Reservation> annuler(@PathVariable Long reservationId) {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(reservationService.annulerReservation(reservationId, userDetails.id()));
    }

    @GetMapping("/mes-reservations")
    @PreAuthorize("hasRole('MEMBRE')")
    @Operation(summary = "Voir mes réservations actuelles et passées")
    public ResponseEntity<List<Reservation>> getMesReservations() {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(reservationService.getMesReservations(userDetails.id()));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'BIBLIOTHECAIRE')")
    @Operation(summary = "Liste de toutes les réservations actives")
    public ResponseEntity<List<Reservation>> getAll() {
        return ResponseEntity.ok(reservationService.getAllReservations());
    }
}
