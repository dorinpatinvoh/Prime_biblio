package com.bibliot.bibliotheque.service;

import com.bibliot.bibliotheque.repository.EmpruntRepository;
import com.bibliot.bibliotheque.repository.ExemplaireRepository;
import com.bibliot.bibliotheque.repository.LivreRepository;
import com.bibliot.bibliotheque.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StatistiqueService {

    private final LivreRepository livreRepository;
    private final ExemplaireRepository exemplaireRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final EmpruntRepository empruntRepository;

    @Transactional(readOnly = true)
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("totalLivresAuCatalogue", livreRepository.count());
        stats.put("totalExemplairesPhysiques", exemplaireRepository.count());
        stats.put("totalMembresInscrits", utilisateurRepository.count()); // Tu peux filtrer par rôle si besoin

        stats.put("livresActuellementEmpruntes", empruntRepository.countByDateRetourEffectifIsNull());
        stats.put("livresEnRetard", empruntRepository.countByDateRetourBeforeAndDateRetourEffectifIsNull(LocalDate.now()));

        return stats;
    }
}