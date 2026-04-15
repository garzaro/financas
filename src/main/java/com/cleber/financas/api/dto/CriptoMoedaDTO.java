package com.cleber.financas.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.cleber.financas.model.enums.StatusTransacao;
import com.cleber.financas.model.enums.TipoTransacao;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CriptoMoedaDTO {
		
	private Long id;
	
	@NotNull(message = "{data.obrigatoria}")
	@FutureOrPresent(message = "{data.futura}")
	private LocalDate dataEntrada;

	@NotNull(message = "{mes.obrigatorio}")
	private Integer mes;
	
	@NotBlank(message = "{corretora.obrigatoria}")
	private String corretora;
	
	@NotBlank(message = "{ativo.obrigatorio}")
	private String ativo;
	
	private String alavancagem;
	
	@NotBlank(message = "{moeda.corrente.obrigatoria}")
	private String moedaCorrente;
	
	@NotNull(message = "{valor.investido.obrigatorio}")
	@PositiveOrZero(message = "{valor.acima.de.zero}")
	private BigDecimal valorInvestido;
	
	@NotNull(message = "{valor.atual.criptomoeda.obrigatorio}")
	@PositiveOrZero(message = "{valor.atual.acima.de.zero}")
	private BigDecimal valorAtualAtivo;
	
	@NotNull(message = "{fracao.ativo.obrigatorio}")
	@PositiveOrZero(message = "fracao.ativo.acima.de.zero")
	private BigDecimal fracaoAtivo;
	
	private LocalDate dataSaida;
	
	/*passar só o id do usuario, nao como objeto*/
    @NotNull
    private Long usuario;
	
//	@NotNull(message = "{status.obrigatorio}")
	private StatusTransacao statusTransacao;
	
	private TipoTransacao tipoTransacao;
}
