package com.bibliot.bibliotheque.model;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "emprunts")
@EntityListeners(AuditingEntityListener.class)
public class Emprunt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "date_emprunt")
    private LocalDate dateEmprunt;

    @Column(name = "date_retour_prevue")
    private LocalDate dateRetour;

    @Column(name = "date_retour_effectif")
    private LocalDate dateRetourEffectif;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    // Relation vers Utilisateur (0,N — 1,1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilisateur_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Utilisateur utilisateur;

    // Relation vers Exemplaire (0,N — 1,1)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exemplaire_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Exemplaire exemplaire;
}
