package com.cleber.financas.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import com.cleber.financas.api.dto.CriptoMoedaDTO;
import com.cleber.financas.exception.RegraDeNegocioException;
import com.cleber.financas.model.entity.CriptoMoeda;
import com.cleber.financas.model.entity.Usuario;
import com.cleber.financas.model.enums.StatusTransacao;
import com.cleber.financas.model.enums.TipoTransacao;
import com.cleber.financas.model.repository.CriptoMoedaRepository;
import com.cleber.financas.model.repository.UsuarioRepository;

import jakarta.persistence.EntityNotFoundException;

/**
 * Testes de integração para a camada de Service relacionada a CriptoMoeda.
 *
 * "PENSAR PRIMEIRO, IMPLEMENTAR DEPOIS". Clairton
 *
 */
@SpringBootTest
@AutoConfigureTestEntityManager
public class CriptoMoedaServiceTest {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private CriptoMoedaService criptoMoedaService; // o spring cuida implementação - @Service

    @Autowired
    private CriptoMoedaRepository criptoMoedaRepository;

    @Autowired
    private TestEntityManager entityManager;

    @BeforeEach
    public void setup() {
        /**
         * Limpar dados anteriores para garantir isolamento entre testes.
         **/
        criptoMoedaRepository.deleteAll();
    }

    @Test
    @DisplayName("Deve criar um usuario")
    @Transactional
    @Rollback(false)
    void deveCriarUmUsuarioEGarantirQueElePossuiUmId() {
        /**cenario**/
        Usuario usuario = criarUsuario();
        /**execução**/
        Usuario usuarioSalvo = usuarioRepository.save(usuario);
        /**verificação**/
        assertThat(usuarioSalvo.getUuid()).isNotNull();
    }

    /**Objetivo 1: salvar múltiplas entidades e verificar contagem total com findAll()**/
    @Test
    @DisplayName("deve retornar contagem correta ao salvar múltiplas entidades")
    void deveRetornarContagemCorretaQuandoSalvarMultiplasMoedas() {
        /**cenario**/
        UUID usuarioId = criarUsuarioEPersistir();
        CriptoMoedaDTO sol = criarCriptomoeda();
        sol.setAtivo("SOL");
        sol.setTipoTransacao(TipoTransacao.COMPRA);

        CriptoMoedaDTO bnb = criarCriptomoeda();
        bnb.setAtivo("BNB");
        bnb.setTipoTransacao(TipoTransacao.COMPRA);
        bnb.setStatusTransacao(StatusTransacao.ABERTA);

        CriptoMoedaDTO xrp = criarCriptomoeda();
        xrp.setAtivo("XRP");
        xrp.setTipoTransacao(TipoTransacao.COMPRA);

        /**ação**/
        criptoMoedaService.salvarCriptomoeda(sol, usuarioId);
        criptoMoedaService.salvarCriptomoeda(bnb, usuarioId);
        criptoMoedaService.salvarCriptomoeda(xrp, usuarioId);

        /**verificação**/
        List<CriptoMoeda> todas = criptoMoedaRepository.findAll();
        assertThat(todas).isNotNull();
        assertThat(todas).hasSize(3);
    }

    /**Objetivo 2: salvar entidade válida e verificar geração de ID**/
    @Test
    @DisplayName("deve gerar ID automaticamente quando salvar entidade válida")
    void deveGerarIdAoSalvarEntidadeValidaQuandoSalvar() {
        /**cenario**/
        UUID usuarioId = criarUsuarioEPersistir();
        CriptoMoedaDTO dto = criarCriptomoeda();

        /**ação**/
        CriptoMoedaDTO salvar = criptoMoedaService.salvarCriptomoeda(dto, usuarioId);

        /**verificação**/
        assertThat(salvar).isNotNull();
        assertThat(salvar.getUuid()).isNotNull();
    }

    /**Objetivo 3: salvar umam criptomoeda e recuperar por ID com todos os campos corretos**/
    @Test
    @DisplayName("deve persistir uma criptomoeda e recuperar por ID com todos os campos corretos")
    void devePersistirCriptomoedaERetornarPorIdQuandoSalvar() {
        /**cenario**/
        UUID usuarioId = criarUsuarioEPersistir();
        CriptoMoedaDTO dto = criarCriptomoeda();

        /**ação**/
        CriptoMoedaDTO salvar = criptoMoedaService.salvarCriptomoeda(dto, usuarioId);

        /**verificação**/
        CriptoMoeda moedaEncontrada = criptoMoedaRepository.findById(salvar.getUuid()).orElseThrow();
        assertThat(moedaEncontrada).isNotNull();
        assertThat(moedaEncontrada.getUuid()).isNotNull();
        assertThat(moedaEncontrada.getCriptomoeda()).isEqualTo(dto.getAtivo());
        assertThat(moedaEncontrada.getValorAtual()).isEqualByComparingTo(dto.getValorAtualAtivo());
        assertThat(moedaEncontrada.getValorInvestido()).isEqualByComparingTo(dto.getValorInvestido());
        assertThat(moedaEncontrada.getMes()).isEqualTo(dto.getMes());
    }

