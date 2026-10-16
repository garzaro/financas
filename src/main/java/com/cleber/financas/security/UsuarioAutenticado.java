package com.cleber.financas.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;
import java.util.UUID;

public class UsuarioAutenticado extends User {

    private final UUID usuarioUuid;

    public UsuarioAutenticado(UUID usuarioUuid, String username, String password, Collection<? extends GrantedAuthority> authorities) {
        super(username, password, authorities);
        this.usuarioUuid = usuarioUuid;
    }

    public UsuarioAutenticado(UUID usuarioUuid, String username, String password, boolean enabled, Collection<? extends GrantedAuthority> authorities) {
        super(username, password, enabled, true, true, true, authorities);
        this.usuarioUuid = usuarioUuid;
    }

    public UUID getUsuarioUuid() {
        return usuarioUuid;
    }
}
