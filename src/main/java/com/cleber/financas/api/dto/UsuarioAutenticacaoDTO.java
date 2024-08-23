package com.cleber.financas.api.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

public class UsuarioAutenticacaoDTO {
    private String email;
    private String senha;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }
}
