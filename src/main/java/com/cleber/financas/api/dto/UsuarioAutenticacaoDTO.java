package com.cleber.financas.api.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

//@Builder
//@Getter
//@Setter
public class UsuarioAutenticacaoDTO {
    private String email;
    private String senha;

    /*BUILDER*/
    private UsuarioAutenticacaoDTO(UsuarioBuilder builder) {
        this.email = builder.email;
        this.senha = builder.senha;
    }

    public String getEmail() {
        return email;
    }

    public String getSenha() {
        return senha;
    }

    public static class UsuarioBuilder{
        private String email;
        private String senha;

        public UsuarioBuilder setEmail(String email) {
            this.email = email;
            return this;
        }

        public UsuarioBuilder setSenha(String senha){
            this.senha = senha;
            return this;
        }

        public UsuarioAutenticacaoDTO build() {
            return new UsuarioAutenticacaoDTO(this);
        }
    }
}