    /**Objetivo 4: deve lancar uma excessao quando tentar salvar criptomoeda com valor nulo**/
    @Test
    @DisplayName("deve lançar exceção quando tentar salvar com criptomoeda nula")
    @Transactional
    void deveLancarExcecaoQuandoSalvarComCriptomoedaNula() {
        /**cenario**/
        UUID usuarioId = criarUsuarioEPersistir();
        CriptoMoedaDTO dto = criarCriptomoeda();
        dto.setAtivo(null);

        /**ação**/ /**verificação**/
        assertThatThrownBy(() -> {
            criptoMoedaService.salvarCriptomoeda(dto, usuarioId);
            entityManager.flush();
        }).isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
    }

    /**Objetivo 5: deve lancar uma excessao quando tentar salvar com usuario nulo**/
    @Test
    @DisplayName("deve lançar exceção quando tentar salvar com id de usuário nulo")
    @Transactional
    void deveLancarExcecaoQuandoSalvarComUsuarioNulo() {
        /**cenario**/
        CriptoMoedaDTO dto = criarCriptomoeda();

        /**ação**/ /**verificação**/
        assertThatThrownBy(() -> {
            criptoMoedaService.salvarCriptomoeda(dto, null);
            entityManager.flush();
        }).isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
    }

    /**Objetivo 6: lançar exceção ao salvar com valorAtual nulo**/
    @Test
    @DisplayName("deve lançar exceção quando tentar salvar com valorAtual nulo")
    @Transactional
    void deveLancarExcecaoQuandoSalvarComValorAtualNulo() {
        /**cenario**/
        UUID usuarioId = criarUsuarioEPersistir();
        CriptoMoedaDTO dto = criarCriptomoeda();
        dto.setValorAtualAtivo(null);

        /**ação**/ /**verificação**/
        assertThatThrownBy(() -> {
            criptoMoedaService.salvarCriptomoeda(dto, usuarioId);
            entityManager.flush();
        }).isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
    }

    /**Objetivo 7: buscar por criptomoeda existente e retornar entidade correta**/
    @Test
    @DisplayName("deve retornar entidade correta ao buscar por criptomoeda existente")
    void deveRetornarEntidadeAoBuscarPorCriptomoedaExistente() {
        /**cenario**/
        UUID usuarioId = criarUsuarioEPersistir();
        CriptoMoedaDTO dto = criarCriptomoeda();
        dto.setAtivo("ETH");
        CriptoMoedaDTO salvo = criptoMoedaService.salvarCriptomoeda(dto, usuarioId);

        /**ação**/
        var opt = criptoMoedaRepository.findByCriptomoeda("ETH");

        /**verificação**/
        assertThat(opt).isPresent();
        assertThat(opt.get().getUuid()).isEqualTo(salvo.getUuid());
    }

    /**Objetivo 8: buscar por criptomoeda inexistente e retornar Optional.empty()**/
    @Test
    @DisplayName("deve retornar Optional.empty ao buscar por criptomoeda inexistente")
    void deveRetornarEmptyQuandoBuscarPorCriptomoedaInexistente() {
        /**cenario**/
        // nenhum dado salvo

        /**ação**/
        var optional = criptoMoedaRepository.findByCriptomoeda("MOEDA_INEXISTENTE");

        /**verificação**/
        assertThat(optional).isEmpty();
    }

    /**Objetivo 9: buscar por ID existente e retornar entidade correta**/
    @Test
    @DisplayName("deve retornar entidade correta ao buscar por ID existente")
    void deveRetornarEntidadeAoBuscarPorIdExistente() {
        /**cenario**/
        UUID usuarioId = criarUsuarioEPersistir();
        CriptoMoedaDTO dto = criarCriptomoeda();
        CriptoMoedaDTO salva = criptoMoedaService.salvarCriptomoeda(dto, usuarioId);

        /**ação**/
        var optional = criptoMoedaRepository.findById(salva.getUuid());

        /**verificação**/
        assertThat(optional).isPresent();
        assertThat(optional.get().getCriptomoeda()).isEqualTo(dto.getAtivo());
    }

