package com.bibliot.bibliotheque.dto;

import lombok.Data;
import java.util.List;

@Data
public class JwtResponse {
    private String token;
    private String type = "Bearer";
    private Long id;
    private String email;
    private List<String> roles;

    // Constructeur personnalisé pour envoyer les infos utiles au frontend
    public JwtResponse(String accessToken, Long id, String email, List<String> roles) {
        this.token = accessToken;
        this.id = id;
        this.email = email;
        this.roles = roles;
    }

    // Constructeur simple (si tu ne veux renvoyer que le token au début)
    public JwtResponse(String accessToken) {
        this.token = accessToken;
    }
}