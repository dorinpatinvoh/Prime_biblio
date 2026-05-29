package com.bibliot.bibliotheque.service;

import com.bibliot.bibliotheque.enums.Statut;
import com.bibliot.bibliotheque.model.Emprunt;
import com.bibliot.bibliotheque.model.Exemplaire;
import com.bibliot.bibliotheque.model.Livre;
import com.bibliot.bibliotheque.repository.EmpruntRepository;
import com.bibliot.bibliotheque.repository.ExemplaireRepository;
import com.bibliot.bibliotheque.repository.LivreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ExemplaireService {

    private final ExemplaireRepository exemplaireRepository;
    private final LivreRepository livreRepository;
    private final EmpruntRepository empruntRepository;

    // --- CREATE ---

    public Exemplaire ajouterUnExemplaire(Long livreId, Exemplaire exemplaire) {
        Livre livre = livreRepository.findById(livreId)
                .orElseThrow(() -> new RuntimeException("Livre non trouvé avec l'ID : " + livreId));

        exemplaire.setLivre(livre);
        return exemplaireRepository.save(exemplaire);
    }

    public List<Exemplaire> ajouterPlusieursExemplairesParQuantite(Long livreId, int quantite) {
        Livre livre = livreRepository.findById(livreId)
                .orElseThrow(() -> new RuntimeException("Livre non trouvé"));

        List<Exemplaire> nouveaux = new java.util.ArrayList<>();
        for (int i = 0; i < quantite; i++) {
            Exemplaire ex = new Exemplaire();
            ex.setLivre(livre);
            ex.setStatut(Statut.DISPONIBLE);
            ex.setEtat(com.bibliot.bibliotheque.enums.Etat.NEUF);
            ex.setDateAcquisition(java.time.LocalDate.now());
            // Génération d'un code barre unique simple
            ex.setCodebarreId(livre.getIsbn() + "-" + System.currentTimeMillis() + "-" + i);
            nouveaux.add(ex);
        }
        return exemplaireRepository.saveAll(nouveaux);
    }

    public List<Exemplaire> ajouterPlusieursExemplaires(Long livreId, List<Exemplaire> exemplaires) {
        Livre livre = livreRepository.findById(livreId)
                .orElseThrow(() -> new RuntimeException("Livre non trouvé avec l'ID : " + livreId));

        for (Exemplaire ex : exemplaires) {
            ex.setLivre(livre);
        }
        return exemplaireRepository.saveAll(exemplaires);
    }

    // --- READ ---

    @Transactional(readOnly = true)
    public List<Exemplaire> getAllExemplaires() {
        return exemplaireRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Exemplaire> getExemplaireById(Long id) {
        return exemplaireRepository.findById(id);
    }

    // --- UPDATE ---

    public Exemplaire modifierExemplaire(Long id, Exemplaire details) {
        Exemplaire exemplaire = exemplaireRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Exemplaire non trouvé avec l'ID : " + id));

        // Mise à jour avec tes champs réels
        exemplaire.setCodebarreId(details.getCodebarreId());
        exemplaire.setNombreDispo(details.getNombreDispo());
        exemplaire.setEtat(details.getEtat());
        exemplaire.setStatut(details.getStatut());
        exemplaire.setDateAcquisition(details.getDateAcquisition());

        return exemplaireRepository.save(exemplaire);
    }

    // --- DELETE ---

    public void supprimerExemplaire(Long id) {
        if (!exemplaireRepository.existsById(id)) {
            throw new RuntimeException("Exemplaire non trouvé");
        }
        exemplaireRepository.deleteById(id);
    }
    // --- 3. SCANNER UN CODE BARRES ---
    @Transactional(readOnly = true)
    public Map<String, Object> scannerCodeBarre(String codebarre) {
        Exemplaire exemplaire = exemplaireRepository.findByCodebarreId(codebarre)
                .orElseThrow(() -> new RuntimeException("Aucun exemplaire trouvé avec ce code-barres"));

        Map<String, Object> resultat = new HashMap<>();
        resultat.put("exemplaire", exemplaire);

        // Si le livre est actuellement emprunté, on va chercher par qui
        if (exemplaire.getStatut() == Statut.EMPRUNTE) {
            // On a besoin du EmpruntRepository ici
            Optional<Emprunt> empruntActif = empruntRepository.findByExemplaireIdAndDateRetourEffectifIsNull(exemplaire.getId());
            if (empruntActif.isPresent()) {
                resultat.put("emprunteur", empruntActif.get().getUtilisateur().getEmail());
                resultat.put("dateRetourPrevue", empruntActif.get().getDateRetour());
            }
        }
        return resultat;
    }
}