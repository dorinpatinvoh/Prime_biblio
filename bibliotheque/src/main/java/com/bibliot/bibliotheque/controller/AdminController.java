package com.bibliot.bibliotheque.controller;

import com.bibliot.bibliotheque.model.Role;
import com.bibliot.bibliotheque.model.Permission;
import com.bibliot.bibliotheque.repository.RoleRepository;
import com.bibliot.bibliotheque.repository.PermissionRepository;
import com.bibliot.bibliotheque.service.StatistiqueService; // 👈 1. Ajoute l'import du service
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map; // 👈 2. Ajoute l'import de Map
import java.util.Set;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final StatistiqueService statistiqueService; // 👈 3. Injecte le service ici

    // 🔴 4. AJOUTE LA ROUTE ATTENDUE PAR ANGULAR ICI
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        return ResponseEntity.ok(statistiqueService.getDashboardStats());
    }

    @GetMapping("/roles")
    public ResponseEntity<List<Role>> getAllRoles() {
        return ResponseEntity.ok(roleRepository.findAll());
    }

    @PostMapping("/roles")
    public ResponseEntity<Role> createRole(@RequestBody Role role) {
        return ResponseEntity.ok(roleRepository.save(role));
    }

    @GetMapping("/permissions")
    public ResponseEntity<List<Permission>> getAllPermissions() {
        return ResponseEntity.ok(permissionRepository.findAll());
    }

    @PutMapping("/roles/{roleId}/permissions")
    public ResponseEntity<Role> updateRolePermissions(
            @PathVariable Long roleId, 
            @RequestBody Set<Permission> permissions) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Rôle non trouvé"));
        role.setPermissions(permissions);
        return ResponseEntity.ok(roleRepository.save(role));
    }
}