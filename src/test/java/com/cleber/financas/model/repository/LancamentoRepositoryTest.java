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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.time.LocalDate;

@RunWith(SpringRunner.class)
@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Profile("test")
public class LancamentoRepositoryTest {
    
    @Autowired
    LancamentoRepository lancamentoRepository;
    @Autowired
    TestEntityManager testEntityManager;
    
    @Test
    public void deveSalvarUmLancamento() {
        /*cenario*/
        Lancamento salvarLancamento = criarLancamento();
        /*ação/ excução*/
        Lancamento lancamento = lancamentoRepository.save(salvarLancamento);
        /*verificação*/
        Assertions.assertThat(salvarLancamento.getId()).isNotNull();
    }
   
    private static Lancamento criarLancamento() {
        return Lancamento.builder()
                .descricao("Fatura do mes de outubro")
                .mes(10)
                .ano(2024)
                .valor(BigDecimal.valueOf(100.50))
                .tipoLancamento(TipoLancamento.DESPESA)
                .statusLancamento(StatusLancamento.PENDENTE)
                .dataCadastro(LocalDate.now())
                .build();
    }
}
