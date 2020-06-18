package com.cleber.financa.service.impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.ExampleMatcher.StringMatcher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cleber.financa.exceptions.RegraNegocioException;
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
		validarLancamento(lancamento);		
		return lancamentoRepository.save(lancamento);
	}
	
	/*
	 * O metodo save, basicamente, salva e atualiza.
	 * O metodo atualizar irá salvar os registro atualizados
	 * desde que exista id ja cadastrado na base de dados. (derrr)
	 */

	@Override
	@Transactional
	public Lancamento atualizarLancamento(Lancamento lancamento) {
		Objects.requireNonNull(lancamento.getId());
		return lancamentoRepository.save(lancamento);
	}
	
	@Override
	@Transactional
	public void deletarLancamento(Lancamento lancamento) {
		Objects.requireNonNull(lancamento.getId());
		lancamentoRepository.delete(lancamento);
	}
	
	/*ver rodapé*/

	@Override
	@Transactional(readOnly = true) //somente leitura para o filtro
	public List<Lancamento> buscarLancamento(Lancamento lancamentoFiltro) {
		Example<Lancamento> example = Example.of(lancamentoFiltro,
				ExampleMatcher.matching()
				.withIgnoreCase()
				.withStringMatcher(StringMatcher.CONTAINING));
		
		return lancamentoRepository.findAll(example);
	}

	@Override
	public void atualizarStatusLancamento(Lancamento lancamento, StatusLancamento statusLancamento) {
		lancamento.setStatus(statusLancamento);
		atualizarLancamento(lancamento);
	}

	@Override
	public void validarLancamento(Lancamento lancamento) {
		
		if (lancamento.getDescricao() == null || lancamento
				.getDescricao()
				.trim()
				.equals(""))
		{
			throw new RegraNegocioException("Hã!!! Não entendi a descrição.");
			
		}
		
		if (lancamento.getMes() == null){
			throw new RegraNegocioException("Então!!! nada não é um mês válido.");
			
		}
		
		if (lancamento.getMes() < 1){
			throw new RegraNegocioException("Aí!!! zero não é um mês válido.");
			
		}
		
		if (lancamento.getMes() > 12){
			throw new RegraNegocioException("Hã!!! no meu calendário só existe 12 meses.");
			
		}
		
		if (lancamento.getAno() == null || lancamento.getAno().toString().length() != 4) {
			throw new RegraNegocioException("Ano deve conter quatro números, parecido com isso:	 1974");
			
		}
		
		/* Deve retornar um usuario já cadastrado na base de dados*/
		if(lancamento.getUsuario() == null || lancamento.getUsuario().getId() == null) {
			throw new RegraNegocioException("Sem usuario cadastrado não vai conseguir acessar");
			
		}
		
		/* Posso unir com compareTo abaixo */
		if(lancamento.getValor() == null) {
			throw new RegraNegocioException("Nada não é possivel lançar");
			
		}
		
		if(lancamento.getValor().compareTo(BigDecimal.ZERO) < 1) {
			throw new RegraNegocioException("Zero não é nada, lance pelo menos 1 centavo");
			
		}
		
		
	}

}



/*
 * SEM O USO DA API DE CONSULTA <QUERY BY EXAMPLE> DO SPRING DATA (Example == Object)
 * <QUERY BY EXAMPLE> API do Spring data para consulta automatica
 * 
 * @Override public List<Lancamento> buscarLancamento(Lancamento lancamentoFiltro) {
 * 
 * String sql = "select * from lancamento where 1 = 1"; 
 * if (lancamentoFiltro.getDescricao() != null) {
 * 
 * sql = sql + " and descrição = " + lancamentoFiltro.getDescricao();
 * 
 * } 
 * . 
 * .
 * .
 * adiciona o sql para cada uma das propriedades da entidade.
 * return null; }
 */