package com.bibliot.bibliotheque.service;

import com.bibliot.bibliotheque.enums.Etat;
import com.bibliot.bibliotheque.enums.Statut;
import com.bibliot.bibliotheque.model.Emprunt;
import com.bibliot.bibliotheque.model.Exemplaire;
import com.bibliot.bibliotheque.model.Utilisateur;
import com.bibliot.bibliotheque.repository.EmpruntRepository;
import com.bibliot.bibliotheque.repository.ExemplaireRepository;
import com.bibliot.bibliotheque.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bibliot.bibliotheque.repository.ReservationRepository;
import com.bibliot.bibliotheque.enums.StatutReservation;
import com.bibliot.bibliotheque.model.Reservation;

@Service
@RequiredArgsConstructor
public class EmpruntService {

    private final EmpruntRepository empruntRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final ExemplaireRepository exemplaireRepository;
    private final ReservationRepository reservationRepository;
    private static final int LIMITE_EMPRUNTS = 5; // Par exemple

    @Transactional
    public Emprunt enregistrerEmprunt(Long utilisateurId, Long exemplaireId) {
        // 1. Vérifier si l'utilisateur existe
        Utilisateur utilisateur = utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        // RÈGLE MÉTIER : Vérifier la limite
        if (utilisateur.getNombrePretsEnCours() >= LIMITE_EMPRUNTS) {
            throw new RuntimeException("Vous avez atteint la limite maximale de " + LIMITE_EMPRUNTS + " emprunts.");
        }

        // 2. Vérifier si l'exemplaire existe et est DISPONIBLE
        Exemplaire exemplaire = exemplaireRepository.findById(exemplaireId)
                .orElseThrow(() -> new RuntimeException("Exemplaire non trouvé"));

        if (exemplaire.getStatut() != Statut.DISPONIBLE) {
            throw new RuntimeException("Cet exemplaire n'est pas disponible actuellement");
        }

        // RÈGLE MÉTIER : Un abonné ne peut pas emprunter deux fois le même livre en même temps
        if (empruntRepository.existsActiveLoanByUserAndBook(utilisateurId, exemplaire.getLivre().getId())) {
            throw new RuntimeException("Vous avez déjà un exemplaire de ce livre en votre possession.");
        }

        // 3. Créer l'emprunt
        Emprunt emprunt = new Emprunt();
        emprunt.setUtilisateur(utilisateur);
        emprunt.setExemplaire(exemplaire);
        emprunt.setDateEmprunt(LocalDate.now());
        emprunt.setDateRetour(LocalDate.now().plusDays(14)); // Prêt de 14 jours par défaut
        // dateRetourEffectif reste null

        // 4. Mettre à jour le statut de l'exemplaire
        exemplaire.setStatut(Statut.EMPRUNTE);

        // 5. Incrémenter le nombre de prêts de l'utilisateur
        utilisateur.setNombrePretsEnCours(utilisateur.getNombrePretsEnCours() + 1);

        // 6. Si l'utilisateur avait une réservation en attente pour ce livre, on la clôture
        reservationRepository.findByLivreIdAndStatutOrderByDateReservationAsc(exemplaire.getLivre().getId(), StatutReservation.EN_ATTENTE)
                .stream()
                .filter(r -> r.getUtilisateur().getId().equals(utilisateurId))
                .findFirst()
                .ifPresent(r -> {
                    r.setStatut(StatutReservation.TERMINEE);
                    reservationRepository.save(r);
                });

        exemplaireRepository.save(exemplaire);
        utilisateurRepository.save(utilisateur);
        return empruntRepository.save(emprunt);
    }

    @Transactional
    public Emprunt enregistrerEmpruntAuto(Long utilisateurId, Long livreId) {
        Exemplaire exemplaire = exemplaireRepository.findByLivreId(livreId).stream()
                .filter(e -> e.getStatut() == Statut.DISPONIBLE)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Aucun exemplaire disponible pour ce livre."));
        
        return enregistrerEmprunt(utilisateurId, exemplaire.getId());
    }

    @Transactional(readOnly = true)
    public List<Emprunt> getAllEmpruntsHistory() {
        return empruntRepository.findAllWithDetails();
    }

    @Transactional
    public Emprunt retournerLivre(Long empruntId) {
        Emprunt emprunt = empruntRepository.findById(empruntId)
                .orElseThrow(() -> new RuntimeException("Emprunt non trouvé"));

        if (emprunt.getDateRetourEffectif() != null) {
            throw new RuntimeException("Ce livre a déjà été retourné");
        }

        // 1. Marquer la date de retour
        emprunt.setDateRetourEffectif(LocalDate.now());

        // 2. Rendre l'exemplaire disponible
        Exemplaire exemplaire = emprunt.getExemplaire();
        exemplaire.setStatut(Statut.DISPONIBLE);

        // 3. Décrémenter le nombre de prêts de l'utilisateur
        Utilisateur utilisateur = emprunt.getUtilisateur();
        utilisateur.setNombrePretsEnCours(Math.max(0, utilisateur.getNombrePretsEnCours() - 1));

        exemplaireRepository.save(exemplaire);
        utilisateurRepository.save(utilisateur);
        return empruntRepository.save(emprunt);
    }

    public List<Emprunt> getAllEmprunts() {
        return empruntRepository.findAllActiveWithDetails();
    }

    @Transactional(readOnly = true)
    public List<Emprunt> getHistoriqueEmpruntsBetweenDates(LocalDate startDate, LocalDate endDate) {
        return empruntRepository.findHistoryBetweenDatesWithDetails(startDate, endDate);
    }


