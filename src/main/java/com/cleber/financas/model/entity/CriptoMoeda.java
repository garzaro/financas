package com.cleber.financas.model.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
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

/**
 * TODO-list
 * [] verificar se o usuario pode inserir uma transacao com a mesma moeda
 * [] verificar o tempo do acesse tken esta em 30 segundo
 *
 * **/

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
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "uuid")
	private UUID uuid;

	@Column(name = "data_entrada", nullable = false)
	private LocalDate dataEntrada;

	@Column(name = "mes")
	private Integer mes;

	@Column(name = "corretora", nullable = false)
	private String corretora;

	@Column(name = "criptomoeda", nullable = false)
	private String criptomoeda;

	@Column(name = "alavancagem")
	private String alavancagem;

	@Column(name = "moeda_corrente", nullable = false)
	private String moedaCorrente;

	@Column(name = "valor_investido", nullable = false)
	private BigDecimal valorInvestido;

	@Column(name = "valor_atual", nullable = false)
	private BigDecimal valorAtual;

    @Column(name = "fracao", nullable = false)
    private BigDecimal fracao;

	@Column(name = "data_saida")
	private LocalDate dataSaida;

	@ManyToOne
    @JoinColumn(name = "usuario_uuid", referencedColumnName = "uuid", nullable = false)
    private Usuario usuario;

	@Column(name = "status_transacao", nullable = false)
	@Enumerated(value = EnumType.STRING)
	private StatusTransacao statusTransacao;

	@Column(name = "tipo_transacao", nullable = false)
	@Enumerated(value = EnumType.STRING)
	private TipoTransacao tipoTransacao;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant dataCriacao;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant dataAtualizacao;
}
