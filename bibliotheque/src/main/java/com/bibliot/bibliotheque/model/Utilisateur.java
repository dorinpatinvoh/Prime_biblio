package com.bibliot.bibliotheque.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "utilisateurs")
public class Utilisateur {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private String prenom;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String motDePasse;

    @Column(name = "nombre_prets_en_cours")
    private Integer nombrePretsEnCours = 0;

    @Column()
    private boolean actif = true;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role; // Devient une relation ManyToOne vers l'entité Role

    public String getMotDePasse() {
        return motDePasse;
    }

    public void setMotDePasse(String motDePasse) {
        this.motDePasse = motDePasse;
    }
}