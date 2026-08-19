package com.cleber.financas.api.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.validator.constraints.br.CPF;

/**
 * Não usei @Builder, @Getter, @Setter
 * resolvi criar na mão o codigo
 * 
 * Implementar Bean Validatation 
 * */
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * TODO-list
 *
 * **/

public class UsuarioDTO {
	
	private UUID uuid;
    
	@NotBlank(message = "{usuario.nome.completo.obrigatorio}")
	@Pattern(regexp = "^[a-zA-ZÀ-ÿ\\s]+$", message = "{usuario.nome.completo.invalido}")
//	@JsonAlias({"nome_completo", "nomeCompleto"})
	private String nomeCompleto;
    
	@NotBlank(message = "{usuario.cpf.obrigatorio}")
	@CPF(message = "{usuario.cpf.invalido}")
	private String cpf;
    
	@NotBlank(message = "{usuario.nome.usuario.obrigatorio}")
//    @JsonAlias({"nome_usuario", "nomeUsuario"})
    private String nomeUsuario;    
    
	@NotBlank(message = "{usuario.email.obrigatorio}")
	@Email(message = "{usuario.email.invalido}")
    private String email;
    
	@NotBlank(message = "{usuario.senha.obrigatoria}")
    private String senha;
	
	private LocalDateTime dataCadastro;

    public UsuarioDTO() {
    }

    /* BUILDER */
    private UsuarioDTO(UsuarioBuilder builder) {
        this.uuid = builder.uuid;
        this.nomeCompleto = builder.nomeCompleto;
        this.cpf = builder.cpf;
        this.nomeUsuario = builder.nomeUsuario;
        this.email = builder.email;
        this.senha = builder.senha;
        this.dataCadastro = builder.dataCadastro;
    }

    public UUID getId() {
        return uuid;
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

   public LocalDateTime getDataCadastro() {
       return dataCadastro;
   }

    public static class UsuarioBuilder {
        private UUID uuid;
        private String nomeCompleto;
        private String cpf;
        private String nomeUsuario;
        private String email;
        private String senha;
        private LocalDateTime dataCadastro;

        public UsuarioBuilder setUuid(UUID id) {
            this.uuid = uuid;
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

       public UsuarioBuilder setDataCadastro(LocalDateTime dataCadastro) {
           this.dataCadastro = dataCadastro;
           return this;
       }

        public UsuarioDTO build() {
            return new UsuarioDTO(this);
        }
    }
}
