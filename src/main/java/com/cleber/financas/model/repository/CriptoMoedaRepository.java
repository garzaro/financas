package com.cleber.financas.model.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cleber.financas.model.entity.CriptoMoeda;


@Repository
public interface CriptoMoedaRepository extends JpaRepository<CriptoMoeda, Long>{
	Optional<CriptoMoeda> findByCriptomoeda(String criptomoeda);
}
