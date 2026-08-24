package com.cleber.financas.model.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

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
import com.cleber.financas.model.entity.Usuario;
import com.cleber.financas.model.enums.StatusLancamento;

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
        Assertions.assertThat(lancamento.getUuid()).isNotNull();
    }
    
    @Test
    public void deveAtualizarUmLancamento(){
        Lancamento lancamento = persistirUmLancamento();
        /*atualizar*/
        lancamento.setMes(10);
        lancamento.setStatusLancamento(StatusLancamento.EFETIVADO);
        
        lancamentoRepository.save(lancamento);
        
        /*comparar com o lancamento ja salvo na base de dados*/
        Lancamento lancamentoAtualizado = testEntityManager.find(Lancamento.class, lancamento.getUuid());
        
        Assertions.assertThat(lancamentoAtualizado.getMes()).isEqualTo(10);
        Assertions.assertThat(lancamentoAtualizado.getStatusLancamento()).isEqualTo(StatusLancamento.EFETIVADO);
    }
    
    @Test
    public void deveBuscarUmLancamentoPorId(){
        Lancamento lancamento = persistirUmLancamento();
        
        Optional<Lancamento> lancamentoEncontrado = lancamentoRepository.findById(lancamento.getUuid());
        
        Assertions.assertThat(lancamentoEncontrado.isPresent()).isTrue();
    }
    
    @Test
    public void deveDeletarUmLancamento(){
        Lancamento lancamento = persistirUmLancamento();
        testEntityManager.persist(lancamento);
        
        lancamento = testEntityManager.find(Lancamento.class, lancamento.getUuid());
        
        lancamentoRepository.delete(lancamento);
        
        Lancamento lancamentoInxistente = testEntityManager.find(Lancamento.class, lancamento.getUuid());
        
        Assertions.assertThat(lancamentoInxistente).isNull();
    }
    
    private Lancamento persistirUmLancamento() {
        Lancamento lancamento = criarLancamento();
        testEntityManager.persist(lancamento);
        return lancamento;
    }
    
    public static Lancamento criarLancamento() {
        Usuario usuario = criarUsuario();
        return Lancamento.builder()
                .descricao("Lancamento de teste para deletar")
                .ano(2024)
                .usuario(usuario)
                .mes(11)
                .valor(BigDecimal.valueOf(10.55))
                .statusLancamento(StatusLancamento.PENDENTE)
                .build();
    }

    public static Usuario criarUsuario() {
        return Usuario.builder()
                .uuid(UUID.randomUUID())
                .nomeCompleto("Usuário")
                .cpf("12345678900")
                .nomeUsuario("usuario")
                .email("usuario@gmail.com")
                .senha("Senha@123")
                .dataCadastro(LocalDateTime.now())
                .build();
    }
}
