package com.bibliot.bibliotheque.repository;

import com.bibliot.bibliotheque.enums.StatutReservation;
import com.bibliot.bibliotheque.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    
    @Query("SELECT r FROM Reservation r JOIN FETCH r.utilisateur JOIN FETCH r.livre WHERE r.utilisateur.id = :userId ORDER BY r.dateReservation DESC")
    List<Reservation> findByUtilisateurIdWithDetails(@Param("userId") Long userId);
    
    @Query("SELECT r FROM Reservation r JOIN FETCH r.utilisateur JOIN FETCH r.livre WHERE r.livre.id = :livreId AND r.statut = :statut ORDER BY r.dateReservation ASC")
    List<Reservation> findByLivreIdAndStatutOrderByDateReservationAsc(@Param("livreId") Long livreId, @Param("statut") StatutReservation statut);

    @Query("SELECT r FROM Reservation r JOIN FETCH r.utilisateur JOIN FETCH r.livre ORDER BY r.dateReservation DESC")
    List<Reservation> findAllWithDetails();

    boolean existsByUtilisateurIdAndLivreIdAndStatut(Long utilisateurId, Long livreId, StatutReservation statut);
}
