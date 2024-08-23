package com.cleber.financas.service.impl;

import com.cleber.financas.model.entity.Lancamento;
import com.cleber.financas.model.entity.StatusLancamento;
import com.cleber.financas.model.repository.LancamentoRepository;
import com.cleber.financas.service.LancamentoService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LancamentoServiceImpl implements LancamentoService {
    
    /*para injetar uma instancia de repositoy, precisa de um contrutor*/
    private LancamentoRepository lancamentoRepository;
    
    /*construtor contendo um instancia para injetar*/
    public LancamentoServiceImpl(LancamentoRepository lancamentoRepository) {
        this.lancamentoRepository = lancamentoRepository;
    }
    
    @Override
    public Lancamento salvarLancamento(Lancamento lancamento) {
        return null;
    }
    
    @Override
    public Lancamento atualizarLancamento(Lancamento lancamento) {
        return null;
    }
    
    @Override
    public void deletarLancamento(Lancamento lancamento) {
    
    }
    
    @Override
    public List<Lancamento> buscarLancamento(Lancamento lancamentoFiltro) {
        return List.of();
    }
    
    @Override
    public void atualizarStatus(Lancamento lancamento, StatusLancamento status) {
    
    }
}
