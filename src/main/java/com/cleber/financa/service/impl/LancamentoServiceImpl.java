package com.cleber.financa.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.cleber.financa.model.entity.Lancamento;
import com.cleber.financa.model.enums.StatusLancamento;
import com.cleber.financa.model.repository.LancamentoRepository;
import com.cleber.financa.service.LancamentoService;

@Service
public class LancamentoServiceImpl implements LancamentoService {
	
	public LancamentoRepository lancamentoRepository;
	/* Injeção de dependencia */
	public LancamentoServiceImpl(LancamentoRepository lancamentoRepository) {
		this.lancamentoRepository = lancamentoRepository;
	}

	@Override
	public Lancamento salvarLancamento(Lancamento lancamento) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Lancamento atualizarLancamento(Lancamento lancamento) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void deletarLancamento(Lancamento lancamento) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<Lancamento> buscarLancamento(Lancamento lancamentoFiltro) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void atualizarStatusLancamento(Lancamento lancamento, StatusLancamento statusLancamento) {
		// TODO Auto-generated method stub
		
	}

}
