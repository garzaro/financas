package com.cleber.financas.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters;

import java.time.LocalDate;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
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
    
    @Column(name = "senha")
    private String senha;

    @Convert(converter = Jsr310JpaConverters.LocalDateConverter.class)
    @Column(name = "data_cadastro")
    private LocalDate dataCadastro;

    /*GETTERS AND SETTERS*/
}

