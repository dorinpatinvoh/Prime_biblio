package com.bibliot.bibliotheque.repository;

import com.bibliot.bibliotheque.model.Emprunt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmpruntRepository extends JpaRepository<Emprunt, Long> {

    // --- OPTIMISATIONS (JOIN FETCH) ---
    @Query("SELECT e FROM Emprunt e " +
           "JOIN FETCH e.utilisateur " +
           "JOIN FETCH e.exemplaire ex " +
           "JOIN FETCH ex.livre " +
           "WHERE e.utilisateur.id = :userId")
    List<Emprunt> findByUtilisateurIdWithDetails(@Param("userId") Long userId);

    @Query("SELECT e FROM Emprunt e " +
           "JOIN FETCH e.utilisateur " +
           "JOIN FETCH e.exemplaire ex " +
           "JOIN FETCH ex.livre " +
           "WHERE e.dateRetourEffectif IS NULL")
    List<Emprunt> findAllActiveWithDetails();

    @Query("SELECT e FROM Emprunt e " +
           "JOIN FETCH e.utilisateur " +
           "JOIN FETCH e.exemplaire ex " +
           "JOIN FETCH ex.livre " +
           "WHERE e.dateEmprunt >= :startDate AND e.dateEmprunt <= :endDate " +
           "ORDER BY e.dateEmprunt DESC")
    List<Emprunt> findHistoryBetweenDatesWithDetails(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT e FROM Emprunt e " +
           "JOIN FETCH e.utilisateur " +
           "JOIN FETCH e.exemplaire ex " +
           "JOIN FETCH ex.livre " +
           "ORDER BY e.dateEmprunt DESC")
    List<Emprunt> findAllWithDetails();

    // --- MÉTHODES POUR LES STATS ET TACHES ---
    long countByDateRetourEffectifIsNull();
    
    long countByDateRetourBeforeAndDateRetourEffectifIsNull(LocalDate date);
    
    List<Emprunt> findByDateRetourBeforeAndDateRetourEffectifIsNull(LocalDate date);
    
    List<Emprunt> findAllByDateRetourBeforeAndDateRetourEffectifIsNull(LocalDate date);

    // --- RECHERCHES SPÉCIFIQUES ---
    // Un exemplaire physique ne peut avoir qu'UN SEUL prêt actif à la fois
    Optional<Emprunt> findByExemplaireIdAndDateRetourEffectifIsNull(Long exemplaireId);
    
    // Pour la vérification avant suppression d'un livre
    long countByExemplaireId(Long exemplaireId);
    
    List<Emprunt> findByUtilisateurIdOrderByDateEmpruntDesc(Long utilisateurId);
    
    List<Emprunt> findByUtilisateurIdAndDateRetourEffectifIsNotNullOrderByDateRetourEffectifDesc(Long utilisateurId);

    // Supprime tous les emprunts liés à un exemplaire (pour suppression en cascade d'un livre)
    void deleteByExemplaireId(Long exemplaireId);

    // Vérifie si l'utilisateur a déjà un exemplaire de ce livre non rendu
    @Query("SELECT COUNT(e) > 0 FROM Emprunt e " +
           "WHERE e.utilisateur.id = :userId " +
           "AND e.exemplaire.livre.id = :livreId " +
           "AND e.dateRetourEffectif IS NULL")
    boolean existsActiveLoanByUserAndBook(@Param("userId") Long userId, @Param("livreId") Long livreId);
}
