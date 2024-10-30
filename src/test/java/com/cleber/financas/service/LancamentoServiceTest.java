package com.cleber.financas.service;

import com.cleber.financas.exception.RegraDeNegocioException;
import com.cleber.financas.model.entity.Lancamento;
import com.cleber.financas.model.entity.StatusLancamento;
import com.cleber.financas.model.repository.LancamentoRepository;
import com.cleber.financas.model.repository.LancamentoRepositoryTest;
import com.cleber.financas.service.impl.LancamentoServiceImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.data.domain.Example;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
public class LancamentoServiceTest {
    /*chamada dos metodo reais para a classe testada - service*/
    @SpyBean
    LancamentoServiceImpl serviceImpl;
    /*simulação de comportamento do repository das chamadas dentro service*/
    @MockBean
    LancamentoRepository lancamentoRepository;
    
    @Test
    public void deveSalvarUmLancamento() {
        /*cenario*/
        Lancamento lancamentoASerSalvo = LancamentoRepositoryTest.criarLancamento();
        /*impedir a validação ao salvar o lancamento*/
        doNothing().when(serviceImpl).validarLancamento(lancamentoASerSalvo);
        /*outro lancamento com o id definido, simulação do que vai ser salvo*/
        Lancamento salvandoLancamento = LancamentoRepositoryTest.criarLancamento();
        salvandoLancamento.setId(1L);
        //salvandoLancamento.setStatusLancamento(StatusLancamento.PENDENTE);
        /*ao chamar lancamento a ser salvo, retorna, salvando lancamento - simulação*/
        Mockito.when(lancamentoRepository.save(lancamentoASerSalvo)).thenReturn(salvandoLancamento);
        /*ação*/
        /*chamando o metodo de salvar para retornar o lancamento salvo*/
        Lancamento lancamento = serviceImpl.salvarLancamento(lancamentoASerSalvo);
        
        /*verificação*/
        assertThat(lancamento.getId()).isEqualTo(salvandoLancamento.getId());
        assertThat(lancamento.getStatusLancamento()).isEqualTo(StatusLancamento.PENDENTE);
    }
    
    @Test
    public void deveAtualizarUmLancamento() {
        /*cenario*/
        /*esse lancamento já esta salvo*/
        Lancamento lancamentoSalvo = LancamentoRepositoryTest.criarLancamento();
        lancamentoSalvo.setId(1L);
        /*impedir a validação ao salvar o lancamento*/
        doNothing().when(serviceImpl).validarLancamento(lancamentoSalvo);
        /*retorna ele mesmo atualizado*/
        Mockito.when(lancamentoRepository.save(lancamentoSalvo)).thenReturn(lancamentoSalvo);
        /*ação*/
        serviceImpl.atualizarLancamento(lancamentoSalvo);
        /*verificação*/
        /*verifica que chamou pelo menos uma vez o metodo de salvar*/
        verify(lancamentoRepository, times(1)).save(lancamentoSalvo);
        /*caso queira checar se os valores estão sendo retornados - id...*/
    }
    
    @Test
    public void deveAtualizarOStatusDoLancamento(){
        /*cenario*/
        Lancamento lancamento = LancamentoRepositoryTest.criarLancamento();
        lancamento.setId(1l);
        lancamento.setStatusLancamento(StatusLancamento.PENDENTE);
        /*criar o novo status para o lancamento*/
        StatusLancamento statusAtualizado = StatusLancamento.EFETIVADO;
        /*nao e necessario chamar o metodo atualizar de fato*/
        Mockito.doReturn(lancamento).when(serviceImpl).atualizarLancamento(lancamento);
        /*ação*/
        serviceImpl.atualizarStatus(lancamento, statusAtualizado);
        
        /*verificação*/
        assertThat(lancamento.getStatusLancamento()).isEqualTo(statusAtualizado);
        Mockito.verify(serviceImpl).atualizarLancamento(lancamento);
        
        
        
    }
    
    @Test
    public void deveDeletarUmLancamento() {
        /*cenário*/
        Lancamento lancamento = LancamentoRepositoryTest.criarLancamento();
        lancamento.setId(1L);
        /*ação*/
        //doNothing().when(lancamentoRepository).delete(lancamento);
        serviceImpl.deletarLancamento(lancamento);
        /*verificação*/
        verify(lancamentoRepository, times(1)).delete(lancamento);
    }
    
    @Test
    public void deveBuscarUmLancamento() { /*filtro*/
        /*cenario*/
        Lancamento lancamento = LancamentoRepositoryTest.criarLancamento();
        lancamento.setId(1L);
        /*criar uma lista de lancamentos*/
        List<Lancamento> listaDeLancamento = Arrays.asList(lancamento);
        Mockito.when(lancamentoRepository.findAll(Mockito.any(Example.class))).thenReturn(listaDeLancamento);
        /*execução*/
        List<Lancamento> resultado = serviceImpl.buscarLancamento(lancamento);
        /*verificação*/
        assertThat(resultado)
                .isNotEmpty() /*trata=se de uma coleção*/
                .hasSize(1)
                .contains(lancamento);
    }
    
    @Test
    public void deveLancarErroAoTentarDeletarUmLancamentoNaoSalvo() {
        /*cenário*/
        Lancamento lancamento = LancamentoRepositoryTest.criarLancamento();
        /*ação*/
        doNothing().when(lancamentoRepository).delete(lancamento); /*só pra teste :() S2*/
        catchThrowableOfType(() -> serviceImpl.atualizarLancamento(lancamento), NullPointerException.class);
        /*verificação*/
        verify(lancamentoRepository, Mockito.never()).delete(lancamento);
        
    }
    
    @Test
    public void deveLancarErroAoTentarAtualizarUmLancamentoNaoSalvo() {
        /*cenario*/
        Lancamento lancamentoASerSalvo = LancamentoRepositoryTest.criarLancamento();
        /*ação e verificação*/
        catchThrowableOfType(() -> serviceImpl.atualizarLancamento(lancamentoASerSalvo), NullPointerException.class);
        /*certificar que nunca foi salvo*/
        verify(lancamentoRepository, Mockito.never()).save(lancamentoASerSalvo);
        
    }
    
    @Test
    public void naoDeveSalvarLancamentoQuandoDerErroDeValidacao() {
        /*cenario*/
        Lancamento lancamentoASerSalvo = LancamentoRepositoryTest.criarLancamento();
        /*lance o erro quando for salvar o lancamento*/
        Mockito.doThrow(RegraDeNegocioException.class).when(serviceImpl).validarLancamento(lancamentoASerSalvo);
        /*ação e verificação*/
        catchThrowableOfType(() -> serviceImpl.salvarLancamento(lancamentoASerSalvo), RegraDeNegocioException.class);
        /*certificar que nunca foi salvo*/
        verify(lancamentoRepository, Mockito.never()).save(lancamentoASerSalvo);
    }
}
