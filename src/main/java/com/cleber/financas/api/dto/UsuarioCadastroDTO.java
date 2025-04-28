package com.cleber.financas.api.dto;

/*Não usei @Builder, @Getter, @Setter
* resolvi criar na mão o codigo*/

public class UsuarioCadastroDTO {
    
    private String nomeCompleto;
    private String cadastroPessoaFisica;
    private String nomeUsuario;
    private String email;
    private String senha;

    public UsuarioCadastroDTO() {
		
	}
    
    /*BUILDER*/
    private UsuarioCadastroDTO(UsuarioBuilder builder) {
        
        this.nomeCompleto = builder.nomeCompleto;
        this.cadastroPessoaFisica = builder.cadastroPessoaFisica;
        this.nomeUsuario = builder.nomeUsuario;
        this.email = builder.email;
        this.senha = builder.senha;
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

    public static class UsuarioBuilder{
        private String nomeCompleto;
        private String cadastroPessoaFisica;
        private String nomeUsuario;
        private String email;
        private String senha;

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

        public UsuarioCadastroDTO build() {
            return new UsuarioCadastroDTO(this);
        }
    }
}
