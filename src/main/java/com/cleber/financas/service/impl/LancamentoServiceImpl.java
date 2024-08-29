package com.cleber.financas.service.impl;

import com.cleber.financas.model.entity.Lancamento;
import com.cleber.financas.model.entity.StatusLancamento;
import com.cleber.financas.model.repository.LancamentoRepository;
import com.cleber.financas.service.LancamentoService;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
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
    @Transactional
    public Lancamento atualizarLancamento(Lancamento lancamento) {
        /*Checagem: se não existir um id de lancamento salvo ele persiste e lança um novo id...*/
        Objects.requireNonNull(lancamento.getId()); /*...garantindo que será passado o lancamento com um novo id*/
        return lancamentoRepository.save(lancamento); /*...se nao passar da nullPointerException*/
    }
    
    @Override
    @Transactional
    public void deletarLancamento(Lancamento lancamento) {
        /*so deleta se existir um lancamento salvo*/
        Objects.requireNonNull(lancamento.getId()); /*Checagem: para garantir que esteja passando o lancamento salvo*/
        lancamentoRepository.delete(lancamento);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Lancamento> buscarLancamento(Lancamento lancamentoFiltro) {
        Example example = Example.of(lancamentoFiltro, ExampleMatcher
                .matching()
                /*ignora se o usuario digitou com caixa alta ou baixa*/
                .withIgnoreCase()
                /*contendo o que for passado na busca - CONTAINING*/
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING));
        return lancamentoRepository.findAll(example);
    }
    
    @Override
    public void atualizarStatus(Lancamento lancamento, StatusLancamento status) {
        lancamento.setStatusLancamento(status); /*seta o estatus do lancamento*/
        atualizarLancamento(lancamento); /*usa a implemetnacao de salvar lancamento para efetivar*/
    }
}

