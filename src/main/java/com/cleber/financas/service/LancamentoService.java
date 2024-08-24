package com.cleber.financas.service;

import com.cleber.financas.model.entity.Lancamento;
import com.cleber.financas.model.entity.StatusLancamento;

import java.util.List;

public interface LancamentoService {
    
    Lancamento salvarLancamento(Lancamento lancamento);
    /*se vai atualizar é porque ja existe na base*/
    Lancamento atualizarLancamento(Lancamento lancamento);
    
    void deletarLancamento(Lancamento lancamento);
    
    /*as propriedades que vierem preenchidas no objeto lancamento recebido sera usado como filtro*/
    List<Lancamento> buscarLancamento(Lancamento lancamentoFiltro);
    
    /*recebe o status e o lancamento*/
    void atualizarStatus(Lancamento lancamento, StatusLancamento status);
}
