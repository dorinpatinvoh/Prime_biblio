package com.bibliot.bibliotheque.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class LivreDTO {
    private Long id;
    private String titre;
    private String auteur;
    private String categorie;
    private String isbn;
    private String resume;
    private String imageUrl;
    private Long nombreDisponible;

    // Constructeur manuel indispensable pour JPQL
    public LivreDTO(Long id, String titre, String auteur, String categorie, String isbn, String resume, String imageUrl, Long nombreDisponible) {
        this.id = id;
        this.titre = titre;
        this.auteur = auteur;
        this.categorie = categorie;
        this.isbn = isbn;
        this.resume = resume;
        this.imageUrl = imageUrl;
        this.nombreDisponible = nombreDisponible;
    }
}