package com.cleber.financas.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.envers.Audited;
import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Audited
@Builder
//@EqualsAndHashCode(of ="id")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name= "usuario", schema = "financeiro")
public class Usuario implements Serializable{
    private static final long serialVersionUID = 1L;

	@GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id")
    private Long id;
    
    @Column(name = "nome")
    private String nome;

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

