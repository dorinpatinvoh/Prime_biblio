package com.bibliot.bibliotheque.security;

import com.bibliot.bibliotheque.security.jwt.AuthTokenFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import java.util.List;

@Configuration
@EnableMethodSecurity // Permet d'utiliser @PreAuthorize dans tes contrôleurs
@RequiredArgsConstructor
@EnableWebSecurity
public class WebSecurityConfig {

    private final AuthTokenFilter authTokenFilter;

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        // 1. CONFIGURATION CORS (Pour accepter Angular sur le port 4200)
        http.cors(cors -> cors.configurationSource(request -> {
                    CorsConfiguration config = new CorsConfiguration();
                    config.setAllowedOrigins(List.of( "http://localhost:4200"));
                    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                    config.setAllowedHeaders(List.of("*"));
                    return config;
                }))

                // 2. DESACTIVATION CSRF (Car on utilise JWT)
                .csrf(csrf -> csrf.disable())

                // 3. PAS DE SESSION SERVEUR
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 4. REGLES D'ACCES AUX ROUTES
                .authorizeHttpRequests(auth ->
                        auth
                                // LIGNE MAGIQUE POUR ANGULAR : Laisse passer les requêtes de pré-vérification
                                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                                // LES ROUTES PUBLIQUES (Tout le monde y a accès)
                                .requestMatchers(
                                        HttpMethod.GET, "/api/livres/**", // Catalogue public
                                        "/api/uploads/**"                      // Images publiques
                                ).permitAll()
                                .requestMatchers(
                                        "/api/utilisateurs/login",    // Connexion
                                        "/api/utilisateurs/register", // Inscription
                                        "/v3/api-docs",
                                        "/v3/api-docs/**",
                                        "/swagger-ui.html",
                                        "/swagger-ui/**",
                                        "/swagger-ui/index.html",
                                        "/webjars/**"
                                ).permitAll()

                                // TOUT LE RESTE EST BLOQUE (Demande un Token JWT valide)
                                .anyRequest().authenticated()
                );

        // 5. AJOUT DE NOTRE FILTRE JWT
        http.addFilterBefore(authTokenFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}