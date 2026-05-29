package com.bibliot.bibliotheque.service;

import com.bibliot.bibliotheque.enums.StatutReservation;
import com.bibliot.bibliotheque.model.Livre;
import com.bibliot.bibliotheque.model.Reservation;
import com.bibliot.bibliotheque.model.Utilisateur;
import com.bibliot.bibliotheque.repository.LivreRepository;
import com.bibliot.bibliotheque.repository.ReservationRepository;
import com.bibliot.bibliotheque.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final LivreRepository livreRepository;
    private final EmpruntService empruntService;

    @Transactional
    public Reservation reserverLivre(Long utilisateurId, Long livreId) {
        Utilisateur utilisateur = utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        Livre livre = livreRepository.findById(livreId)
                .orElseThrow(() -> new RuntimeException("Livre non trouvé"));

        // Règle 1: L'utilisateur ne peut pas réserver un livre s'il a déjà des exemplaires disponibles
        if (livre.getNombreDisponible() > 0) {
            throw new RuntimeException("Des exemplaires sont disponibles. Veuillez emprunter le livre directement.");
        }

        // Règle 2: L'utilisateur ne doit pas avoir déjà réservé ce livre (s'il est encore en attente)
        boolean dejaReserve = reservationRepository.existsByUtilisateurIdAndLivreIdAndStatut(utilisateurId, livreId, StatutReservation.EN_ATTENTE);
        if (dejaReserve) {
            throw new RuntimeException("Vous avez déjà une réservation en attente pour ce livre.");
        }

        Reservation reservation = new Reservation();
        reservation.setUtilisateur(utilisateur);
        reservation.setLivre(livre);
        reservation.setDateReservation(LocalDate.now());
        reservation.setStatut(StatutReservation.EN_ATTENTE);

        return reservationRepository.save(reservation);
    }

    @Transactional
    public Reservation annulerReservation(Long reservationId, Long utilisateurId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Réservation introuvable"));

        if (!reservation.getUtilisateur().getId().equals(utilisateurId)) {
            throw new RuntimeException("Vous n'êtes pas autorisé à annuler cette réservation.");
        }

        reservation.setStatut(StatutReservation.ANNULEE);
        return reservationRepository.save(reservation);
    }

    @Transactional(readOnly = true)
    public List<Reservation> getMesReservations(Long utilisateurId) {
        return reservationRepository.findByUtilisateurIdWithDetails(utilisateurId);
    }

    @Transactional(readOnly = true)
    public List<Reservation> getAllReservations() {
        return reservationRepository.findAllWithDetails();
    }
}
