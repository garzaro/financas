package com.cleber.financas.service;

import com.cleber.financas.model.entity.Lancamento;
import com.cleber.financas.model.entity.StatusLancamento;

import java.util.List;
import java.util.Optional;

public interface LancamentoService {
    
    /*vai receber um lancamento e vai salvar*/
    Lancamento salvarLancamento(Lancamento lancamento);

    Lancamento atualizarLancamento(Lancamento lancamento);
    
    void deletarLancamento(Lancamento lancamento);
    
    /*as propriedades que vierem preenchidas no lancamento recebido sera usado como filtro*/
    List<Lancamento> buscarLancamento(Lancamento lancamentoFiltro);
    
    /*recebe o status e o lancamento*/
    void atualizarStatus(Lancamento lancamento, StatusLancamento status);
    
    /*ver se ja exite o lancamento na base de dados*/
    void validarLancamento(Lancamento lancamento);
    
    Optional<Lancamento> obterLancamentoPorId(Long id);
}
