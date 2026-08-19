package com.cleber.financas.model.entity;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.envers.Audited;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
    @Column(name = "uuid")
    private UUID uuid;
    
    @Column(name = "nome_completo", nullable = false, length = 150)
    private String nomeCompleto;
    
    @Column(name = "cpf", nullable = false, length = 11)
    private String cpf;
    
    @Column(name = "nome_usuario", nullable = false, length = 50)
    private String nomeUsuario;    
   
    @Column(name = "email", nullable = false, length = 255)
    private String email;	
   
    @Column(name = "senha", nullable = false, length = 255)
    @JsonIgnore
    private String senha;

    @CreationTimestamp
    @Column(name = "data_cadastro", updatable = false)
    private LocalDateTime dataCadastro;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant dataAtualizacao;

    // @Builder.Default //ja nasce ativo
    @Column(name = "is_ativo")
    private Boolean isAtivo;

    /*GETTERS AND SETTERS*/
    /*HASHCODE AND EQUALS*/
    /*TO STRING*/
    
    
    
}
