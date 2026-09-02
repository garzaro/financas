package com.cleber.financas.api.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

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

	private UUID uuid;

	@NotNull(message = "{criptomoeda.data.obrigatoria}")
	@FutureOrPresent(message = "{criptomoeda.data.futura}")
	private LocalDate dataEntrada;

	@NotNull(message = "{criptomoeda.mes.obrigatorio}")
	private Integer mes;

	@NotBlank(message = "{criptomoeda.corretora.obrigatoria}")
	private String corretora;

	@NotBlank(message = "{criptomoeda.ativo.obrigatorio}")
	private String ativo;

	private String alavancagem;

	@NotBlank(message = "{criptomoeda.moeda.corrente.obrigatoria}")
	private String moedaCorrente;

	@PositiveOrZero(message = "{criptomoeda.valor.acima.de.zero}")
	@NotNull(message = "{criptomoeda.valor.investido.obrigatorio}")
	private BigDecimal valorInvestido;

	@PositiveOrZero(message = "{criptomoeda.valor.atual.acima.de.zero}")
	@NotNull(message = "{criptomoeda.valor.atual.obrigatorio}")
	private BigDecimal valorAtualAtivo;

	@PositiveOrZero(message = "{criptomoeda.fracao.ativo.acima.de.zero}")
	@NotNull(message = "{criptomoeda.fracao.ativo.obrigatorio}")
	private BigDecimal fracaoAtivo;

	private LocalDate dataSaida;

	/*passar só o id do usuario, nao como objeto*/
    @NotNull
    private UUID usuario;

	@NotNull(message = "{criptomoeda.status.obrigatorio}")
	private StatusTransacao statusTransacao;

	@NotNull(message = "{criptomoeda.tipo.transacao.obrigatorio}")
	private TipoTransacao tipoTransacao;

	// private LocalDateTime dataCadastro;

	// private Instant dataAtualizacao;

}
