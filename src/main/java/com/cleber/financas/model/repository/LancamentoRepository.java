package com.cleber.financas.model.repository;

import com.cleber.financas.model.entity.Lancamento;
import com.cleber.financas.model.entity.TipoLancamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;

public interface LancamentoRepository extends JpaRepository<Lancamento, Long> {

    @Query(value = "select sum(l.valor) "
            + "from Lancamento l join l.usuario u "
            + "where u.id = :idUsuario and l.tipoLancamento = :tipoLancamento "
            + "group by u")
    BigDecimal obterSaldoPorUsuarioETipo(
            @Param("idUsuario")
            Long idUsuario,
            @Param("tipoLancamento")
            TipoLancamento tipoLancamento);
}