    /**
     * Récupère l'historique complet des emprunts d'un utilisateur spécifique.
     */
    @Transactional(readOnly = true)
    public List<Emprunt> getEmpruntsByUtilisateur(Long utilisateurId) {
        // Optionnel : Vérifier si l'utilisateur existe
        if (!utilisateurRepository.existsById(utilisateurId)) {
            throw new RuntimeException("Utilisateur non trouvé avec l'ID : " + utilisateurId);
        }

        // On retourne la liste optimisée avec JOIN FETCH
        return empruntRepository.findByUtilisateurIdWithDetails(utilisateurId);
    }

    @Transactional
    public Emprunt prolongerEmprunt(Long empruntId, Long utilisateurId) {
        Emprunt emprunt = empruntRepository.findById(empruntId)
                .orElseThrow(() -> new RuntimeException("Emprunt non trouvé"));

        // 1. Vérifier que c'est bien son emprunt
        if (!emprunt.getUtilisateur().getId().equals(utilisateurId)) {
            throw new RuntimeException("Vous ne pouvez pas prolonger un emprunt qui ne vous appartient pas.");
        }

        // 2. Vérifier que le livre n'est pas déjà rendu
        if (emprunt.getDateRetourEffectif() != null) {
            throw new RuntimeException("Ce livre a déjà été retourné.");
        }

        // 3. Vérifier qu'il n'est pas déjà en retard
        if (LocalDate.now().isAfter(emprunt.getDateRetour())) {
            throw new RuntimeException("Impossible de prolonger : vous êtes déjà en retard !");
        }

        // 4. Prolonger de 7 jours
        emprunt.setDateRetour(emprunt.getDateRetour().plusDays(7));
        return empruntRepository.save(emprunt);
    }

    @Transactional
    public Emprunt retournerLivreAvecConstat(Long empruntId, Etat nouvelEtat) {
        Emprunt emprunt = empruntRepository.findById(empruntId)
                .orElseThrow(() -> new RuntimeException("Emprunt non trouvé"));

        if (emprunt.getDateRetourEffectif() != null) {
            throw new RuntimeException("Ce livre a déjà été retourné");
        }

        emprunt.setDateRetourEffectif(LocalDate.now());

        Exemplaire exemplaire = emprunt.getExemplaire();
        exemplaire.setEtat(nouvelEtat); // Mise à jour de l'état (BON, MOYEN, MAUVAIS)

        // Si l'état est MAUVAIS, on ne le remet pas DISPONIBLE pour éviter de le prêter à nouveau
        if (nouvelEtat == Etat.MAUVAIS) {
            exemplaire.setStatut(Statut.RESERVER); // Ou tu pourrais créer un Statut.EN_REPARATION
        } else {
            exemplaire.setStatut(Statut.DISPONIBLE);
        }

        Utilisateur utilisateur = emprunt.getUtilisateur();
        utilisateur.setNombrePretsEnCours(Math.max(0, utilisateur.getNombrePretsEnCours() - 1));

        exemplaireRepository.save(exemplaire);
        utilisateurRepository.save(utilisateur);
        return empruntRepository.save(emprunt);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getStatistiquesRetards() {
        LocalDate aujourdhui = LocalDate.now();

        long nombreRetards = empruntRepository.countByDateRetourBeforeAndDateRetourEffectifIsNull(aujourdhui);
        List<Emprunt> listeRetards = empruntRepository.findByDateRetourBeforeAndDateRetourEffectifIsNull(aujourdhui);

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalEnRetard", nombreRetards);
        stats.put("listeRetards", listeRetards);

        return stats;
    }

    @Transactional(readOnly = true)
    public List<Emprunt> getHistoriqueMembre(Long utilisateurId) {
        return empruntRepository.findByUtilisateurIdAndDateRetourEffectifIsNotNullOrderByDateRetourEffectifDesc(utilisateurId);
    }

    // --- 1. DÉCLARER UN LIVRE PERDU ---
    @Transactional
    public Emprunt declarerPerte(Long empruntId) {
        Emprunt emprunt = empruntRepository.findById(empruntId)
                .orElseThrow(() -> new RuntimeException("Emprunt non trouvé"));

        if (emprunt.getDateRetourEffectif() != null) {
            throw new RuntimeException("Ce livre a déjà été retourné, il ne peut pas être déclaré perdu par ce client.");
        }

        // 1. Clôturer le prêt (pour arrêter de compter les jours de retard)
        emprunt.setDateRetourEffectif(LocalDate.now());

        // 2. Marquer l'exemplaire physique comme PERDU
        Exemplaire exemplaire = emprunt.getExemplaire();
        exemplaire.setStatut(Statut.PERDU);
        // L'état physique n'a plus d'importance, mais tu peux le mettre à MAUVAIS si tu veux.

        // 3. Libérer la place pour le client (optionnel: tu pourrais aussi bloquer son compte)
        Utilisateur utilisateur = emprunt.getUtilisateur();
        utilisateur.setNombrePretsEnCours(Math.max(0, utilisateur.getNombrePretsEnCours() - 1));

        exemplaireRepository.save(exemplaire);
        utilisateurRepository.save(utilisateur);
        return empruntRepository.save(emprunt);
    }

    // --- 2. LISTE DES RETARDS POUR RELANCES ---
    @Transactional(readOnly = true)
    public List<Emprunt> getListeRetards() {
        return empruntRepository.findByDateRetourBeforeAndDateRetourEffectifIsNull(LocalDate.now());
    }

}