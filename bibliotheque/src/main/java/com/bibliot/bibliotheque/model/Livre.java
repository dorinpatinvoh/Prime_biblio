package com.bibliot.bibliotheque.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "livres")
public class Livre {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column()
    private String auteur;

    @Column()
    private String categorie;

    @Column()
    private String titre;

    @Column(unique = true)
    private String isbn;

    @Column(columnDefinition = "TEXT")
    private String resume;

    @Column()
    private String imageUrl;

    public int getNombreDisponible(){
        return (exemplaires != null) ? exemplaires.size() : 0;
    }

    @OneToMany(mappedBy = "livre", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    @JsonManagedReference // "Je suis le parent, je gère la liste"
    private List<Exemplaire> exemplaires = new ArrayList<>();
}
