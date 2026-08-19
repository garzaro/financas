package com.cleber.financas.model.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cleber.financas.model.entity.CriptoMoeda;
import com.cleber.financas.model.enums.StatusTransacao;
import com.cleber.financas.model.enums.TipoTransacao;


@Repository
public interface CriptoMoedaRepository extends JpaRepository<CriptoMoeda, UUID>{
	Optional<CriptoMoeda> findByCriptomoeda(String criptomoeda);

	List<CriptoMoeda> findByStatusTransacao(StatusTransacao status);

	List<CriptoMoeda> findByTipoTransacao(TipoTransacao tipo);
}