    /**Objetivo 10: buscar por ID inexistente e retornar Optional.empty()**/
    @Test
    @DisplayName("deve retornar Optional.empty ao buscar por ID inexistente")
    void deveRetornarEmptyQuandoBuscarPorIdInexistente() {
        /**cenario**/
        //nehum dado salvo
        /**ação**/
        var optional = criptoMoedaRepository.findById(java.util.UUID.randomUUID());

        /**verificação**/
        assertThat(optional).isEmpty();
    }

    /**Objetivo 11: retornar todos ao objetos marcados com enums específicos**/
    @Test
    @DisplayName("deve retornar apenas objetos do StatusTransacao/TipoTransacao especificados")
    void deveRetornarEntidadesPorEnumsQuandoBuscar() {
        /**cenario**/
        UUID usuarioId = criarUsuarioEPersistir();
        CriptoMoedaDTO criptomoedaA = criarCriptomoeda();
        criptomoedaA.setAtivo("ETH");
        criptomoedaA.setStatusTransacao(StatusTransacao.ANALISAR);

        CriptoMoedaDTO criptomoedaB = criarCriptomoeda();
        criptomoedaB.setAtivo("SOL");
        criptomoedaB.setTipoTransacao(TipoTransacao.COMPRA);

        criptoMoedaService.salvarCriptomoeda(criptomoedaA, usuarioId);
        criptoMoedaService.salvarCriptomoeda(criptomoedaB, usuarioId);

        /**ação**/
        List<CriptoMoeda> analisar = criptoMoedaRepository.findByStatusTransacao(StatusTransacao.ANALISAR);
        List<CriptoMoeda> compra = criptoMoedaRepository.findByTipoTransacao(TipoTransacao.COMPRA);

        /**verificação**/
        assertThat(analisar).isNotNull();
        assertThat(analisar).anyMatch(criptoMoeda -> criptoMoeda.getCriptomoeda().equals("ETH"));

        assertThat(compra).isNotNull();
        assertThat(compra).hasSize(1);
        assertThat(compra.get(0).getCriptomoeda()).isEqualTo("SOL");
    }

    /**Objetivo 12: existsByCriptomoeda retorna true para existente**/
    @Test
    @DisplayName("deve retornar true para criptomoeda existente usando findByCriptomoeda como verificação")
    void deveRetornarTrueQuandoCriptomoedaExistir() {
        /**cenario**/
        UUID usuarioId = criarUsuarioEPersistir();
        CriptoMoedaDTO dto = criarCriptomoeda();
        dto.setAtivo("EXISTE");
        criptoMoedaService.salvarCriptomoeda(dto, usuarioId);

        /**ação**/
        boolean existe = criptoMoedaRepository.findByCriptomoeda("EXISTE").isPresent();

        /**verificação**/
        assertThat(existe).isTrue();
    }

    /**Objetivo 13: existsByCriptomoeda retorna false para inexistente**/
    @Test
    @DisplayName("deve retornar false para criptomoeda inexistente usando findByCriptomoeda como verificação")
    void deveRetornarFalseQuandoCriptomoedaNaoExistir() {
        /**cenario**/
        // nada foi salvo
        /**ação**/
        boolean existe = criptoMoedaRepository.findByCriptomoeda("NAO_EXISTE").isPresent();

        /**verificação**/
        assertThat(existe).isFalse();
    }

    /**Objetivo 14: atualizar valor do objeto existente e persistir corretamente**/
    @Test
    @DisplayName("deve atualizar valor e persistir corretamente quando o objeto existe")
    void deveAtualizarPrecoQuandoObjetoExistir() {
        /**cenario**/
        UUID usuarioId = criarUsuarioEPersistir();
        CriptoMoedaDTO dto = criarCriptomoeda();
        CriptoMoedaDTO salvar = criptoMoedaService.salvarCriptomoeda(dto, usuarioId);

        /**ação**/
        CriptoMoedaDTO dtoParaAtualizar = salvar;
        dtoParaAtualizar.setValorAtualAtivo(BigDecimal.valueOf(5555));
        criptoMoedaService.atualizarCriptomoeda(dtoParaAtualizar);

        /**verificação**/
        CriptoMoeda atualizada = criptoMoedaService.obterCriptomoedaPorId(salvar.getUuid()).orElseThrow(); // buscar depois da atualização
        assertThat(atualizada.getValorAtual()).isEqualByComparingTo(BigDecimal.valueOf(5555));
    }

