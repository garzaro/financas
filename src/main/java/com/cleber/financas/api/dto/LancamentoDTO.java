package com.cleber.financas.api.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LancamentoDTO {
    private Long id;
    private String descricao;
    private String mes;
    private Integer ano;
	private BigDecimal valor;
    /*passar só o id do usuario, nao como objeto*/
    @NotNull
    private Long usuario;
    private String tipoLancamento;
    private String statusLancamento;
	private LocalDate dataCadastro;

/**DEIXEI PRA CASO AS ANOTATION DE PROBLEMA MAS NAO PRECISA**/
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getDescricao() {
		return descricao;
	}
	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}
	public String getMes() {
		return mes;
	}
	public void setMes(String mes) {
		this.mes = mes;
	}
	public Integer getAno() {
		return ano;
	}
	public void setAno(Integer ano) {
		this.ano = ano;
	}
	public BigDecimal getValor() {
		return valor;
	}
	public void setValor(BigDecimal valor) {
		this.valor = valor;
	}
	public Long getUsuario() {
		return usuario;
	}
	public void setUsuario(Long usuario) {
		this.usuario = usuario;
	}
	public String getTipo() {
		return tipoLancamento;
	}
	public void setTipo(String tipo) {
		this.tipoLancamento = tipo;
	}
	public String getStatusLancamento() {
		return statusLancamento;
	}
	public void setStatusLancamento(String statusLancamento) {
		this.statusLancamento = statusLancamento;
	}
    
    
}
