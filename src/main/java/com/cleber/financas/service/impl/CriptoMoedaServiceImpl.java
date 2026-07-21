package com.cleber.financas.service.impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.cleber.financas.exception.RegraDeNegocioException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cleber.financas.api.dto.CriptoMoedaDTO;
import com.cleber.financas.model.entity.CriptoMoeda;
import com.cleber.financas.model.enums.StatusTransacao;
import com.cleber.financas.model.mapper.CriptoMoedaMapper;
import com.cleber.financas.model.repository.CriptoMoedaRepository;
import com.cleber.financas.service.CriptoMoedaService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CriptoMoedaServiceImpl implements CriptoMoedaService{
	
	private final CriptoMoedaRepository criptoMoedaRepository;
	private final CriptoMoedaMapper criptoMoedaMapper;
	
//	@Override
//	@Transactional
//	public CriptoMoedaDTO salvarCriptomoeda(CriptoMoedaDTO criptoMoedaDTO) {
//		CriptoMoeda entidadeConvertida = criptoMoedaMapper.entityToDto(criptoMoedaDTO);
//		return criptoMoedaRepository.save(entidadeConvertida);
//	}
	
	@Override
	@Transactional
	public CriptoMoedaDTO salvarCriptomoeda(CriptoMoedaDTO criptoMoedaDTO) {
		if (criptoMoedaDTO.getAtivo() == null) {
			throw new RegraDeNegocioException("A criptomoeda não pode ser nula");
		}
		
		CriptoMoeda entidadeConvertida = criptoMoedaMapper.dtoToEntity(criptoMoedaDTO);
		
		CriptoMoeda criptoMoedaSalva = criptoMoedaRepository.save(entidadeConvertida);
		
		return criptoMoedaMapper.entityToDto(criptoMoedaSalva);
	}
	
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
	public Optional<CriptoMoeda> obterCriptomoedaPorId(UUID uuid) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BigDecimal obterValorCriptomoedaPorUsuario(Long uuid) {
		// TODO Auto-generated method stub
		return null;
	}
}
