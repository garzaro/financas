package com.cleber.financa.model.entity;

import java.io.Serializable;
import java.time.LocalDate;

import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters;

import com.cleber.financa.model.enums.StatusLancamento;
import com.cleber.financa.model.enums.TipoLancamento;

import lombok.Builder;
import lombok.Data;
@Builder
@Data
@Entity
public class Lancamento implements Serializable{

	private static final long serialVersionUID = 1L;
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String descricao;
	private Integer ano;
	private Integer mes;
	
	@Convert(converter = Jsr310JpaConverters.LocalDateConverter.class)
	private LocalDate dataCadastro;
	
	/*relacionamento*/
	@ManyToOne
	@JoinColumn(name = "idUsuario")
	private Usuario usuario;
	
	@Enumerated(value = EnumType.STRING)
	private TipoLancamento tipo;
	
	@Enumerated(value = EnumType.STRING)
	private StatusLancamento status;

}
