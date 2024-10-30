package com.cleber.financas.model.repository;

import com.cleber.financas.model.entity.Lancamento;
import com.cleber.financas.model.entity.StatusLancamento;
import com.cleber.financas.model.entity.TipoLancamento;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.Optional;

import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
public class LancamentoRepositoryTest {
    @Autowired
    LancamentoRepository lancamentoRepository;
    @Autowired
    TestEntityManager entityManager;
    
    @DisplayName("Lancamento salvo com sucesso")
    @Test
    public void deveSalvarLancamento(){
        Lancamento lancamento = criarLancamento();
        /*ação*/
        lancamento = lancamentoRepository.save(lancamento);
        /*verificação*/
        Assertions.assertThat(lancamento.getId()).isNotNull();
    }
    
    @Test
    public void deveBuscarLancamentoPorId(){
        /*cenario*/
        Lancamento lancamento = criarESalvarLancamento();
        /*ação*/
        Optional<Lancamento> lancamentoEncontrado = lancamentoRepository.findById(lancamento.getId());
        /*verificação*/
        Assertions.assertThat(lancamentoEncontrado.isPresent()).isTrue();
    }
    
    @Test
    public void deveAtualizarLancamento(){
        Lancamento lancamento = criarESalvarLancamento();
        lancamento.setMes(10);
        lancamento.setStatusLancamento(StatusLancamento.EFETIVADO);
        lancamentoRepository.save(lancamento);
        /*verificaçaõ*/
        /*comparar lancamento previamente salvo com as atualização feitas*/
        Lancamento lancamentoAtualizado = entityManager.find(Lancamento.class, lancamento.getId());
        /*verificação*/
        Assertions.assertThat(lancamentoAtualizado.getMes()).isEqualTo(10);
        Assertions.assertThat(lancamentoAtualizado.getStatusLancamento()).isEqualTo(StatusLancamento.EFETIVADO);
    }
    
    @Test
    public void deveDeletarLancamento(){
        /*cenario*/
        /*pega o lancamento para salvar*/
        Lancamento lancamento = criarESalvarLancamento();
        /*ação*/
        /*procure o lancamento na base e dados*/
        lancamento = entityManager.find(Lancamento.class, lancamento.getId());
        /*deleta o lancamento*/
        lancamentoRepository.delete(lancamento);
        /*certifque-se que o lancamento foi deletado*/
        Lancamento lancamentoInexistente = entityManager.find(Lancamento.class, lancamento.getId());
        /*verificação*/
        Assertions.assertThat(lancamentoInexistente).isNull();
    }
    
    private Lancamento criarESalvarLancamento() {
        Lancamento lancamento = criarLancamento();
        entityManager.persist(lancamento);
        return lancamento;
    }
    
    public static Lancamento criarLancamento() {
        Lancamento lancamento = Lancamento.builder()
                .descricao("lancamento")
                .ano(2024)
                .mes(11)
                .valor(BigDecimal.valueOf(10.50))
                .tipoLancamento(TipoLancamento.RECEITA)
                .statusLancamento(StatusLancamento.PENDENTE)
                .build();
        return lancamento;
    }
}
