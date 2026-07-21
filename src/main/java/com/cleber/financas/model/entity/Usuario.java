package com.cleber.financas.model.entity;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.envers.Audited;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Audited
@Builder
//@EqualsAndHashCode(of ="id")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name= "usuario", schema = "financeiro")
public class Usuario{
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_usuario")
    private UUID id;
    
    @NotBlank(message = "{usuario.nome-completo.obrigatorio}")
    @Pattern(regexp = "^[a-zA-ZÀ-ÿ\\s]+$", message = "{usuario.nome-completo.invalido}")
    @Column(name = "nome_completo", nullable = false, length = 255)
    private String nomeCompleto;

    @NotBlank(message = "{usuario.cpf.obrigatorio}")
    @Column(name = "cpf", nullable = false, length = 14)
    private String cpf;

    @NotBlank(message = "{usuario.nome-usuario.obrigatorio}")
    @Column(name = "nome_usuario", nullable = false, length = 255)
    private String nomeUsuario;
    
    @NotBlank(message = "{usuario.email.obrigatorio}")
    @Email(message = "{usuario.email.invalido}")
    @Column(name = "email", nullable = false, length = 255)
    private String email;
	
    @NotBlank(message = "{usuario.senha.obrigatoria}")
    @Column(name = "senha", nullable = false, length = 255)
    @JsonIgnore
    private String senha;

    @CreationTimestamp
    @Column(name = "data_cadastro", updatable = false) /**, columnDefinition = "DATE DEFAULT CURRENT_DATA"*/
    private LocalDateTime dataCadastro;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant dataAtualizacao;

    @Column(name = "ativo")
    private Boolean isAtivo;

    /*GETTERS AND SETTERS*/
    /*HASHCODE AND EQUALS*/
    /*TO STRING*/
    
    
    
}
