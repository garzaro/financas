package com.cleber.financas.model.repository;

import com.cleber.financas.model.entity.Lancamento;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LancamentoRepository extends JpaRepository<Lancamento, Long> {

}
