package com.cleber.financas.model.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.envers.Audited;
import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
@Audited
@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "lancamento", schema = "financeiro")
public class Lancamento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    @Column(name = "descricao")
    private String descricao;
    
    @Column(name = "mes")
    private Integer mes;
    
    @Column(name = "ano")
    private Integer ano;
    
    @ManyToOne
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;

    @Column(name = "valor")
    private BigDecimal valor;

    /**
     * Registra o momento EXATO da criação da entidade.
     * valor definido automaticamente pelo Hibernate na
     * primeira vez que a entidade é salva. `updatable = false`
     * garante que este campo nunca seja alterado após a criação.
     */
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant dataCriacao;

    /**
     * Registra o momento EXATO da última atualização da entidade.
     * O valor é atualizado automaticamente pelo Hibernate toda vez
     * que a entidade é modificada.
     */
    @UpdateTimestamp
    @Column(nullable = false)
    private Instant dataAtualizacao;

    /**
     * Enums
     * */

    @Column(name = "tipo_lancamento")
    @Enumerated(value = EnumType.STRING)
    private TipoLancamento tipoLancamento;
    
    @Column(name = "status_lancamento")
    @Enumerated(value = EnumType.STRING)
    private StatusLancamento statusLancamento;
    
}
