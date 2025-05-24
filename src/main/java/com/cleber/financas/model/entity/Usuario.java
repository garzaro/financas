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
import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters;
import java.io.Serializable;
import java.time.LocalDate;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
//@EqualsAndHashCode(of ="id")
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

    @Column(name = "usuario")
    private String usuario;
    
    @Column(name = "email")
    private String email;
	
    @Column(name = "senha")
    @JsonIgnore
    private String senha;

    @Convert(converter = Jsr310JpaConverters.LocalDateConverter.class)
    @Column(name = "data_cadastro")
    private LocalDate dataCadastro;

    /*GETTERS AND SETTERS*/
    /*HASHCODE AND EQUALS*/
    /*TO STRING*/
}

