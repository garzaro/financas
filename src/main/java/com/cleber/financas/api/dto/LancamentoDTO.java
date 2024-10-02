package com.cleber.financas.api.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class LancamentoDTO {
	private Long id;
	private String descricao;
	private Integer mes;
	private Integer ano;
	private BigDecimal valor;
	/*passar só o id do usuario, nao como objeto*/
	private Long usuario;
	private String tipo;
	private String status;
	/*LocalDate dataCadastro;*/
}
