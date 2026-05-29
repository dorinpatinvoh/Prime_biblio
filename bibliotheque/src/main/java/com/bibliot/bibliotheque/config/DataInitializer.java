package com.bibliot.bibliotheque.config;

import com.bibliot.bibliotheque.model.Permission;
import com.bibliot.bibliotheque.model.Role;
import com.bibliot.bibliotheque.model.Utilisateur;
import com.bibliot.bibliotheque.repository.PermissionRepository;
import com.bibliot.bibliotheque.repository.RoleRepository;
import com.bibliot.bibliotheque.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UtilisateurRepository utilisateurRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    @Transactional // Garantit qu'Hibernate garde les entités synchronisées (évite l'erreur Transient)
    public void run(String... args) {
        
        // 1. Initialisation des Permissions individuellement
        ensurePermission("GERER_UTILISATEURS", "Ajouter, modifier ou désactiver des utilisateurs");
        ensurePermission("GERER_LIVRES", "Ajouter et modifier les livres");
        ensurePermission("EMPRUNTER", "Autorisation d'emprunter des livres");

        Permission gererUtilisateurs = permissionRepository.findByNom("GERER_UTILISATEURS").orElse(null);
        Permission gererLivres = permissionRepository.findByNom("GERER_LIVRES").orElse(null);
        Permission emprunter = permissionRepository.findByNom("EMPRUNTER").orElse(null);

        // 2. Initialisation des Rôles individuellement
        if (roleRepository.findByNom("ADMIN").isEmpty()) {
            Set<Permission> adminPermissions = new HashSet<>();
            if (gererUtilisateurs != null) adminPermissions.add(gererUtilisateurs);
            if (gererLivres != null) adminPermissions.add(gererLivres);
            roleRepository.save(new Role(null, "ADMIN", adminPermissions));
        }

        if (roleRepository.findByNom("BIBLIOTHECAIRE").isEmpty()) {
            Set<Permission> biblioPermissions = new HashSet<>();
            if (gererLivres != null) biblioPermissions.add(gererLivres);
            roleRepository.save(new Role(null, "BIBLIOTHECAIRE", biblioPermissions));
        }

        if (roleRepository.findByNom("MEMBRE").isEmpty()) {
            Set<Permission> membrePermissions = new HashSet<>();
            if (emprunter != null) membrePermissions.add(emprunter);
            roleRepository.save(new Role(null, "MEMBRE", membrePermissions));
        }

        // Récupération sécurisée des rôles pour les comptes d'usine
        Role adminRole = roleRepository.findByNom("ADMIN").orElseThrow(() -> new RuntimeException("Rôle ADMIN introuvable"));
        Role biblioRole = roleRepository.findByNom("BIBLIOTHECAIRE").orElseThrow(() -> new RuntimeException("Rôle BIBLIOTHECAIRE introuvable"));
        Role membreRole = roleRepository.findByNom("MEMBRE").orElseThrow(() -> new RuntimeException("Rôle MEMBRE introuvable"));

        // 3. Création des comptes par défaut
        if (utilisateurRepository.findByEmail("admin@biblio.com").isEmpty()) {
            Utilisateur admin = new Utilisateur();
            admin.setNom("Admin");
            admin.setPrenom("System");
            admin.setEmail("admin@biblio.com");
            admin.setMotDePasse(passwordEncoder.encode("admin123"));
            admin.setRole(adminRole);
            admin.setActif(true);
            utilisateurRepository.save(admin);
        }

        if (utilisateurRepository.findByEmail("biblio@biblio.com").isEmpty()) {
            Utilisateur biblio = new Utilisateur();
            biblio.setNom("Martin");
            biblio.setPrenom("Sophie");
            biblio.setEmail("biblio@biblio.com");
            biblio.setMotDePasse(passwordEncoder.encode("biblio123"));
            biblio.setRole(biblioRole);
            biblio.setActif(true);
            utilisateurRepository.save(biblio);
        }

        if (utilisateurRepository.findByEmail("membre@biblio.com").isEmpty()) {
            Utilisateur membre = new Utilisateur();
            membre.setNom("Dupont");
            membre.setPrenom("Jean");
            membre.setEmail("membre@biblio.com");
            membre.setMotDePasse(passwordEncoder.encode("membre123"));
            membre.setRole(membreRole);
            membre.setActif(true);
            utilisateurRepository.save(membre);
        }

        System.out.println(">>> [SUCCÈS] Base de données prête : Comptes et privilèges disponibles.");
    }

    private void ensurePermission(String nom, String description) {
        if (permissionRepository.findByNom(nom).isEmpty()) {
            permissionRepository.save(new Permission(null, nom, description));
        }
    }
}