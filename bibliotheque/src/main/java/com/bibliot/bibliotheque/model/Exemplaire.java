package com.bibliot.bibliotheque.model;

import com.bibliot.bibliotheque.enums.Etat;
import com.bibliot.bibliotheque.enums.Statut;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
@Entity
@Data
@NoArgsConstructor
public class Exemplaire {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Code-barres ou identifiant physique de l'exemplaire
    @NotBlank(message = "Le code-barres ne peut pas être vide")
    @Column(name = "codebarre_id", unique = true, nullable = false)
    private String codebarreId;


    private Integer nombreDispo;
    @Enumerated(EnumType.STRING)
    private Etat etat;
    private LocalDate dateAcquisition;
    @Enumerated(EnumType.STRING)
    private Statut statut;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "livre_id", nullable = false)
    @JsonIgnoreProperties({"exemplaires", "hibernateLazyInitializer", "handler"})
    private Livre livre;

}
