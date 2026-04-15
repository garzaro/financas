package com.cleber.financas.model.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.envers.Audited;

import com.cleber.financas.model.enums.StatusTransacao;
import com.cleber.financas.model.enums.TipoTransacao;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Audited
@Entity
@Data
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "criptomoeda", schema = "financeiro")
public class CriptoMoeda {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;
	
	@Column(name = "data_entrada")
	private LocalDate dataEntrada;
	
	@Column(name = "mes")
	private Integer mes;
	
	@Column(name = "corretora")
	private String corretora;
	
	@Column(name = "criptomoeda")
	private String criptomoeda;
	
	@Column(name = "alavancagem")
	private String alavancagem;
	
	@Column(name = "moeda_corrente")
	private String moedaCorrente;
	
	@Column(name = "valor_investido")
	private BigDecimal valorInvestido;
	
	@Column(name = "valor_atual")
	private BigDecimal valorAtual;
	
	@Column(name = "fracao")
	private BigDecimal fracao;
	
	@Column(name = "data_saida")
	private LocalDate dataSaida;
	
	@ManyToOne
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;
	
	@CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant dataCriacao;
	
	@UpdateTimestamp
    @Column(nullable = false)
    private Instant dataAtualizacao;

	@Column(name = "status_transacao")
	@Enumerated(value = EnumType.STRING)
	private StatusTransacao statusTransacao;
	
	@Column(name = "tipo_transacao")
	@Enumerated(value = EnumType.STRING)
	private TipoTransacao tipoTransacao;
}
