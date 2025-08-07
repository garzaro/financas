package com.cleber.financas.api.dto;

/*Não usei @Builder, @Getter, @Setter
* resolvi criar na mão o codigo*/

import java.time.LocalDate;

public class UsuarioDTO {
    private Long id;
    private String nome;   
    private String cpf;
    private String usuario;
    private String email;
    private String senha;
    private LocalDate dataCadastro;

    public UsuarioDTO() {
		
	}
    
    /*BUILDER*/
    private UsuarioDTO(UsuarioBuilder builder) {
        this.id = builder.id;
        this.nome = builder.nome;
        this.cpf = builder.cpf;
        this.usuario = builder.usuario;
        this.email = builder.email;
        this.senha = builder.senha;
        this.dataCadastro = builder.build().dataCadastro;
    }
    public Long getId(){return id;}

    public String getNome() {
        return nome;
    }

    public String getCpf(){
        return cpf;
    }

    public String getUsuario(){
        return usuario;
    }

    public String getEmail() {
        return email;
    }

    public String getSenha() {
        return senha;
    }

    public LocalDate getDataCadastro(){
        return dataCadastro;
    }

    public static class UsuarioBuilder{
        private Long id;
        private String nome;
        private String cpf;
        private String usuario;
        private String email;
        private String senha;
        private LocalDate dataCadastro;

        public UsuarioBuilder setId(Long id) {
            this.id = id;
            return this;
        }

        public UsuarioBuilder setNome(String nome) {
        	this.nome = nome;
        	return this;
        }

        public UsuarioBuilder setCpf(String cpf) {
            this.cpf = cpf;
            return this;
        }

        public UsuarioBuilder setUsuario(String usuario) {
            this.usuario = usuario;
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

        public UsuarioBuilder setDataCadastro(LocalDate dataCadastro){
            this.dataCadastro = dataCadastro;
            return this;
        }

        public UsuarioDTO build() {
            return new UsuarioDTO(this);
        }
    }
}
