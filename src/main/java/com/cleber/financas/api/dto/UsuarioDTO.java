package com.cleber.financas.api.dto;

/*Não usei @Builder, @Getter, @Setter
* resolvi criar na mão o codigo*/
public class UsuarioDTO {

    private String nomeCompleto;
    private String cadastroPessoaFisica;
    private String nomeUsuario;
    private String email;
    private String senha;

    /*GETTERS AND SETTERS*/
    public String getNomeCompleto() {
        return nomeCompleto;
    }

    public UsuarioDTO setNomeCompleto(String nomeCompleto) {
        this.nomeCompleto = nomeCompleto;
        return this;
    }

    public String getCadastroPessoaFisica() {
        return cadastroPessoaFisica;
    }

    public UsuarioDTO setCadastroPessoaFisica(String cadastroPessoaFisica) {
        this.cadastroPessoaFisica = cadastroPessoaFisica;
        return this;
    }

    public String getNomeUsuario() {
        return nomeUsuario;
    }

    public UsuarioDTO setNomeusuario(String nomeUsuario) {
        this.nomeUsuario = nomeUsuario ;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public UsuarioDTO setEmail(String email) {
        this.email = email;
        return this;
    }

    public String getSenha() {
        return senha;
    }

    public UsuarioDTO setSenha(String senha) {
        this.senha = senha;
        return this;
    }

    /*BUILDER*/
    private UsuarioDTO(UsuarioDTO builder) {
        this.nomeCompleto = builder.nomeCompleto;
        this.cadastroPessoaFisica = builder.cadastroPessoaFisica;
        this.nomeUsuario = builder.nomeUsuario;
        this.email = builder.email;
        this.senha = builder.senha;
    }

    public UsuarioDTO build() {
        return new UsuarioDTO(this);
    }
}
