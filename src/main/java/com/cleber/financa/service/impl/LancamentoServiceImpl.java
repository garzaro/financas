package com.cleber.financa.service.impl;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
	
	/*
	 * Usando a annotation @Transactional. 
	 * abre conexao faz commit e um rollback em caso de
	 * algum erro na transação.
	 */

	@Override
	@Transactional      
	public Lancamento salvarLancamento(Lancamento lancamento) {
		
		return lancamentoRepository.save(lancamento);
	}
	
	/*
	 * O metodo atualizar irá salvar os registro atualizados
	 * desde que exista id ja cadastrado na base de dados. (derrr)
	 */

	@Override
	@Transactional
	public Lancamento atualizarLancamento(Lancamento lancamento) {
		Objects.requireNonNull(lancamento);
		return lancamentoRepository.save(lancamento);
	}
	
	@Override
	@Transactional
	public void deletarLancamento(Lancamento lancamento) {
		Objects.requireNonNull(lancamento.getId());
		lancamentoRepository.delete(lancamento);
	}

	@Override
	public List<Lancamento> buscarLancamento(Lancamento lancamentoFiltro) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void atualizarStatusLancamento(Lancamento lancamento, StatusLancamento statusLancamento) {
		lancamento.setStatus(statusLancamento);
		atualizarLancamento(lancamento);
	}

}
