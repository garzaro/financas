package com.cleber.financas.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.cleber.financas.api.dto.CriptoMoedaDTO;
import com.cleber.financas.model.entity.CriptoMoeda;
import com.cleber.financas.model.enums.StatusTransacao;

@Service
public interface CriptoMoedaService {
	CriptoMoeda salvarCriptomoeda(CriptoMoedaDTO criptoMoedaDTO);
	
	CriptoMoeda atualizarCriptomoeda(CriptoMoedaDTO criptoMoedaDTO);
	
	List<CriptoMoeda> buscarCriptomoeda(CriptoMoeda criptoMoedaFiltro);
	
	void deletarCriptoMoeda(CriptoMoedaDTO criptoMoeda);
	
	void atualizarStatus(CriptoMoedaDTO criptoMoeda, StatusTransacao stuStatusTransacao);
	
	Optional<CriptoMoeda> obterCriptomoedaPorId(Long uuid);
	
	BigDecimal obterValorCriptomoedaPorUsuario(Long uuid);	
}
