package com.bibliot.bibliotheque.security.jwt;

import com.bibliot.bibliotheque.security.services.UserDetailsImpl;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtils {
    private final String jwtSecret = "votre_cle_secrete_tres_longue_qui_doit_faire_plus_de_32_caracteres";
    private final int jwtExpirationMs = 86400000;

    private Key key() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    public String generateJwtToken(Authentication authentication) {
    UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();
    
    // On récupère le rôle (ex: ROLE_ADMIN)
    String role = userPrincipal.getAuthorities().stream()
            .map(item -> item.getAuthority())
            .findFirst().orElse("ROLE_MEMBRE");

    return Jwts.builder()
            .setSubject(userPrincipal.getUsername())
            .claim("role", role) // <--- AJOUTE CETTE LIGNE
            .setIssuedAt(new Date())
            .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
            .signWith(key(), SignatureAlgorithm.HS256)
            .compact();
}

    public String getUserNameFromJwtToken(String token) {
        return Jwts.parserBuilder().setSigningKey(key()).build()
                .parseClaimsJws(token).getBody().getSubject();
    }

    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parserBuilder().setSigningKey(key()).build().parseClaimsJws(authToken);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }
}