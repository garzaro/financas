package com.cleber.financas.api.dto;

/**
 * Não usei @Builder, @Getter, @Setter
 * resolvi criar na mão o codigo
 * 
 * Implementar Bean Validatation 
 * */
import java.time.Instant;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonAlias;

/**
 * TODO-list
 * [] - Não permitir usuario salvar senha com o proprio nome
 * **/

public class UsuarioDTO {
	private UUID id;
    
	@JsonAlias({"nome_completo", "nomeCompleto"})
	private String nomeCompleto;
    
	private String cpf;
    
    @JsonAlias({"nome_usuario", "nomeUsuario"})
    private String nomeUsuario;
    
    private String email;
    
    private String senha;
    
    private Instant dataCadastro;

    public UsuarioDTO() {

    }

    /* BUILDER */
    private UsuarioDTO(UsuarioBuilder builder) {
        this.id = builder.id;
        this.nomeCompleto = builder.nomeCompleto;
        this.cpf = builder.cpf;
        this.nomeUsuario = builder.nomeUsuario;
        this.email = builder.email;
        this.senha = builder.senha;
        this.dataCadastro = builder.dataCadastro;
    }

    public UUID getId() {
        return id;
    }

    public String getNomeCompleto() {
        return nomeCompleto;
    }

    public String getCpf() {
        return cpf;
    }

    public String getNomeUsuario() {
        return nomeUsuario;
    }

    public String getEmail() {
        return email;
    }

    public String getSenha() {
        return senha;
    }

    public Instant getDataCadastro() {
        return dataCadastro;
    }

    public static class UsuarioBuilder {
        private UUID id;
        private String nomeCompleto;
        private String cpf;
        private String nomeUsuario;
        private String email;
        private String senha;
        private Instant dataCadastro;

        public UsuarioBuilder setId(UUID id) {
            this.id = id;
            return this;
        }

        public UsuarioBuilder setNomeCompleto(String nomeCompleto) {
            this.nomeCompleto = nomeCompleto;
            return this;
        }

        public UsuarioBuilder setCpf(String cpf) {
            this.cpf = cpf;
            return this;
        }

        public UsuarioBuilder setNomeUsuario(String nomeUsuario) {
            this.nomeUsuario = nomeUsuario;
            return this;
        }

        public UsuarioBuilder setEmail(String email) {
            this.email = email;
            return this;
        }

        public UsuarioBuilder setSenha(String senha) {
            this.senha = senha;
            return this;
        }

        public UsuarioBuilder setDataCadastro(Instant dataCadastro) {
            this.dataCadastro = dataCadastro;
            return this;
        }

        public UsuarioDTO build() {
            return new UsuarioDTO(this);
        }
    }
}
