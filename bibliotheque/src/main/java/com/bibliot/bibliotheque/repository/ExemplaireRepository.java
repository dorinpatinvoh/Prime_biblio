package com.bibliot.bibliotheque.repository;

import com.bibliot.bibliotheque.model.Exemplaire;
import com.bibliot.bibliotheque.enums.Statut;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExemplaireRepository extends JpaRepository<Exemplaire, Long> {
    List<Exemplaire> findByLivreId(Long livreId);
    
    Optional<Exemplaire> findByCodebarreId(String codebarreId);

    // Vérifie si un exemplaire est emprunté pour un livre donné
    boolean existsByLivreIdAndStatut(Long livreId, Statut statut);

    // Supprime tous les exemplaires d'un livre
    void deleteByLivreId(Long livreId);
}