    /**Objetivo 15: atualizar tipoTransacao e verificar persistência**/
    @Test
    @DisplayName("deve atualizar tipoTransacao e persistir corretamente quando objeto existe")
    void deveAtualizarTipoTransacaoQuandoObjetoExistir() {
        /**cenario**/
        UUID usuarioId = criarUsuarioEPersistir();
        CriptoMoedaDTO dto = criarCriptomoeda();
        CriptoMoedaDTO salvar = criptoMoedaService.salvarCriptomoeda(dto, usuarioId);

        /**ação**/
        CriptoMoedaDTO dtoParaAtualizar = salvar;
        dtoParaAtualizar.setTipoTransacao(TipoTransacao.COMPRA);
        criptoMoedaService.atualizarCriptomoeda(dtoParaAtualizar);

        /**verificação**/
        CriptoMoeda atualizada = criptoMoedaService.obterCriptomoedaPorId(salvar.getUuid()).orElseThrow(); // busca depois da atualização
        assertThat(atualizada.getTipoTransacao()).isEqualTo(TipoTransacao.COMPRA);
    }

    /**Objetivo 16: lançar exceção ao tentar atualizar objeto com ID inexistente**/
    @Test
    @DisplayName("deve lançar exceção ao tentar atualizar objeto com ID inexistente")
    @Transactional
    void deveLancarExcecaoQuandoAtualizarIdInexistente() {
        /**cenario**/
         CriptoMoedaDTO dto = criarCriptomoeda();
         dto.setUuid(UUID.randomUUID());
        /**ação**/ /**verificação**/
        assertThatThrownBy(() -> criptoMoedaService.atualizarCriptomoeda(dto))
                .isInstanceOf(RegraDeNegocioException.class);
    }

    /**Objetivo 17: deletar por ID existente e verificar ausência no banco**/
    @Test
    @DisplayName("deve deletar por ID existente e verificar ausência")
    void deveDeletarPorIdQuandoExistir() {
        /**cenario**/
        UUID usuarioId = criarUsuarioEPersistir();
        CriptoMoedaDTO dto = criarCriptomoeda();
        CriptoMoedaDTO salvo = criptoMoedaService.salvarCriptomoeda(dto, usuarioId);

        /**ação**/
        CriptoMoedaDTO dtoParaDeletar = salvo;
        criptoMoedaService.deletarCriptoMoeda(dtoParaDeletar);

        /**verificação**/
        assertThat(criptoMoedaService.obterCriptomoedaPorId(salvo.getUuid())).isEmpty();
    }

    /**Objetivo 18: lançar exceção ao tentar deletar ID inexistente**/
    @Test
    @DisplayName("deve lançar exceção ao tentar deletar ID inexistente")
    @Transactional
    void deveLancarExcecaoQuandoDeletarIdInexistente() {
        /**cenario**/
        CriptoMoedaDTO idInexistente = criarCriptomoeda();
        idInexistente.setUuid(UUID.randomUUID());
        /**ação**/ /**verificação**/
        assertThatThrownBy(() -> criptoMoedaService.deletarCriptoMoeda(idInexistente))
                .isInstanceOf(EntityNotFoundException.class);
    }

    /**METODOS AUXILIARES**/

    /** auxiliar exigido: retorna DTO válido pronto para salvar */
    public CriptoMoedaDTO criarCriptomoeda() {
        return CriptoMoedaDTO.builder()
                .dataEntrada(LocalDate.now())
                .mes(LocalDate.now().getMonthValue())
                .corretora("Binance")
                .ativo("BTC")
                .alavancagem(null)
                .moedaCorrente("USD")
                .valorInvestido(BigDecimal.valueOf(1000))
                .valorAtualAtivo(BigDecimal.valueOf(1100))
                .fracaoAtivo(BigDecimal.valueOf(0.5))
                .dataSaida(null)
                .statusTransacao(null)
                .tipoTransacao(null)
                .build();
    }

    /** retorna o UUID do usuário persistido, usado como segundo parâmetro de salvarCriptomoeda */
    public UUID criarUsuarioEPersistir() {
        return persistirUsuario().getUuid();
    }

    /**cria e persiste um Usuario mínimo para satisfazer FK **/
   public Usuario persistirUsuario() {
       Usuario usuario = Usuario.builder()
               .nomeCompleto("Usuário")
               .cpf("12345678900")
               .nomeUsuario("usuario" + Instant.now().toEpochMilli()) // Evitar duplicidade se houver unique constraints
               .email("usuario" + Instant.now().toEpochMilli() + "@gmail.com")
               .senha("Senha@123")
               .dataCadastro(LocalDateTime.now())
               .build();
       return usuarioRepository.save(usuario);
   }

    public static Usuario criarUsuario() {
        return Usuario.builder()
//                .id(1L)
                .nomeCompleto("Usuário")
                .cpf("12345678900")
                .nomeUsuario("usuario")
                .email("usuario@gmail.com")
                .senha("Senha@123")
                .dataCadastro(LocalDateTime.now())
                .build();
    }
}
