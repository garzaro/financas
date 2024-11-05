package com.cleber.financas.service.impl;

/*@Slf4j faz parte do projeto lombok, substitui o Logger*/
/*@Slf4j - private static Logger log = LoggerFactory.getLogger(LancamentoServiceImpl.class);*/

import com.cleber.financas.exception.RegraDeNegocioException;
import com.cleber.financas.model.entity.Lancamento;
import com.cleber.financas.model.entity.StatusLancamento;
import com.cleber.financas.model.entity.TipoLancamento;
import com.cleber.financas.model.repository.LancamentoRepository;
import com.cleber.financas.service.LancamentoService;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class LancamentoServiceImpl implements LancamentoService {
    
    /*para injetar uma instancia de repositoy, precisa de um construtor*/
    private LancamentoRepository lancamentoRepository;
    
    /*construtor contendo um instancia para injetar*/
    public LancamentoServiceImpl(LancamentoRepository lancamentoRepository) {
        this.lancamentoRepository = lancamentoRepository;
    }
    
    @Override
    @Transactional /*spring abre transação com a base, roda o metodo, faz commit e, se error, then rollback*/
    public Lancamento salvarLancamento(Lancamento lancamento) {
        /*chamando*/
        validarLancamento(lancamento);
        /*lancamento salva automaticamente tem status de pendente*/
        lancamento.setStatusLancamento(StatusLancamento.PENDENTE);
        
        return lancamentoRepository.save(lancamento);
    }
    
    @Override
    @Transactional
    public Lancamento atualizarLancamento(Lancamento lancamento) {
        /*Checagem: se não existir um id de lancamento salvo ele persiste e lança um novo id...*/
        Objects.requireNonNull(lancamento.getId()); /*...garantindo que será passado o lancamento com um novo id*/
        validarLancamento(lancamento);
        return lancamentoRepository.save(lancamento); /*...se nao passar da nullPointerException*/
    }
    
    @Override
    @Transactional
    public void deletarLancamento(Lancamento lancamento) {
        /*so deleta se existir um lancamento salvo*/
        Objects.requireNonNull(lancamento.getId()); /*Checagem: para garantir que esteja passando o lancamento salvo, id nao pode ser nulo*/
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
        /*valida o status - nullo ou invalido*/
        if (status == null)
            throw new RegraDeNegocioException("O status [" + status + "] é invalido, forneça um status valido.");
        lancamento.setStatusLancamento(status);
        atualizarLancamento(lancamento);
    }
    
    @Override
    public void validarLancamento(Lancamento lancamento) {
        /*informar uma descrição válida, trim remove espaço vazio no inicio e no fim tornando um string vazia*/
        if (lancamento.getDescricao() == null || lancamento.getDescricao().trim().equals("")) {
            throw new RegraDeNegocioException("Informar uma descrição válida.");
        }
        
        if (lancamento.getMes() == null || lancamento.getMes() < 1 || lancamento.getMes() > 12) {
            throw new RegraDeNegocioException("Informar um mês válido.");
        }
        
        if (lancamento.getAno() == null || lancamento.getAno().toString().length() != 4) {
            throw new RegraDeNegocioException("Informar um ano válido.");
        }
        
        if (lancamento.getUsuario() == null || lancamento.getUsuario().getId() == null) {
            throw new RegraDeNegocioException("Informar um Usuário.");
        }
        /*compareTo para compara valor como BigDecimal*/
        if (lancamento.getValor() == null || lancamento.getValor().compareTo(BigDecimal.ZERO) < 1) {
            throw new RegraDeNegocioException("Informe o valor acima de 1 real.");
        }
        
        if (lancamento.getTipoLancamento() == null) {
            throw new RegraDeNegocioException("Informar um tipo de lancamento.");
        }

        /* Verificar se a data é null e não colocar a data de ontem
        if (lancamento.getDataCadastro() == null || lancamento.getDataCadastro().isAfter(LocalDate.now())) {
            throw new RegraDeNegocioException("Data nula ou vazia. Informe uma data válida.");
        }*/
    }
    
    @Override
    public Optional<Lancamento> obterLancamentoPorId(Long id) {
        return lancamentoRepository.findById(id);
    }
    
    @Override
    @Transactional
    public BigDecimal obterSaldoPorUsuario(Long id) {
        BigDecimal receita = lancamentoRepository
                .obterSaldoPorTipoLancamentoEUsuarioEstatus(id, TipoLancamento.RECEITA, StatusLancamento.EFETIVADO);
        BigDecimal despesa = lancamentoRepository
                .obterSaldoPorTipoLancamentoEUsuarioEstatus(id, TipoLancamento.DESPESA, StatusLancamento.EFETIVADO);
        
        /*se ambas forem nulas, define como zero
        if (receita == null){
            receita = BigDecimal.ZERO;
        }
        if (despesa == null){
            despesa = BigDecimal.ZERO;
        }*/
        /*Operador ternario*/
        receita = receita != null ? receita : BigDecimal.ZERO;
        despesa = despesa != null ? despesa : BigDecimal.ZERO;
        
        /*calcula o saldo*/
        return receita.subtract(despesa);
    }
}
    



