package com.cleber.financas.model.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.cleber.financas.model.entity.Lancamento;
import com.cleber.financas.model.entity.StatusLancamento;
import com.cleber.financas.model.entity.TipoLancamento;

@RunWith(SpringRunner.class)
@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@ActiveProfiles("test")
public class LancamentoRepositoryTest {

    @Autowired
    LancamentoRepository lancamentoRepository;
    @Autowired
    TestEntityManager testEntityManager;

    @Test
    public void deveSalvarUmLancamento(){
        /*cenario*/
        Lancamento lancamento = salvarLancamento();
        /*ação/ execução*/
        lancamento = lancamentoRepository.save(lancamento);
        /*verificação*/
        Assertions.assertThat(lancamento.getId()).isNotNull();

    }

    @Test
    public void deveAtualizarUmaLancamento(){
        /*ação*/
        Lancamento lancamento = criarESalvarLancamento();
        lancamento.setAno(2023);
        lancamento.setMes(11);
        lancamento.setStatusLancamento(StatusLancamento.EFETIVADO);

        lancamentoRepository.save(lancamento);

        /*ação*/
        Lancamento lancamentoAtualizado = testEntityManager.find(Lancamento.class, lancamento.getId());

        /*verificaçãõ*/
        Assertions.assertThat(lancamentoAtualizado.getAno()).isEqualTo(2023);
        Assertions.assertThat(lancamento.getMes()).isEqualTo(11);
        Assertions.assertThat(lancamento.getStatusLancamento()).isEqualTo(StatusLancamento.EFETIVADO);

    }
    
    @Test
    public void deveBuscarUmLancamentoPorId() {
    	/*cenario*/
    	Lancamento lancamento = criarESalvarLancamento();
    	
    	/*execução*/
    	Optional<Lancamento> buscarLancamento = lancamentoRepository.findById(lancamento.getId());
    	
    	/*verificação*/
    	Assertions.assertThat(buscarLancamento.isPresent()).isTrue();
    	
    	
    }

    @Test
    public void deveDeletarUmLancamento(){
        /*cenario*/
        Lancamento lancamento = criarESalvarLancamento();

        /*acao*/
        lancamento = testEntityManager.find(Lancamento.class, lancamento.getId());
        lancamentoRepository.delete(lancamento);
        Lancamento lancamentoInexistente = testEntityManager.find(Lancamento.class,lancamento.getId());

        /*verificação*/
        Assertions.assertThat(lancamentoInexistente).isNull();
    }

    private Lancamento criarESalvarLancamento() {
        Lancamento lancamento = salvarLancamento();
        testEntityManager.persist(lancamento);
        return lancamento;
    }

    public static Lancamento salvarLancamento(){
        return Lancamento.builder()
                //.id(1L)
                .descricao("Fatura do mes de outubro")
                .mes(10)
                .ano(2024)
                .valor(BigDecimal.valueOf(10.50))
                .dataCadastro(LocalDate.now())
                .tipoLancamento(TipoLancamento.RECEITA)
                .statusLancamento(StatusLancamento.PENDENTE)
                //.usuario(Usuario.builder().build())
                .dataCadastro(LocalDate.now())
                .build();
    }
}
