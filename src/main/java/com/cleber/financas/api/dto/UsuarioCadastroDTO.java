package com.cleber.financas.api.dto;

/*Não usei @Builder, @Getter, @Setter
* resolvi criar na mão o codigo*/

public class UsuarioCadastroDTO {
    
    private String nome;
    private String cpf;
    private String usuario;
    private String email;
    private String senha;

    public UsuarioCadastroDTO() {
		
	}
    
    /*BUILDER*/
    private UsuarioCadastroDTO(UsuarioBuilder builder) {
        
        this.nome = builder.nome;
        this.cpf = builder.cpf;
        this.usuario = builder.usuario;
        this.email = builder.email;
        this.senha = builder.senha;
    }

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

    public static class UsuarioBuilder{
        private String nome;
        private String cpf;
        private String usuario;
        private String email;
        private String senha;

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

        public UsuarioCadastroDTO build() {
            return new UsuarioCadastroDTO(this);
        }
    }
}
