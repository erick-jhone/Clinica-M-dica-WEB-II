package com.ifto.clinica.model.security;

import com.ifto.clinica.model.entity.Usuario;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

public class UsuarioDetails implements UserDetails {

    private final Usuario usuario;

    public UsuarioDetails(Usuario usuario) {
        this.usuario = usuario;
    }

    public Usuario getUsuario() {
        return usuario;
    }
    public String getPaciente() {
        return getPaciente();
    }

    public String getNomeUsuario() {
        if (usuario != null && usuario.getPaciente() != null) {
            return usuario.getPaciente().getNome();
        }
        return usuario != null ? usuario.getPaciente().getNome() : "Visitante";
    }
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return usuario.getAuthorities();
    }

    @Override
    public String getPassword() {
        return usuario.getPassword();
    }

    @Override
    public String getUsername() {
        return usuario.getLogin();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
