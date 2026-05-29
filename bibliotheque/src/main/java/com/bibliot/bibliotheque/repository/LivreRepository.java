package com.bibliot.bibliotheque.repository;

import com.bibliot.bibliotheque.dto.LivreDTO;
import com.bibliot.bibliotheque.model.Livre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LivreRepository extends JpaRepository<Livre, Long> {

    @Query("SELECT new com.bibliot.bibliotheque.dto.LivreDTO(" +
            "l.id, l.titre, l.auteur, l.categorie, l.isbn, l.resume, l.imageUrl, " +
            "(SELECT COUNT(e) FROM Exemplaire e WHERE e.livre = l AND e.statut = com.bibliot.bibliotheque.enums.Statut.DISPONIBLE)) " +
            "FROM Livre l")
    List<LivreDTO> findAllProjected();

    Optional<Livre> findByIsbn(String isbn);
}