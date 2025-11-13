package com.cleber.financas.model.repository;

import com.cleber.financas.model.entity.Lancamento;
import com.cleber.financas.model.entity.StatusLancamento;
import com.cleber.financas.model.entity.TipoLancamento;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.Optional;

@RunWith(SpringRunner.class)
@DataJpaTest
//@SpringBootTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@ActiveProfiles("test")
public class LancamentoRepositoryTest {
    @Autowired
    LancamentoRepository lancamentoRepository;
    @Autowired
    TestEntityManager testEntityManager;
    
    @Test
    public void deveSalvarUmLancamento(){
        Lancamento lancamento = criarLancamento();
        lancamento = lancamentoRepository.save(lancamento);
        Assertions.assertThat(lancamento.getId()).isNotNull();
    }
    
    @Test
    public void deveAtualizarUmLancamento(){
        Lancamento lancamento = persistirUmLancamento();
        /*atualizar*/
        lancamento.setMes("setembro");
        lancamento.setStatusLancamento(StatusLancamento.EFETIVADO);
        
        lancamentoRepository.save(lancamento);
        
        /*comparar com o lancamento ja salvo na base de dados*/
        Lancamento lancamentoAtualizado = testEntityManager.find(Lancamento.class, lancamento.getId());
        
        Assertions.assertThat(lancamentoAtualizado.getMes()).isEqualTo(10);
        Assertions.assertThat(lancamentoAtualizado.getStatusLancamento()).isEqualTo(StatusLancamento.EFETIVADO);
    }
    
    @Test
    public void deveBuscarUmLancamentoPorId(){
        Lancamento lancamento = persistirUmLancamento();
        
        Optional<Lancamento> lancamentoEncontrado = lancamentoRepository.findById(lancamento.getId());
        
        Assertions.assertThat(lancamentoEncontrado.isPresent()).isTrue();
    }
    
    @Test
    public void deveDeletarUmLancamento(){
        Lancamento lancamento = persistirUmLancamento();
        testEntityManager.persist(lancamento);
        
        lancamento = testEntityManager.find(Lancamento.class, lancamento.getId());
        
        lancamentoRepository.delete(lancamento);
        
        Lancamento lancamentoInxistente = testEntityManager.find(Lancamento.class, lancamento.getId());
        
        Assertions.assertThat(lancamentoInxistente).isNull();
    }
    
    private Lancamento persistirUmLancamento() {
        Lancamento lancamento = criarLancamento();
        testEntityManager.persist(lancamento);
        return lancamento;
    }
    
    public static Lancamento criarLancamento() {
        return Lancamento.builder()
                .descricao("Lancamento de teste para deletar")
                .ano(2024)
                .mes("novembro")
                .valor(BigDecimal.valueOf(10.55))
                .statusLancamento(StatusLancamento.PENDENTE)
                .build();
    }
}
