package com.bibliot.bibliotheque.repository;

import com.bibliot.bibliotheque.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByNom(String nom);
}