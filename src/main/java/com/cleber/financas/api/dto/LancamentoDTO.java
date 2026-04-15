package com.cleber.financas.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LancamentoDTO {
    private Long id;
    private String descricao;
    private Integer mes;
    private Integer ano;
	private BigDecimal valor;
    /*passar só o id do usuario, nao como objeto*/
    @NotNull
    private Long usuario;
    private String tipoLancamento;
    private String statusLancamento;
	private LocalDate dataCadastro;

/**DEIXEI PRA CASO AS ANOTATION DE PROBLEMA MAS NAO PRECISA**/
//	public Long getId() {
//		return id;
//	}
//	public void setId(Long id) {
//		this.id = id;
//	}
//	public String getDescricao() {
//		return descricao;
//	}
//	public void setDescricao(String descricao) {
//		this.descricao = descricao;
//	}
//	public Integer getMes() {
//		return mes;
//	}
//	public void setMes(Integer mes) {
//		this.mes = mes;
//	}
//	public Integer getAno() {
//		return ano;
//	}
//	public void setAno(Integer ano) {
//		this.ano = ano;
//	}
//	public BigDecimal getValor() {
//		return valor;
//	}
//	public void setValor(BigDecimal valor) {
//		this.valor = valor;
//	}
//	public Long getUsuario() {
//		return usuario;
//	}
//	public void setUsuario(Long usuario) {
//		this.usuario = usuario;
//	}
//	public String getTipoLancamento() {
//		return tipoLancamento;
//	}
//	public void setTipoLancamento(String tipoLancamento) {
//		this.tipoLancamento = tipoLancamento;
//	}
//	public String getStatusLancamento() {
//		return statusLancamento;
//	}
//	public void setStatusLancamento(String statusLancamento) {
//		this.statusLancamento = statusLancamento;
//	}
//
//    @Override
//    public boolean equals(Object o) {
//        if (o == null || getClass() != o.getClass()) return false;
//        LancamentoDTO that = (LancamentoDTO) o;
//        return Objects.equals(id, that.id) &&
//                Objects.equals(descricao, that.descricao) &&
//                Objects.equals(mes, that.mes) &&
//                Objects.equals(ano, that.ano) &&
//                Objects.equals(valor, that.valor) &&
//                Objects.equals(usuario, that.usuario) &&
//                Objects.equals(tipoLancamento, that.tipoLancamento) &&
//                Objects.equals(statusLancamento, that.statusLancamento) &&
//                Objects.equals(dataCadastro, that.dataCadastro);
//    }
//
//    @Override
//    public int hashCode() {
//        return Objects.hash(id, descricao, mes, ano, valor, usuario, tipoLancamento, statusLancamento, dataCadastro);
//    }
//
//    @Override
//    public String toString() {
//        return "LancamentoDTO{" +
//                "id=" + id +
//                ", descricao='" + descricao + '\'' +
//                ", mes=" + mes +
//                ", ano=" + ano +
//                ", valor=" + valor +
//                ", usuario=" + usuario +
//                ", tipoLancamento='" + tipoLancamento + '\'' +
//                ", statusLancamento='" + statusLancamento + '\'' +
//                ", dataCadastro=" + dataCadastro +
//                '}';
//    }
}
