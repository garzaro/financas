package com.cleber.financa.model.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cleber.financa.model.entity.Lancamento;

public interface LancamentoRepository extends JpaRepository<Lancamento, Long> {
	
	boolean existsByMes(Integer mes);
	boolean existsByAno(Integer ano);
	

}
