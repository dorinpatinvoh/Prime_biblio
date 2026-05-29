package com.bibliot.bibliotheque.controller;

import com.bibliot.bibliotheque.dto.JwtResponse;
import com.bibliot.bibliotheque.dto.LoginRequest;
import com.bibliot.bibliotheque.model.Role;
import com.bibliot.bibliotheque.model.Utilisateur;
import com.bibliot.bibliotheque.repository.RoleRepository;
import com.bibliot.bibliotheque.repository.UtilisateurRepository;
import com.bibliot.bibliotheque.security.jwt.JwtUtils;
import com.bibliot.bibliotheque.security.services.UserDetailsImpl;
import com.bibliot.bibliotheque.service.UtilisateurService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/utilisateurs")
@RequiredArgsConstructor
@Tag(name = "Utilisateurs", description = "Gestion des comptes et authentification")
public class UtilisateurController {

    private final UtilisateurService utilisateurService;
    private final UtilisateurRepository utilisateurRepository;
    private final RoleRepository roleRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    @PostMapping("/login")
    @Operation(summary = "Se connecter")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getMotDePasse()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .toList();

        return ResponseEntity.ok(new JwtResponse(jwt, userDetails.id(), userDetails.getUsername(), roles));
    }

    @PostMapping("/register")
    @Operation(summary = "S'inscrire (Client uniquement)")
    public ResponseEntity<Utilisateur> register(@RequestBody Utilisateur utilisateur) {
        Role membreRole = roleRepository.findByNom("MEMBRE")
                .orElseThrow(() -> new RuntimeException("Le rôle MEMBRE n'est pas configuré en base de données"));
        
        utilisateur.setRole(membreRole);
        utilisateur.setActif(true);
        utilisateur.setNombrePretsEnCours(0);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(utilisateurService.sauvegarderUtilisateur(utilisateur));
    }

    @GetMapping
    public List<Utilisateur> findAll() {
        return utilisateurService.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Utilisateur> findById(@PathVariable Long id) {
        return ResponseEntity.ok(utilisateurService.getById(id));
    }

   @GetMapping("/me")
    public ResponseEntity<Map<String, String>> getMe(Principal principal) {
        if (principal == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        Utilisateur user = utilisateurRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        Map<String, String> userInfo = new HashMap<>();
        userInfo.put("email", user.getEmail());
        userInfo.put("role", user.getRole().getNom()); 
        userInfo.put("nom", user.getNom());
        userInfo.put("id", user.getId().toString());

        return ResponseEntity.ok(userInfo);
    }

    // 1. On garde cette méthode pour les requêtes POST /api/utilisateurs standard
    @PostMapping
    public ResponseEntity<Utilisateur> create(@RequestBody Utilisateur utilisateur) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(utilisateurService.sauvegarderUtilisateur(utilisateur));
    }

    // 2. MODIFICATION ICI : On ajoute un "path" unique à l'annotation 🟢
    @PostMapping("/admin-create") // La route devient : POST /api/utilisateurs/admin-create
    @PreAuthorize("hasRole('ADMIN')") 
    public ResponseEntity<?> creerUtilisateurParAdmin(@RequestBody Utilisateur nouvelUtilisateur) {
        try {
            Role roleAAssocier = nouvelUtilisateur.getRole(); 
            Utilisateur cree = utilisateurService.creerEmploye(nouvelUtilisateur, roleAAssocier);
            return ResponseEntity.status(HttpStatus.CREATED).body(cree);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}