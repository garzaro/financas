package com.cleber.financa.service;

import java.util.List;

import com.cleber.financa.model.entity.Lancamento;
import com.cleber.financa.model.enums.StatusLancamento;

public interface LancamentoService { //CRUD
	
	Lancamento salvarLancamento(Lancamento lancamento);
	
	Lancamento atualizarLancamento(Lancamento lancamento);
	
	void deletarLancamento(Lancamento lancamento);
	
	/*
	 * receber os parametros de busca de lancamento
	 * e usar as propriedade deste lancamento como filtro
	 * acesso a base e retornar a lista de objetos
	 * */
	
	List<Lancamento> buscarLancamento(Lancamento lancamentoFiltro);
	
	/*
	 * Metodo para alteração de status do lancamento
	 * PENDENTE, EFETIVAR E CANCELAR
	 * */
	
	void atualizarStatusLancamento(Lancamento lancamento, StatusLancamento statusLancamento);
		
	
	

}
