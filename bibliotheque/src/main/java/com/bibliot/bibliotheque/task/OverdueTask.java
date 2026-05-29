package com.bibliot.bibliotheque.task;

import com.bibliot.bibliotheque.model.Emprunt;
import com.bibliot.bibliotheque.repository.EmpruntRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OverdueTask {

    private final EmpruntRepository empruntRepository;

    // Se lance tous les jours à minuit
    @Scheduled(cron = "0 0 0 * * *")
    public void checkOverdueLoans() {
        LocalDate today = LocalDate.now();

        // On cherche les emprunts non rendus dont la date de retour est dépassée
        List<Emprunt> retards = empruntRepository. findAllByDateRetourBeforeAndDateRetourEffectifIsNull(today);

        for (Emprunt e : retards) {
            // Ici tu peux :
            // 1. Envoyer un mail automatique
            // 2. Changer le statut de l'emprunt ou de l'utilisateur
            log.warn("ALERTE : L'utilisateur {} est en retard pour le livre {}", 
                    e.getUtilisateur().getEmail(), e.getExemplaire().getLivre().getTitre());
        }
    }
}