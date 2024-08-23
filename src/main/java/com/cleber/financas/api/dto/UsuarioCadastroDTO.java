package com.cleber.financas.api.dto;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/*Não usei @Builder, @Getter, @Setter
* resolvi criar na mão o codigo*/
@Getter
@Setter
@Builder
public class UsuarioCadastroDTO {
    
    private String nomeCompleto;
    private String cadastroPessoaFisica;
    private String nomeUsuario;
    private String email;
    private String senha;
    private LocalDate dataCadastro;
    
    /*BUILDER
    private UsuarioCadastroDTO(UsuarioBuilder builder) {
        
        this.nomeCompleto = builder.nomeCompleto;
        this.cadastroPessoaFisica = builder.cadastroPessoaFisica;
        this.nomeUsuario = builder.nomeUsuario;
        this.email = builder.email;
        this.senha = builder.senha;
        this.dataCadastro = builder.dataCadastro;
    }

    public String getNomeCompleto() {
        return nomeCompleto;
    }

    public String getCadastroPessoaFisica(){
        return cadastroPessoaFisica;
    }

    public String getNomeUsuario(){
        return nomeUsuario;
    }

    public String getEmail() {
        return email;
    }

    public String getSenha() {
        return senha;
    }

    public LocalDate getDataCadastro() {
        return dataCadastro;
    }

    public static class UsuarioBuilder{
        private String nomeCompleto;
        private String cadastroPessoaFisica;
        private String nomeUsuario;
        private String email;
        private String senha;
        private LocalDate dataCadastro;
        
        public UsuarioBuilder setNomeCompleto(String nomeCompleto) {
        this.nomeCompleto = nomeCompleto;
        return this;
        }

        public UsuarioBuilder setCadastroPessoaFisica(String cadastroPessoaFisica) {
            this.cadastroPessoaFisica = cadastroPessoaFisica;
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

        public UsuarioBuilder setSenha(String senha){
            this.senha = senha;
            return this;
        }

        public UsuarioBuilder setDataCadastro(LocalDate dataCadastro) {
            this.dataCadastro = dataCadastro;
            return this;
        }

        public UsuarioCadastroDTO build() {
            return new UsuarioCadastroDTO(this);
        }
    }*/
}
