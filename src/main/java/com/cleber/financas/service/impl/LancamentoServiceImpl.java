package com.cleber.financas.service.impl;

import com.cleber.financas.model.entity.Lancamento;
import com.cleber.financas.model.entity.StatusLancamento;
import com.cleber.financas.model.repository.LancamentoRepository;
import com.cleber.financas.service.LancamentoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
public class LancamentoServiceImpl implements LancamentoService {
    
    /*para injetar uma instancia de repositoy, precisa de um contrutor*/
    private LancamentoRepository lancamentoRepository;
    
    /*construtor contendo um instancia para injetar*/
    public LancamentoServiceImpl(LancamentoRepository lancamentoRepository) {
        this.lancamentoRepository = lancamentoRepository;
    }
    
    @Override
    @Transactional /*spring abre transação com a base, roda o metodo, faz commit e, se error, then rollback*/
    public Lancamento salvarLancamento(Lancamento lancamento) {
        return lancamentoRepository.save(lancamento);
    }
    
    @Override
    public Lancamento atualizarLancamento(Lancamento lancamento) {
        /*Checagem: se não existir um id de lancamento salvo ele persiste e lanca um novo id...*/
        Objects.requireNonNull(lancamento.getId()); /*...garantindo que será passado o lancamento com um novo id*/
        return lancamentoRepository.save(lancamento); /*...se nao passar da nullPointerException*/
    }
    
    @Override
    public void deletarLancamento(Lancamento lancamento) {
        /*so deletar se existir um lancamento salvo*/
        Objects.requireNonNull(lancamento.getId()); /*Checagem: para garantir que esteja passando o lancamento salvo*/
        lancamentoRepository.save(lancamento);
    
    }
    
    @Override
    public List<Lancamento> buscarLancamento(Lancamento lancamentoFiltro) {
        return List.of();
    }
    
    @Override
    public void atualizarStatus(Lancamento lancamento, StatusLancamento status) {
    
    }
}
