package com.cleber.financas.model.entity;

import com.cleber.financas.jackson.DeserializadorBigDecimalCustomizado;
import com.cleber.financas.jackson.DeserializadorLocalDate;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters;

import java.math.BigDecimal;
import java.time.LocalDate;

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

    //@JsonDeserialize(using = DeserializadorBigDecimalCustomizado.class)
    @Column(name = "valor")
    private BigDecimal valor;
    
    @JsonDeserialize(using = DeserializadorLocalDate.class)
    @Column(name = "data_cadastro")
    @Convert(converter = Jsr310JpaConverters.LocalDateConverter.class)
    private LocalDate dataCadastro;
    
    @Column(name = "tipo_lancamento")
    @Enumerated(value = EnumType.STRING)
    private TipoLancamento tipoLancamento;
    
    @Column(name = "status_lancamento")
    @Enumerated(value = EnumType.STRING)
    private StatusLancamento statusLancamento;

}
