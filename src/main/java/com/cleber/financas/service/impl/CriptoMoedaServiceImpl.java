package com.cleber.financas.service.impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.cleber.financas.api.dto.CriptoMoedaDTO;
import com.cleber.financas.model.entity.CriptoMoeda;
import com.cleber.financas.model.enums.StatusTransacao;
import com.cleber.financas.model.enums.TipoTransacao;
import com.cleber.financas.model.mapper.CriptoMoedaMapper;
import com.cleber.financas.model.repository.CriptoMoedaRepository;
import com.cleber.financas.service.CriptoMoedaService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CriptoMoedaServiceImpl implements CriptoMoedaService{
	
	private final CriptoMoedaRepository criptoMoedaRepository;
	
	private final CriptoMoedaMapper criptoMoedaMapper;
	
	@Override
	@Transactional
	public CriptoMoeda salvarCriptomoeda(CriptoMoedaDTO dto) {
		CriptoMoeda entidadeConvertida = criptoMoedaMapper.dtoToEntity(dto);
		//valor sobreposto pelo mapper - deadcode	
		dto.setStatusTransacao(StatusTransacao.ANALISAR);
		dto.setTipoTransacao(TipoTransacao.DECIDIR);
		
		return criptoMoedaRepository.save(entidadeConvertida);
	}
	
//	public CriptoMoeda salvarCriptomoeda(Criptomoeda criptomoeda) {
//		/* chamando */
//		validarCriptomoeda(criptomoeda);
//		criptomoeda.setStatusTransacao(StatusTransacao.ANALISAR);
//
//		return CriptomoedaRepository.save(criptomoeda);
//	}
	

	@Override
	public CriptoMoeda atualizarCriptomoeda(CriptoMoedaDTO criptoMoeda) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<CriptoMoeda> buscarCriptomoeda(CriptoMoeda criptoMoedaFiltro) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void deletarCriptoMoeda(CriptoMoedaDTO criptoMoedaDTO) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void atualizarStatus(CriptoMoedaDTO criptoMoedaDTO, StatusTransacao stuStatusTransacao) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Optional<CriptoMoeda> obterCriptomoedaPorId(Long uuid) {
		// TODO Auto-generated method stub
		return Optional.empty();
	}

	@Override
	public BigDecimal obterValorCriptomoedaPorUsuario(Long uuid) {
		// TODO Auto-generated method stub
		return null;
	}

}
