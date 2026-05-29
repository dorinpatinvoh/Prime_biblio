package com.bibliot.bibliotheque.service;

import com.bibliot.bibliotheque.dto.LivreDTO;
import com.bibliot.bibliotheque.exception.ResourceNotFoundException;
import com.bibliot.bibliotheque.model.Exemplaire;
import com.bibliot.bibliotheque.model.Livre;
import com.bibliot.bibliotheque.repository.EmpruntRepository;
import com.bibliot.bibliotheque.repository.ExemplaireRepository;
import com.bibliot.bibliotheque.repository.LivreRepository;
import com.bibliot.bibliotheque.enums.Statut;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class LivreService {

    private final LivreRepository livreRepository;
    private final ExemplaireRepository exemplaireRepository;
    private final EmpruntRepository empruntRepository;

    public Livre createLivre(Livre livre) {
        return livreRepository.save(livre);
    }

    public List<Livre> createLivres(List<Livre> livres) {
        return livreRepository.saveAll(livres);
    }

    @Transactional(readOnly = true)
    public List<LivreDTO> getAllLivres() {
        return livreRepository.findAllProjected();
    }

    @Transactional(readOnly = true)
    public Optional<Livre> getLivreById(Long id) {
        return livreRepository.findById(id);
    }

    public Livre updateLivre(Long id, Livre livreDetails) {
        Livre livre = livreRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Livre non trouvé avec l'id : " + id));

        livre.setTitre(livreDetails.getTitre());
        livre.setAuteur(livreDetails.getAuteur());
        livre.setIsbn(livreDetails.getIsbn());
        livre.setImageUrl(livreDetails.getImageUrl());
        livre.setCategorie(livreDetails.getCategorie());
        livre.setResume(livreDetails.getResume());

        return livreRepository.save(livre);
    }

    public void deleteLivre(Long id) {
        if (!livreRepository.existsById(id)) {
            throw new ResourceNotFoundException("Livre non trouvé");
        }

        // 1. Vérifier qu'aucun exemplaire n'est actuellement prêté
        boolean empruntsEnCours = exemplaireRepository.existsByLivreIdAndStatut(id, Statut.EMPRUNTE);
        if (empruntsEnCours) {
            throw new RuntimeException("Suppression impossible : des exemplaires de ce livre sont actuellement prêtés.");
        }

        // 2. Supprimer l'historique des emprunts pour chaque exemplaire (sinon FK error)
        List<Exemplaire> exemplaires = exemplaireRepository.findByLivreId(id);
        for (Exemplaire ex : exemplaires) {
            empruntRepository.deleteByExemplaireId(ex.getId());
        }

        // 3. Supprimer les exemplaires
        exemplaireRepository.deleteByLivreId(id);

        // 4. Supprimer le livre
        livreRepository.deleteById(id);
    }

    public void deleteAllLivres() {
        livreRepository.deleteAll();
    }
}