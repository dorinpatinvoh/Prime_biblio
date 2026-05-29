package com.bibliot.bibliotheque.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Format d'email invalide")
    private String email;

    @NotBlank(message = "Le mot de passe est obligatoire")
    private String motDePasse;

    // Ajout manuel du getter pour éviter les soucis d'IDE
    public String getMotDePasse() {
        return motDePasse;
    }
}