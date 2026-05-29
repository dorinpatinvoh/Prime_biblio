package com.bibliot.bibliotheque.security.services;

import com.bibliot.bibliotheque.model.Utilisateur;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public record UserDetailsImpl(Long id, String email, String password,
                              Collection<? extends GrantedAuthority> authorities) implements UserDetails {

    public static UserDetailsImpl build(Utilisateur user) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        
        // Ajout du rôle préfixé par ROLE_
        authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().getNom()));

        // Ajout dynamique de l'ensemble des permissions rattachées à ce rôle
        if (user.getRole().getPermissions() != null) {
            user.getRole().getPermissions().forEach(permission -> {
                authorities.add(new SimpleGrantedAuthority(permission.getNom()));
            });
        }

        return new UserDetailsImpl(
                user.getId(),
                user.getEmail(),
                user.getMotDePasse(),
                authorities);
    }

    @Override public String getUsername() { return email; }
    @Override public String getPassword() { return password; }
    @Override public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}