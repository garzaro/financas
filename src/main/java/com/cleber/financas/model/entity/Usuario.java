package com.cleber.financas.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters;

import java.time.LocalDate;
import java.util.Objects;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name= "usuario", schema = "financeiro")
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    @Column(name = "nome_completo")
    private String nomeCompleto;
    
    @Column(name = "cpf")
    private String cadastroPessoaFisica;
    
    @Column(name = "nome_usuario")
    private String nomeUsuario;
    
    @Column(name = "email")
    private String email;
	
    @JsonIgnore
    @Column(name = "senha")
    private String senha;

    @Convert(converter = Jsr310JpaConverters.LocalDateConverter.class)
    @Column(name = "data_cadastro")
    private LocalDate dataCadastro;

    
    
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getNomeCompleto() {
		return nomeCompleto;
	}

	public void setNomeCompleto(String nomeCompleto) {
		this.nomeCompleto = nomeCompleto;
	}

	public String getCadastroPessoaFisica() {
		return cadastroPessoaFisica;
	}

	public void setCadastroPessoaFisica(String cadastroPessoaFisica) {
		this.cadastroPessoaFisica = cadastroPessoaFisica;
	}

	public String getNomeUsuario() {
		return nomeUsuario;
	}

	public void setNomeUsuario(String nomeUsuario) {
		this.nomeUsuario = nomeUsuario;
	}

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

	public LocalDate getDataCadastro() {
		return dataCadastro;
	}

	public void setDataCadastro(LocalDate dataCadastro) {
		this.dataCadastro = dataCadastro;
	}
	
	public static class UsuarioBuilder{
		private String nomeCompleto;
        private String cadastroPessoaFisica;
        private String nomeUsuario;
        private String email;
        private String senha;
        private LocalDate dataCadastro;
	}
            

	@Override
	public int hashCode() {
		return Objects.hash(cadastroPessoaFisica, dataCadastro, email, id, nomeCompleto, nomeUsuario, senha);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Usuario other = (Usuario) obj;
		return Objects.equals(cadastroPessoaFisica, other.cadastroPessoaFisica)
				&& Objects.equals(dataCadastro, other.dataCadastro) && Objects.equals(email, other.email)
				&& Objects.equals(id, other.id) && Objects.equals(nomeCompleto, other.nomeCompleto)
				&& Objects.equals(nomeUsuario, other.nomeUsuario) && Objects.equals(senha, other.senha);
	}

	@Override
	public String toString() {
		return "Usuario [id=" + id + ", nomeCompleto=" + nomeCompleto + ", cadastroPessoaFisica=" + cadastroPessoaFisica
				+ ", nomeUsuario=" + nomeUsuario + ", email=" + email + ", senha=" + senha + ", dataCadastro="
				+ dataCadastro + "]";
	}

    /*GETTERS AND SETTERS*/
    
    
}

