package com.cleber.financas.model.repository;

import com.cleber.financas.model.entity.Lancamento;
import com.cleber.financas.model.entity.StatusLancamento;
import com.cleber.financas.model.entity.TipoLancamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public interface LancamentoRepository extends JpaRepository<Lancamento, Long> {
    
    @Query(value = "select sum(l.valor) "
            + " from Lancamento l join l.usuario u "
            + " where u.id = :idUsuario and l.tipoLancamento = :tipoLancamento and l.statusLancamento = :statusLancamento "
            + " group by u")
    BigDecimal obterSaldoPorTipoLancamentoEUsuarioEstatus(
            @Param("idUsuario") Long idUsuario,
            @Param("tipoLancamento") TipoLancamento tipoLancamento,
            @Param("statusLancamento") StatusLancamento statusLancamento
    );
    
}
