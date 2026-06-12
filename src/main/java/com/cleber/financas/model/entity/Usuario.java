package com.cleber.financas.model.entity;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
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
    @Column(name = "id_usuario", updatable = false, nullable = false)
    private UUID id;
    
    @Column(name = "nome_completo")
    private String nomeCompleto;

    @Column(name = "cpf")
    private String cpf;

    @Column(name = "nome_usuario")
    private String nomeUsuario;
    
    @Column(name = "email")
    private String email;
	
    @Column(name = "senha", nullable = false, length = 255)
    @JsonIgnore
    private String senha;

    @CreationTimestamp
    @Column(name = "data_cadastro", updatable = false) /**, columnDefinition = "DATE DEFAULT CURRENT_DATA"*/
    private Instant dataCadastro;

	/**@Convert(converter = Jsr310JpaConverters.LocalDateConverter.class)
    @Column(name = "data_cadastro")
    private LocalDate dataCadastro;
     */

    /*GETTERS AND SETTERS*/
    /*HASHCODE AND EQUALS*/
    /*TO STRING*/
    
    
    
}
