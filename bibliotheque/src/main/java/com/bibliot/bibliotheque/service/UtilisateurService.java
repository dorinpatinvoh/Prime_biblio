package com.bibliot.bibliotheque.service;

import com.bibliot.bibliotheque.model.Role;
import com.bibliot.bibliotheque.model.Utilisateur;
import com.bibliot.bibliotheque.repository.UtilisateurRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UtilisateurService {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;

    public Utilisateur sauvegarderUtilisateur(Utilisateur utilisateur) {
        // 1. Éviter les doublons d'email qui font crasher Hibernate et génèrent des 403/500
        if (utilisateur.getId() == null && utilisateurRepository.findByEmail(utilisateur.getEmail()).isPresent()) {
            throw new RuntimeException("Cet email est déjà utilisé par un autre compte");
        }

        // 2. Sécuriser l'encodage : on n'encode que si ce n'est pas déjà un hash BCrypt
        // Les hash BCrypt commencent toujours par "$2a$" ou "$2b$"
        if (!utilisateur.getMotDePasse().startsWith("$2a$") && !utilisateur.getMotDePasse().startsWith("$2b$")) {
            utilisateur.setMotDePasse(passwordEncoder.encode(utilisateur.getMotDePasse()));
        }
        
        return utilisateurRepository.save(utilisateur);
    }

    public List<Utilisateur> getAll() {
        return utilisateurRepository.findAll();
    }

    public void supprimer(Long id) {
        utilisateurRepository.deleteById(id);
    }

    @Transactional
    public Utilisateur changerStatutCompte(Long utilisateurId, boolean statutActif) {
        Utilisateur user = utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        user.setActif(statutActif);
        return utilisateurRepository.save(user);
    }

    @Transactional
    public Utilisateur changerRole(Long utilisateurId, Role nouveauRole) {
        Utilisateur user = utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        user.setRole(nouveauRole);
        return utilisateurRepository.save(user);
    }

    @Transactional
    public Utilisateur creerEmploye(Utilisateur employe, Role role) {
        if (utilisateurRepository.findByEmail(employe.getEmail()).isPresent()) {
            throw new RuntimeException("Cet email est déjà utilisé");
        }

        employe.setRole(role);
        employe.setMotDePasse(passwordEncoder.encode(employe.getMotDePasse()));

        return utilisateurRepository.save(employe);
    }

    public Utilisateur getById(Long id) {
       return utilisateurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé")); 
    }
}